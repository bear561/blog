import http from './index'

export function getFriendLinks() {
  return http.get('/friend-links')
}
