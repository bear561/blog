<template>
  <div class="ai-chat-widget" :class="{ 'is-mobile': isMobile }">
    <!-- 浮动按钮: 安静的圆环 -->
    <transition name="fade-scale">
      <div v-if="!aiStore.isOpen && !isMobile" class="chat-float-btn" @click="toggleChat" title="问答助手">
        <el-icon :size="20"><ChatDotRound /></el-icon>
      </div>
    </transition>

    <!-- 聊天面板 -->
    <transition name="slide-up">
      <div v-if="aiStore.isOpen" class="chat-panel">
        <div class="chat-header">
          <span class="header-title">问答</span>
          <div class="header-actions">
            <el-button text :icon="Delete" size="small" @click="aiStore.clearMessages()" title="清空对话" />
            <el-button text :icon="Close" size="small" @click="toggleChat" />
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div v-if="aiStore.messages.length === 0" class="chat-welcome">
            <p class="welcome-text">有什么我可以帮你的？</p>
            <div class="quick-prompts">
              <span
                v-for="prompt in quickPrompts"
                :key="prompt"
                class="prompt-tag"
                @click="sendQuickPrompt(prompt)"
              >{{ prompt }}</span>
            </div>
          </div>
          <div
            v-for="msg in aiStore.messages"
            :key="msg.id"
            class="chat-message"
            :class="msg.role"
          >
            <div class="message-avatar" v-if="msg.role === 'assistant'">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="renderMarkdown(msg.content)"></div>
              <div v-if="msg.role === 'assistant' && msg.content === '' && aiStore.isStreaming" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
            <div class="message-avatar" v-if="msg.role === 'user'">
              <el-avatar :size="28" icon="UserFilled" />
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            resize="none"
            @keyup.enter.exact="handleSend"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            circle
            size="small"
            class="send-btn"
            :loading="aiStore.isStreaming"
            :disabled="!inputText.trim()"
            @click="handleSend"
          />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Close, Delete, Promotion } from '@element-plus/icons-vue'
import { useAiStore } from '@/stores/ai'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { useDeviceMode } from '@/composables/useDeviceMode'

const aiStore = useAiStore()
const { isMobile } = useDeviceMode()
const router = useRouter()
const inputText = ref('')
const messagesContainer = ref(null)
let clickHandler = null

const quickPrompts = [
  '这个博客是关于什么的？',
  '推荐几篇文章',
  '讲个冷笑话'
]

function onMessageClick(e) {
  const link = e.target.closest('a')
  if (!link) return
  const href = link.getAttribute('href')
  console.log('点击链接，href:', href)
  if (!href) return

  // 如果是带 target="_blank" 的链接，让浏览器默认打开新标签页
  if (link.getAttribute('target') === '_blank') {
    console.log('外链，允许浏览器默认行为')
    return
  }

  // 以站点根路径为基准解析 URL（不能用 window.location.href，
  // 否则在 /article/7 页面点击相对链接 "article/8" 会解析成 /article/article/8）
  let url
  try {
    url = new URL(href, window.location.origin)
    console.log('URL解析成功:', url.toString())
  } catch {
    // 如果无法解析为 URL（例如相对路径），尝试用 location 处理
    console.log('无法用 origin 解析，尝试用 location.href')
    try {
      url = new URL(href, window.location.href)
      console.log('用 location 解析成功:', url.toString())
    } catch {
      console.log('URL 解析失败，href:', href)
      return
    }
  }

  // 外链：让浏览器默认行为（新标签页打开）
  if (url.origin !== window.location.origin) {
    console.log('外链，origin 不同，允许浏览器默认行为')
    // 如果有 target="_blank"，已经返回了，这里让浏览器默认处理
    return
  }

  e.preventDefault()
  console.log('阻止默认行为，准备跳转')

  const targetPath = url.pathname + url.search + url.hash
  console.log('目标路径:', targetPath)
  console.log('当前设备:', isMobile.value ? '移动端' : '桌面端')
  console.log('面板状态:', aiStore.isOpen)

  // 移动端：先导航，再关闭面板
  if (isMobile.value) {
    console.log('移动端：先执行路由跳转')
    router.push(targetPath).then(() => {
      console.log('路由跳转成功，关闭面板')
      // 等待路由切换完成、页面渲染完成后再关闭面板
      setTimeout(() => {
        aiStore.isOpen = false
      }, 300)
    }).catch((err) => {
      console.error('路由跳转失败:', err)
      // 跳转失败时也关闭面板
      aiStore.isOpen = false
    })
  } else {
    console.log('桌面端：直接导航')
    router.push(targetPath).catch((err) => {
      console.error('桌面端路由跳转失败:', err)
    })
  }
}

