import http from './index'

export function getArticles(params) {
  return http.get('/articles', { params })
}

export function getArticle(id) {
  return http.get(`/articles/${id}`)
}

export function getArchive() {
  return http.get('/articles/archive')
}

export function getHot() {
  return http.get('/articles/hot')
}

export function searchArticles(keyword) {
  return http.get('/articles/search', { params: { keyword } })
}
