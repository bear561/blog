import { test } from '@playwright/test'

// 程序化抽取布局几何信息，用于验证：
// 1. 桌面两栏是否重叠、目录是否在右侧 sticky
// 2. 表格在桌面/移动端是否横向溢出（列压缩自适应是否生效）
// 3. 移动端悬浮目录按钮是否在右下角
test('桌面布局信息', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 800 })
  await page.goto('/article/11')
  await page.waitForSelector('.article-content')
  await page.waitForTimeout(800)

  const info = await page.evaluate(() => {
    const box = (el) => {
      if (!el) return null
      const b = el.getBoundingClientRect()
      return { x: Math.round(b.x), y: Math.round(b.y), w: Math.round(b.width), h: Math.round(b.height) }
    }
    const tables = [...document.querySelectorAll('.article-content table')].map((t) => ({
      w: t.clientWidth, scrollW: t.scrollWidth, overflowX: t.clientWidth < t.scrollWidth,
    }))
    const aside = document.querySelector('.article-toc-aside')
    return {
      innerW: window.innerWidth,
      docScrollW: document.body.scrollWidth,
      main: box(document.querySelector('.article-main')),
      toc: box(aside),
      tocPosition: aside ? getComputedStyle(aside).position : null,
      tables,
    }
  })
  console.log('DESKTOP:' + JSON.stringify(info))
})

test('移动端布局信息', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 667 })
  await page.goto('/article/11')
  await page.waitForSelector('.m-toc-fab')
  await page.waitForTimeout(800)

  const info = await page.evaluate(() => {
    const box = (el) => {
      const b = el.getBoundingClientRect()
      return { x: Math.round(b.x), y: Math.round(b.y), w: Math.round(b.width), h: Math.round(b.height) }
    }
    const tables = [...document.querySelectorAll('.article-content table')].map((t) => ({
      w: t.clientWidth, scrollW: t.scrollWidth, overflowX: t.clientWidth < t.scrollWidth,
    }))
    return {
      innerW: window.innerWidth,
      docScrollW: document.body.scrollWidth,
      fab: box(document.querySelector('.m-toc-fab')),
      tables,
    }
  })
  console.log('MOBILE:' + JSON.stringify(info))
})