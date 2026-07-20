import http from './index'

export function getComments(articleId) {
  return http.get(`/comments/${articleId}`)
}

export function postComment(data) {
  return http.post('/comments', data)
}
