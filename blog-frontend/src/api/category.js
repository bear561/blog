import http from './index'

export function getCategories() {
  return http.get('/categories')
}