function renderMarkdown(text) {
  if (!text) return ''
  try {
    // AI 输出不可信：渲染后统一经 DOMPurify 消毒再进 v-html
    return DOMPurify.sanitize(marked.parse(text, { breaks: true, gfm: true }))
  } catch {
    return DOMPurify.sanitize(text.replace(/</g, '&lt;'))
  }
}

function toggleChat() {
  aiStore.isOpen = !aiStore.isOpen
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || aiStore.isStreaming) return
  inputText.value = ''
  await aiStore.sendMessage(text)
  await scrollToBottom()
}

function sendQuickPrompt(prompt) {
  inputText.value = prompt
  handleSend()
}

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(
  () => aiStore.messages.length,
  () => { scrollToBottom() }
)

// 移动端打开聊天时锁定背景滚动（桌面浮窗不锁，避免回归）
watch(
  [isMobile, () => aiStore.isOpen],
  ([m, open]) => { document.body.style.overflow = (m && open) ? 'hidden' : '' }
)

// 添加全局点击监听器
onMounted(() => {
  clickHandler = (e) => {
    // 只在移动端且面板打开时监听链接点击
    if (isMobile.value && aiStore.isOpen) {
      const link = e.target.closest('a')
      if (!link) return

      const href = link.getAttribute('href')
      if (!href) return

      // 如果是带 target="_blank" 的链接，让浏览器默认打开新标签页
      if (link.getAttribute('target') === '_blank') {
        return
      }

      // 以站点根路径为基准解析 URL
      let url
      try {
        url = new URL(href, window.location.origin)
      } catch {
        try {
          url = new URL(href, window.location.href)
        } catch {
          return
        }
      }

      // 外链：让浏览器默认行为
      if (url.origin !== window.location.origin) {
        return
      }

      e.preventDefault()
      const targetPath = url.pathname + url.search + url.hash

      // 移动端：先导航，再关闭面板
      if (isMobile.value) {
        router.push(targetPath).then(() => {
          setTimeout(() => {
            aiStore.isOpen = false
          }, 300)
        }).catch((err) => {
          console.error('路由跳转失败:', err)
          aiStore.isOpen = false
        })
      } else {
        router.push(targetPath)
      }
    }
  }

  document.addEventListener('click', clickHandler, { passive: true })
})

onUnmounted(() => {
  if (clickHandler) {
    document.removeEventListener('click', clickHandler)
  }
})

// 路由切换时关闭移动端聊天面板
watch(
  () => router.currentRoute.value,
  () => {
    if (isMobile.value && aiStore.isOpen) {
      aiStore.isOpen = false
    }
  }
)

onUnmounted(() => {
  document.body.style.overflow = ''
  // 移除路由监听
  router.afterEach = null
})
</script>

<style scoped>
.ai-chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

/* 浮动按钮 */
.chat-float-btn {
  width: 48px; height: 48px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(196,79,46,.25);
  transition: transform .2s, box-shadow .2s;
}
.chat-float-btn:hover { transform: scale(1.06); box-shadow: 0 4px 18px rgba(196,79,46,.35); }

/* 聊天面板 */
.chat-panel {
  width: 370px; height: 540px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: 0 4px 24px rgba(0,0,0,.08);
  display: flex; flex-direction: column; overflow: hidden;
}

