import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000
})

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

http.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端鉴权失败时返回 HTTP 200，真正的状态码在 body.code 里
    if (res.code === 401 || res.code === 403) {
      localStorage.removeItem('token')
      ElMessage.error(res.message || '登录状态已失效，请重新登录')
      return Promise.reject(new Error(res.message || 'Unauthorized'))
    }
    if (res.code && res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || 'Request failed')
      return Promise.reject(new Error(res.message || 'Request failed'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      localStorage.removeItem('token')
      ElMessage.error(error.response?.data?.message || '登录状态已失效，请重新登录')
    } else {
      const message = error.response?.data?.message || error.message || 'Network error'
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default http
