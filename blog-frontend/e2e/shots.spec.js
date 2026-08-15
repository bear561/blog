import { test } from '@playwright/test'

const URL = '/article/11'

test('截图: 桌面顶部 + 目录栏 + 表格特写', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 800 })
  await page.goto(URL)
  await page.waitForSelector('.article-detail')
  await page.waitForTimeout(1000)

  // 桌面首屏（viewport，非 fullpage）
  await page.screenshot({ path: 'test-results/shot-desktop-top.jpg', type: 'jpeg', quality: 85 })

  // 目录侧栏特写
  const toc = page.locator('.article-toc')
  if (await toc.count()) {
    await toc.screenshot({ path: 'test-results/shot-toc.jpg', type: 'jpeg', quality: 85 })
  }

  // 第一个表格特写（滚到它可见再截）
  const tbl = page.locator('.article-content table').first()
  if (await tbl.count()) {
    await tbl.scrollIntoViewIfNeeded()
    await page.waitForTimeout(400)
    await tbl.screenshot({ path: 'test-results/shot-table.jpg', type: 'jpeg', quality: 85 })
  }
})

test('截图: 移动端抽屉', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 667 })
  await page.goto(URL)
  await page.waitForSelector('.m-toc-fab')
  await page.click('.m-toc-fab')
  await page.waitForSelector('.el-drawer')
  await page.waitForTimeout(700)
  await page.locator('.el-drawer').screenshot({ path: 'test-results/shot-mobile-drawer.jpg', type: 'jpeg', quality: 85 })
})