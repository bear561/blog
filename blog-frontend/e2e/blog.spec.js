import { test, expect } from '@playwright/test'

const ARTICLE_URL = '/article/11'

test.describe('博客文章页冒烟测试', () => {
  test('桌面端: 目录侧栏 + 表格 + 正文渲染', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 })
    await page.goto(ARTICLE_URL)
    await page.waitForSelector('.article-detail', { timeout: 15000 })
    await page.waitForTimeout(1000)

    await expect(page.locator('.article-content')).toBeVisible()

    const tableCount = await page.locator('.article-content table').count()
    console.log('  ✅ 表格数量:', tableCount)

    const tocVisible = await page.locator('.article-toc').isVisible().catch(() => false)
    console.log('  ✅ 目录侧栏可见:', tocVisible)

    if (tocVisible) {
      const tocItems = await page.locator('.article-toc .toc-item').count()
      console.log('  ✅ 目录项数量:', tocItems)
      await page.locator('.article-toc .toc-item a').first().click()
      await page.waitForTimeout(800)
    }

    await page.screenshot({ path: 'test-results/desktop-article.png', fullPage: true })
    expect(tableCount).toBeGreaterThan(0)
    expect(tocVisible).toBeTruthy()
  })

  test('移动端: 悬浮目录按钮 + 抽屉', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto(ARTICLE_URL)
    await page.waitForSelector('.m-art__body', { timeout: 15000 })
    await page.waitForTimeout(800)

    const fab = page.locator('.m-toc-fab')
    await expect(fab).toBeVisible()
    console.log('  ✅ 悬浮目录按钮可见')
    await page.screenshot({ path: 'test-results/mobile-fab.png' })

    await fab.click()
    await page.waitForSelector('.el-drawer', { timeout: 5000 })
    await page.waitForTimeout(600)
    await page.screenshot({ path: 'test-results/mobile-drawer.png' })

    await page.locator('.m-toc-link').first().click()
    await page.waitForTimeout(800)
    console.log('  ✅ 目录抽屉点击跳转完成')
    await page.screenshot({ path: 'test-results/mobile-after-jump.png' })
  })

  test('评论: 提交根评论', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 })
    await page.goto(ARTICLE_URL)
    await page.waitForSelector('.comment-form', { timeout: 15000 })

    const nick = 'pw测试' + Date.now().toString().slice(-5)
    await page.fill('input[placeholder*="昵称"]', nick)
    await page.fill('input[placeholder*="邮箱"]', `${nick}@test.com`)
    await page.fill('.comment-form textarea', '这是 Playwright 自动提交的测试评论 ' + Date.now())
    await page.click('.comment-form button[type="submit"]')
    await page.waitForTimeout(2500)

    const hasComment = await page.locator('.comment-item').count()
    console.log('  ✅ 提交后评论条数:', hasComment)
    await page.screenshot({ path: 'test-results/comment-submitted.png' })
    expect(hasComment).toBeGreaterThan(0)
  })

  test('评论: 匿名提交立即可见', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 })
    await page.goto(ARTICLE_URL)
    await page.waitForSelector('.comment-form', { timeout: 15000 })

    // 勾选匿名,昵称/邮箱行应折叠
    await page.locator('.comment-form input[type="checkbox"]').check()
    await expect(page.locator('input[placeholder*="昵称"]')).not.toBeVisible()

    const anonContent = '匿名测试 ' + Date.now()
    await page.fill('.comment-form textarea', anonContent)
    await page.click('.comment-form button[type="submit"]')
    await page.waitForTimeout(2500)

    // 全部自动审核,应立即可见
    const visible = await page.locator(`.comment-item:has-text("${anonContent}")`).count()
    console.log('  ✅ 匿名评论可见数:', visible)
    expect(visible).toBeGreaterThan(0)

    // 昵称应形如 Anonymous#xxxx
    const nick = await page
      .locator(`.comment-item:has-text("${anonContent}") .comment-nickname`)
      .first()
      .textContent()
    expect(nick).toMatch(/Anonymous#[0-9a-f]{4}/)
    await page.screenshot({ path: 'test-results/comment-anonymous.png' })
  })
})