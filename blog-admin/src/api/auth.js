import request from './index'

export function login(data) {
  return request.post('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getAdminInfo() {
  return request.get('/auth/info')
}
