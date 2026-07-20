import http from './index'

export function getSiteConfig() {
  return http.get('/site-config')
}
