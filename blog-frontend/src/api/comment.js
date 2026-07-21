import http from './index'

export function getComments(articleId) {
  return http.get(`/comments?articleId=${articleId}`)
}

export function postComment(data) {
  return http.post('/comments', data)
}
