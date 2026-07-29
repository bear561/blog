import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// 与后端 SseEmitter 超时保持一致（AIChatController: 300s）
const STREAM_TIMEOUT_MS = 300000
const STORAGE_KEY = 'ai-chat-session'

// 整页刷新/跳转后恢复对话：messages 与 sessionId 持久化到 sessionStorage
function loadSavedState() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const saved = JSON.parse(raw)
    if (saved && Array.isArray(saved.messages) && saved.sessionId) return saved
    return null
  } catch {
    return null
  }
}

const savedState = loadSavedState()

let messageId = savedState
  ? savedState.messages.reduce((max, m) => Math.max(max, m.id || 0), 0)
  : 0
function nextId() {
  return ++messageId
}

/**
 * 逐块读取 SSE 流（fetch + ReadableStream，增量解析）。
 * 后端事件格式（Spring SseEmitter）：
 *   event:message / event:done / event:error
 *   data:<payload>（多行 data 按 SSE 规范以 \n 拼接）
 * 事件之间以空行（\n\n）分隔。
 */
async function readSseStream(body, handlers) {
  const reader = body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      let sepIndex
      while ((sepIndex = buffer.indexOf('\n\n')) !== -1) {
        const rawEvent = buffer.slice(0, sepIndex)
        buffer = buffer.slice(sepIndex + 2)
        dispatchSseEvent(rawEvent, handlers)
      }
    }
    // 连接关闭时缓冲区里可能还剩一个未以空行结尾的事件
    if (buffer.trim()) {
      dispatchSseEvent(buffer, handlers)
    }
  } finally {
    reader.releaseLock()
  }
}

function dispatchSseEvent(rawEvent, { onMessage, onError, onDone }) {
  let name = 'message'
  const dataLines = []
  for (const line of rawEvent.split('\n')) {
    if (line.startsWith('event:')) {
      name = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      // SSE 规范：冒号后的第一个空格不属于数据
      dataLines.push(line.slice(5).replace(/^ /, ''))
    }
  }
  if (dataLines.length === 0) return
  const data = dataLines.join('\n')
  if (name === 'message') onMessage?.(data)
  else if (name === 'error') onError?.(data)
  else if (name === 'done') onDone?.(data)
}

export const useAiStore = defineStore('ai', () => {
  const messages = ref(savedState?.messages ?? [])
  const sessionId = ref(savedState?.sessionId ?? 'session_' + Date.now())
  // 面板默认收起：刷新后不突然弹出，但对话内容已在 store 中
  const isOpen = ref(false)
  const isStreaming = ref(false)

  // 持久化对话与 sessionId，整页刷新也能恢复上下文
  watch(
    [messages, sessionId],
    () => {
      try {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
          messages: messages.value,
          sessionId: sessionId.value
        }))
      } catch {
        // 存储不可用或超限时静默降级，不影响聊天本身
      }
    },
    { deep: true }
  )

  function addMessage(role, content) {
    messages.value.push({ id: nextId(), role, content, timestamp: new Date().toISOString() })
  }

  async function sendMessage(content) {
    if (!content.trim() || isStreaming.value) return

    addMessage('user', content)
    isStreaming.value = true

    addMessage('assistant', '')
    const targetIndex = messages.value.length - 1
    // 通过索引取回响应式代理，流式期间逐字追加
    const appendToAssistant = (text) => {
      const msg = messages.value[targetIndex]
      if (msg) msg.content += text
    }

    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), STREAM_TIMEOUT_MS)

    try {
      const history = messages.value
        .filter(m => m.content && m.role !== 'assistant')
        .slice(0, -1)
        .map(m => ({ role: m.role, content: m.content }))

      const resp = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: content, sessionId: sessionId.value, history }),
        signal: controller.signal
      })

      if (!resp.ok) throw new Error('HTTP ' + resp.status)
      if (!resp.body) throw new Error('当前浏览器不支持流式读取')

      await readSseStream(resp.body, {
        onMessage: (chunk) => appendToAssistant(chunk),
        onError: (msg) => {
          const tip = msg || '抱歉，AI 服务暂时不可用，请稍后再试。'
          const msgObj = messages.value[targetIndex]
          if (msgObj) msgObj.content += (msgObj.content ? '\n\n' : '') + tip
        },
        onDone: (data) => {
          try {
            const parsed = JSON.parse(data)
            if (parsed.sessionId) sessionId.value = parsed.sessionId
          } catch {
            // done 载荷非 JSON 时忽略，不影响聊天内容
          }
        }
      })

      if (!messages.value[targetIndex]?.content) {
        appendToAssistant('抱歉，AI 服务暂时不可用。')
      }
    } catch (e) {
      // 超时/手动中止：保留已输出的内容，静默结束
      if (e.name !== 'AbortError') {
        console.error('AI error:', e)
        if (!messages.value[targetIndex]?.content) {
          appendToAssistant('抱歉，AI 服务暂时不可用。')
        }
      }
    } finally {
      clearTimeout(timer)
      isStreaming.value = false
    }
  }

  function clearMessages() {
    messages.value = []
    sessionId.value = 'session_' + Date.now()
  }

  return { messages, sessionId, isOpen, isStreaming, sendMessage, clearMessages }
})