.chat-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px;
  background: var(--primary);
  color: #fff;
  flex-shrink: 0;
}
.header-title { font-size: 14px; font-weight: 600; }
.header-actions { display: flex; gap: 4px; }
.header-actions :deep(.el-button) { color: rgba(255,255,255,.8); }
.header-actions :deep(.el-button:hover) { color: #fff; }

/* 消息区 */
.chat-messages {
  flex: 1; overflow-y: auto; padding: 16px;
  display: flex; flex-direction: column; gap: 14px;
}

.chat-welcome { text-align: center; padding: 40px 20px; }
.welcome-text { font-size: 14px; color: var(--text-secondary); margin-bottom: 20px; }

.quick-prompts { display: flex; flex-direction: column; gap: 8px; align-items: center; }
.prompt-tag {
  font-size: 12px; color: var(--text-secondary);
  border: 1px solid var(--border); padding: 6px 14px; border-radius: 100px;
  cursor: pointer; transition: all .15s;
}
.prompt-tag:hover { color: var(--primary); border-color: var(--primary-light); background: #fdf0eb; }

/* 消息气泡 */
.chat-message { display: flex; gap: 8px; align-items: flex-start; }
.chat-message.user { flex-direction: row-reverse; }

.chat-message.assistant .message-content {
  background: var(--bg-warm); color: var(--text);
  border-radius: 4px 10px 10px 10px;
}
.chat-message.user .message-content {
  background: var(--accent); color: #fff;
  border-radius: 10px 4px 10px 10px;
}

.message-content { padding: 10px 14px; max-width: 250px; font-size: 13px; line-height: 1.6; word-break: break-word; }
.chat-message.assistant .message-content { max-width: 310px; padding: 14px 16px; }

/* ---- Markdown rendered content ---- */

/* 无样式列表 = 文章卡片 */
.message-content :deep(ul) { margin: 4px 0; padding: 0; list-style: none; }
.message-content :deep(ol) { margin: 4px 0; padding-left: 18px; }

/* 列表项 = 卡片分隔 */
.message-content :deep(li) {
  margin: 0 0 10px 0; padding: 0 0 10px 0;
  border-bottom: 1px solid var(--border);
}
.message-content :deep(li:last-child) { margin-bottom: 0; padding-bottom: 0; border-bottom: none; }

/* 文章标题（li 内的第一个 strong） */
.message-content :deep(li strong) {
  display: block; font-size: 14px; font-weight: 600; color: var(--text); margin-bottom: 3px;
}
.message-content :deep(p strong) { font-weight: 600; }

/* 小标题 */
.message-content :deep(h1), .message-content :deep(h2), .message-content :deep(h3),
.message-content :deep(h4), .message-content :deep(h5), .message-content :deep(h6) {
  margin: 6px 0 4px; font-size: 14px; font-weight: 600;
}

/* 日期/元信息 code */
.message-content :deep(code) {
  font-size: 11px; background: transparent; color: var(--text-muted); padding: 0;
}

/* 链接按钮 */
.message-content :deep(a) {
  color: var(--primary); text-decoration: none; font-size: 12px;
  display: inline-block; margin-top: 2px; padding: 2px 6px;
  border: 1px solid transparent; border-radius: 3px;
  transition: all 0.2s;
  cursor: pointer;
}
.message-content :deep(a:hover) {
  text-decoration: underline;
  background: var(--bg-warm);
  border-color: var(--border);
}

.message-content :deep(p) { margin: 3px 0; }
.message-content :deep(hr) { margin: 8px 0; border: none; border-top: 1px solid var(--border); }
.message-content :deep(pre) {
  font-size: 12px; background: rgba(0,0,0,.04); padding: 8px; border-radius: 4px;
  overflow-x: auto; margin: 4px 0;
}
.message-content :deep(blockquote) {
  margin: 4px 0; padding-left: 10px; border-left: 3px solid var(--primary-light);
  color: var(--text-secondary);
}

.message-avatar { flex-shrink: 0; color: var(--primary); }

/* 输入中 */
.typing-indicator { display: flex; gap: 4px; padding: 4px 0; }
.typing-indicator span {
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--text-muted);
  animation: typing 1.4s infinite ease-in-out;
}
.typing-indicator span:nth-child(2) { animation-delay: .2s; }
.typing-indicator span:nth-child(3) { animation-delay: .4s; }
@keyframes typing {
  0%, 60%, 100% { opacity: .3; transform: scale(.8); }
  30% { opacity: 1; transform: scale(1); }
}

/* 输入区 */
.chat-input {
  padding: 12px 16px; border-top: 1px solid var(--border);
  display: flex; gap: 8px; align-items: flex-end; flex-shrink: 0;
}
.chat-input :deep(.el-textarea__inner) { font-size: 13px; border-radius: 8px; }
.send-btn { flex-shrink: 0; }

/* 过渡 */
.fade-scale-enter-active, .fade-scale-leave-active { transition: all .3s ease; }
.fade-scale-enter-from, .fade-scale-leave-to { opacity: 0; transform: scale(.8); }
.slide-up-enter-active, .slide-up-leave-active { transition: all .3s ease; }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(20px); }

/* ===== 移动端：根节点静态、面板全屏抽屉 ===== */
/* 根节点 static → 关闭时无占位、无全屏透明层吞点击 */
.ai-chat-widget.is-mobile { position: static; }
.ai-chat-widget.is-mobile .chat-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  border-radius: 0;
  border: none;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  pointer-events: auto; /* 确保可以接收点击事件 */
}
.ai-chat-widget.is-mobile .chat-header { padding: 14px 16px; }
.ai-chat-widget.is-mobile .chat-messages { flex: 1; overflow-y: auto; overscroll-behavior: contain; }
.ai-chat-widget.is-mobile .message-content { max-width: 78%; }
.ai-chat-widget.is-mobile .chat-message.assistant .message-content { max-width: 86%; }
.ai-chat-widget.is-mobile .chat-input { padding-bottom: calc(12px + env(safe-area-inset-bottom)); }

/* 移动端面板过渡 */
.ai-chat-widget.is-mobile .chat-panel.slide-up-enter-active,
.ai-chat-widget.is-mobile .chat-panel.slide-up-leave-active {
  transition: transform 0.3s ease;
}
.ai-chat-widget.is-mobile .chat-panel.slide-up-enter-from {
  transform: translateY(100%);
}
.ai-chat-widget.is-mobile .chat-panel.slide-up-leave-to {
  transform: translateY(100%);
}
</style>
