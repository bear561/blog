import http from './index'

export function getTags() {
  return http.get('/tags')
}
