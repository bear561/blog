<template>
  <div class="ai-chat-widget">
    <!-- Floating button -->
    <transition name="fade-scale">
      <div
        v-if="!aiStore.isOpen"
        class="chat-float-btn"
        @click="toggleChat"
      >
        <el-icon :size="24"><ChatDotRound /></el-icon>
      </div>
    </transition>

    <!-- Chat panel -->
    <transition name="slide-up">
      <div v-if="aiStore.isOpen" class="chat-panel">
        <div class="chat-header">
          <div class="header-title">
            <el-icon :size="18"><ChatDotRound /></el-icon>
            <span>AI Assistant</span>
          </div>
          <div class="header-actions">
            <el-button
              text
              :icon="Delete"
              size="small"
              @click="aiStore.clearMessages()"
              title="Clear chat"
            />
            <el-button
              text
              :icon="Close"
              size="small"
              @click="toggleChat"
            />
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div v-if="aiStore.messages.length === 0" class="chat-welcome">
            <div class="welcome-icon">
              <el-icon :size="40"><ChatDotRound /></el-icon>
            </div>
            <p>Hello! I'm your AI assistant. How can I help you today?</p>
            <div class="quick-prompts">
              <el-tag
                v-for="prompt in quickPrompts"
                :key="prompt"
                class="prompt-tag"
                @click="sendQuickPrompt(prompt)"
              >
                {{ prompt }}
              </el-tag>
            </div>
          </div>
          <div
            v-for="msg in aiStore.messages"
            :key="msg.id"
            class="chat-message"
            :class="msg.role"
          >
            <div class="message-avatar" v-if="msg.role === 'assistant'">
              <el-icon :size="24"><ChatDotRound /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-text" v-text="msg.content"></div>
              <div v-if="msg.role === 'assistant' && msg.content === '' && aiStore.isStreaming" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
            <div class="message-avatar" v-if="msg.role === 'user'">
              <el-avatar :size="32" icon="UserFilled" />
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="Type your message..."
            resize="none"
            @keyup.enter.exact="handleSend"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            circle
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
import { ChatDotRound, Close, Delete, Promotion } from '@element-plus/icons-vue'
import { useAiStore } from '@/stores/ai'

const aiStore = useAiStore()
const inputText = ref('')
const messagesContainer = ref(null)

const quickPrompts = [
  'What is this blog about?',
  'Recommend some articles',
  'Tell me a tech joke'
]

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
  () => {
    scrollToBottom()
  }
)
</script>

<style scoped>
.ai-chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

/* Floating button */
.chat-float-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;
}

.chat-float-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
}

/* Chat panel */
.chat-panel {
  width: 380px;
  height: 560px;
  background: var(--bg-white);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, var(--primary-color), #764ba2);
  color: #fff;
  flex-shrink: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.header-actions .el-button {
  color: #fff;
}

/* Messages area */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-welcome {
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  color: var(--primary-color);
  margin-bottom: 16px;
}

.chat-welcome p {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.quick-prompts {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.prompt-tag {
  cursor: pointer;
  max-width: 100%;
}

.prompt-tag:hover {
  background: rgba(64, 158, 255, 0.1);
}

/* Message */
.chat-message {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.chat-message.assistant .message-content {
  background: var(--bg-color);
  border-radius: 4px 12px 12px 12px;
}

.chat-message.user .message-content {
  background: linear-gradient(135deg, var(--primary-color), #764ba2);
  color: #fff;
  border-radius: 12px 4px 12px 12px;
}

.message-content {
  padding: 10px 14px;
  max-width: 260px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.message-avatar {
  flex-shrink: 0;
  color: var(--primary-color);
}

/* Typing indicator */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-placeholder);
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

/* Input area */
.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--border-light);
  display: flex;
  gap: 8px;
  align-items: flex-end;
  flex-shrink: 0;
}

.chat-input :deep(.el-textarea__inner) {
  font-size: 13px;
  border-radius: 8px;
}

.send-btn {
  flex-shrink: 0;
}

/* Transitions */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.3s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: 768px) {
  .chat-panel {
    width: calc(100vw - 32px);
    height: 480px;
    right: 0;
  }
}
</style>
