import { defineStore } from 'pinia'
import { ref } from 'vue'
import { chat as chatApi } from '@/api/ai'

export const useAiStore = defineStore('ai', () => {
  const messages = ref([])
  const sessionId = ref(generateSessionId())
  const isOpen = ref(false)
  const isStreaming = ref(false)

  function generateSessionId() {
    return 'session_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
  }

  function addMessage(role, content) {
    messages.value.push({
      id: Date.now(),
      role,
      content,
      timestamp: new Date().toISOString()
    })
  }

  async function sendMessage(content) {
    if (!content.trim() || isStreaming.value) return

    addMessage('user', content)
    isStreaming.value = true

    const assistantMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '',
      timestamp: new Date().toISOString()
    }
    messages.value.push(assistantMessage)

    try {
      const response = await chatApi({
        messages: messages.value.filter(m => m.content).map(m => ({
          role: m.role,
          content: m.content
        })),
        sessionId: sessionId.value
      })

      if (!response.ok) {
        throw new Error('Chat request failed')
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6).trim()
            if (data === '[DONE]') continue
            try {
              const parsed = JSON.parse(data)
              if (parsed.content) {
                assistantMessage.content += parsed.content
              }
            } catch {
              assistantMessage.content += data
            }
          }
        }
      }
    } catch (e) {
      console.error('AI chat error:', e)
      assistantMessage.content = 'Sorry, something went wrong. Please try again.'
    } finally {
      isStreaming.value = false
    }
  }

  function clearMessages() {
    messages.value = []
    sessionId.value = generateSessionId()
  }

  return {
    messages,
    sessionId,
    isOpen,
    isStreaming,
    sendMessage,
    clearMessages
  }
})
