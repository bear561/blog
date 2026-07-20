import request from './index'

export function getArticles(params) {
  return request.get('/admin/articles', { params })
}

export function getArticle(id) {
  return request.get(`/admin/articles/${id}`)
}

export function createArticle(data) {
  return request.post('/admin/articles', data)
}

export function updateArticle(id, data) {
  return request.put(`/admin/articles/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/admin/articles/${id}`)
}

export function publishArticle(id) {
  return request.put(`/admin/articles/${id}/publish`)
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
