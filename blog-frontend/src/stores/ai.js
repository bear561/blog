import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAiStore = defineStore('ai', () => {
  const messages = ref([])
  const sessionId = ref('session_' + Date.now())
  const isOpen = ref(false)
  const isStreaming = ref(false)

  function addMessage(role, content) {
    messages.value.push({ id: Date.now(), role, content, timestamp: new Date().toISOString() })
  }

  async function sendMessage(content) {
    if (!content.trim() || isStreaming.value) return

    addMessage('user', content)
    isStreaming.value = true

    const assistantMsg = { id: Date.now() + 1, role: 'assistant', content: '', timestamp: new Date().toISOString() }
    messages.value.push(assistantMsg)
    const lastIdx = messages.value.length - 1

    try {
      const history = messages.value
        .filter(m => m.content && m.role !== 'assistant')
        .slice(0, -1)
        .map(m => ({ role: m.role, content: m.content }))

      const resp = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: content, sessionId: sessionId.value, history }),
        signal: AbortSignal.timeout(60000)
      })

      if (!resp.ok) throw new Error('HTTP ' + resp.status)

      const text = await resp.text()
      const lines = text.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const val = line.slice(5).trim()
          if (val && val !== '[DONE]' && !val.startsWith('{')) {
            messages.value[lastIdx].content += val
          }
        }
      }
    } catch (e) {
      console.error('AI error:', e)
      if (!messages.value[lastIdx].content) {
        messages.value[lastIdx].content = '抱歉，AI 服务暂时不可用。'
      }
    } finally {
      isStreaming.value = false
    }
  }

  function clearMessages() {
    messages.value = []
    sessionId.value = 'session_' + Date.now()
  }

  return { messages, sessionId, isOpen, isStreaming, sendMessage, clearMessages }
})
