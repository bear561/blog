const BASE_URL = '/api'

export function chat(data) {
  const token = localStorage.getItem('token')
  return fetch(`${BASE_URL}/ai/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data)
  })
}
