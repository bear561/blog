/**
 * 估算中文文章阅读时间
 * 中文阅读速度约 300-400 字/分钟，取 350
 * 英文约 200-250 words/min，混合文本保守取中位
 */
export function readingTime(html) {
  if (!html) return 1

  // 去除 HTML 标签
  const text = html.replace(/<[^>]+>/g, '').replace(/\s+/g, '')
  const charCount = text.length

  // 纯中文按 350 字/分钟，保留整数，最少 1 分钟
  const minutes = Math.max(1, Math.round(charCount / 350))
  return minutes
}
