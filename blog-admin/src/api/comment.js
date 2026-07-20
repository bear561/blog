import request from './index'

export function getComments(params) {
  return request.get('/admin/comments', { params })
}

export function approveComment(id) {
  return request.put(`/admin/comments/${id}/approve`)
}

export function deleteComment(id) {
  return request.delete(`/admin/comments/${id}`)
}

export function replyComment(id, data) {
  return request.post(`/admin/comments/${id}/reply`, data)
}
