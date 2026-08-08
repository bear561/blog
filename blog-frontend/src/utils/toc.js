/**
 * 从文章 HTML 中提取目录标题(h2/h3)。
 * 返回顺序与 DOM 中 document.querySelectorAll('h2, h3') 一致，供索引对应跳转。
 * @param {string} html - 文章正文 HTML
 * @returns {Array<{text: string, level: number}>}
 */
export function extractHeadings(html) {
  if (!html) return []
  const doc = new DOMParser().parseFromString(html, 'text/html')
  const list = []
  doc.querySelectorAll('h2, h3').forEach((el) => {
    const text = el.textContent.trim()
    if (!text) return
    list.push({ text, level: el.tagName.toLowerCase() === 'h2' ? 2 : 3 })
  })
  return list
}