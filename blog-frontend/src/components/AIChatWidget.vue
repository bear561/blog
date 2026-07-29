<template>
  <div class="ai-chat-widget">
    <!-- 浮动按钮: 安静的圆环 -->
    <transition name="fade-scale">
      <div v-if="!aiStore.isOpen" class="chat-float-btn" @click="toggleChat" title="问答助手">
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
            <div class="message-content" @click="onMessageClick">
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
import { ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Close, Delete, Promotion } from '@element-plus/icons-vue'
import { useAiStore } from '@/stores/ai'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const aiStore = useAiStore()
const router = useRouter()
const inputText = ref('')
const messagesContainer = ref(null)

const quickPrompts = [
  '这个博客是关于什么的？',
  '推荐几篇文章',
  '讲个冷笑话'
]

function onMessageClick(e) {
  const link = e.target.closest('a')
  if (!link) return
  const href = link.getAttribute('href')
  if (!href) return

  // 以当前页面为基准解析：根路径 /article/7、相对路径 article/7、
  // 协议相对 //host/...、绝对 URL 都能正确判断是否同域
  let url
  try {
    url = new URL(href, window.location.href)
  } catch {
    return
  }
  // 只拦截同域链接走 SPA 路由——整页刷新会销毁 Pinia store，导致聊天上下文丢失；
  // 真正的外链交给浏览器默认行为
  if (url.origin !== window.location.origin) return
  e.preventDefault()
  router.push(url.pathname + url.search + url.hash)
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
  display: inline-block; margin-top: 2px;
}
.message-content :deep(a:hover) { text-decoration: underline; }

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
</style>
