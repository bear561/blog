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

export function searchArticles(keyword, extra = {}) {
  // extra 供移动端分页使用；桌面调用点不传，请求与之前完全一致（向后兼容）
  return http.get('/articles/search', { params: { keyword, ...extra } })
}
