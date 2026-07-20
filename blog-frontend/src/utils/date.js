/**
 * Format a date string into a human-readable format.
 * @param {string|Date} date - The date to format
 * @param {string} format - The format pattern (default: 'YYYY-MM-DD HH:mm')
 * @returns {string} Formatted date string
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm') {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const pad = (n) => String(n).padStart(2, '0')

  const tokens = {
    YYYY: d.getFullYear(),
    MM: pad(d.getMonth() + 1),
    DD: pad(d.getDate()),
    HH: pad(d.getHours()),
    mm: pad(d.getMinutes()),
    ss: pad(d.getSeconds()),
    M: d.getMonth() + 1,
    D: d.getDate()
  }

  let result = format
  for (const [token, value] of Object.entries(tokens)) {
    result = result.replace(token, value)
  }

  return result
}

/**
 * Get relative time description from a date.
 * @param {string|Date} date - The date to compare
 * @returns {string} Relative time (e.g., "3 days ago")
 */
export function timeAgo(date) {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const now = Date.now()
  const diff = now - d.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return 'just now'
  if (minutes < 60) return `${minutes} minute${minutes > 1 ? 's' : ''} ago`
  if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''} ago`
  if (days < 30) return `${days} day${days > 1 ? 's' : ''} ago`
  if (months < 12) return `${months} month${months > 1 ? 's' : ''} ago`
  return `${years} year${years > 1 ? 's' : ''} ago`
}
