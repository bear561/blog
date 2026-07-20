import request from './index'

export function getFriendLinks() {
  return request.get('/admin/friend-links')
}

export function createFriendLink(data) {
  return request.post('/admin/friend-links', data)
}

export function updateFriendLink(id, data) {
  return request.put(`/admin/friend-links/${id}`, data)
}

export function deleteFriendLink(id) {
  return request.delete(`/admin/friend-links/${id}`)
}
