import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 并发请求同时 401 时，避免重复跳转和刷屏提示
let handlingUnauthorized = false

function handleUnauthorized(message) {
  localStorage.removeItem('token')
  if (handlingUnauthorized) return
  handlingUnauthorized = true
  ElMessage.error(message || '登录已过期，请重新登录')
  const done = () => {
    handlingUnauthorized = false
  }
  if (router.currentRoute.value.path !== '/admin/login') {
    router.push('/admin/login').finally(done)
  } else {
    done()
  }
}

request.interceptors.response.use(
  response => {
    const res = response.data
    // 后端鉴权失败时返回 HTTP 200，真正的状态码在 body.code 里
    if (res.code === 401 || res.code === 403) {
      handleUnauthorized(res.message)
      return Promise.reject(new Error(res.message || 'Unauthorized'))
    }
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      handleUnauthorized(error.response?.data?.message)
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
