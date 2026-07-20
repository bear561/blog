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
    if (res.code && res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || 'Request failed')
      return Promise.reject(new Error(res.message || 'Request failed'))
    }
    return res
  },
  (error) => {
    const message = error.response?.data?.message || error.message || 'Network error'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http
