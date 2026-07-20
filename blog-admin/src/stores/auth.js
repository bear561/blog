import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')

  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    nickname.value = res.data.nickname
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('nickname', res.data.nickname)
  }

  function logout() {
    request.post('/auth/logout').catch(() => {})
    token.value = ''
    nickname.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
  }

  return { token, nickname, login, logout }
})
