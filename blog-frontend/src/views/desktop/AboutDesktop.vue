<template>
  <div class="about-layout container">
    <aside class="about-sidebar">
      <!-- 作者卡片 -->
      <div class="author-card">
        <el-avatar :size="64" class="author-avatar" :src="config.site_avatar">
          <el-icon :size="30"><UserFilled /></el-icon>
        </el-avatar>
        <h2 class="author-name">{{ config.site_name || '博主' }}</h2>
        <p class="author-bio" v-if="config.site_description">{{ config.site_description }}</p>
      </div>

      <!-- 时钟 -->
      <div class="side-widget clock-widget">
        <AnalogClock />
      </div>

      <!-- 博客统计 -->
      <div class="side-widget stats-widget">
        <div class="stat-row">
          <span class="stat-icon">✍️</span>
          <span class="stat-label">文章</span>
          <span class="stat-value">{{ articleTotal }} 篇</span>
        </div>
        <div class="stat-row">
          <span class="stat-icon">📁</span>
          <span class="stat-label">分类</span>
          <span class="stat-value">{{ categoryTotal }} 个</span>
        </div>
        <div class="stat-row">
          <span class="stat-icon">🏷️</span>
          <span class="stat-label">标签</span>
          <span class="stat-value">{{ tagTotal }} 个</span>
        </div>
      </div>

      <!-- 导航 -->
      <nav class="side-widget nav-widget">
        <a href="#about-content" class="nav-item active">关于本站</a>
        <router-link to="/archive" class="nav-item">文章归档</router-link>
        <router-link to="/friend-links" class="nav-item">友情链接</router-link>
      </nav>

      <!-- 社交 -->
      <div class="side-widget social-widget">
        <a v-if="social.github" :href="social.github" target="_blank" class="social-link" title="GitHub">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>
        </a>
        <a v-if="social.gitee" :href="social.gitee" target="_blank" class="social-link" title="Gitee">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="11" fill="none" stroke="currentColor" stroke-width="2"/><text x="12" y="17" text-anchor="middle" font-size="13" font-weight="700" fill="currentColor">G</text></svg>
        </a>
        <a v-if="social.csdn" :href="social.csdn" target="_blank" class="social-link" title="CSDN">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><rect x="3" y="3" width="18" height="18" rx="4" fill="none" stroke="currentColor" stroke-width="2"/><text x="12" y="17" text-anchor="middle" font-size="12" font-weight="700" fill="currentColor">C</text></svg>
        </a>
        <a v-if="social.juejin" :href="social.juejin" target="_blank" class="social-link" title="稀土掘金">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><polygon points="12,2 22,12 12,22 2,12" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><text x="12" y="16" text-anchor="middle" font-size="10" font-weight="700" fill="currentColor">掘</text></svg>
        </a>
        <span v-if="!social.github && !social.gitee && !social.csdn && !social.juejin" class="no-social">暂未添加社交链接</span>
      </div>
    </aside>

    <!-- 主内容 -->
    <main class="about-main" id="about-content" v-loading="loading">
      <div v-if="aboutHtml" class="markdown-body">
        <ArticleContent :html="aboutHtml" />
      </div>
      <el-empty v-else-if="!loading" description="暂无内容" />
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { getSiteConfig } from '@/api/siteConfig'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { marked } from 'marked'
import ArticleContent from '@/components/ArticleContent.vue'
import AnalogClock from '@/components/AnalogClock.vue'

const aboutHtml = ref('')
const loading = ref(false)
const articleTotal = ref(0)
const categoryTotal = ref(0)
const tagTotal = ref(0)
const config = reactive({ site_name: '', site_description: '' })

// 社交链接 — 改成你自己的链接
const social = reactive({
  github: 'https://github.com',
  gitee: 'https://gitee.com',
  csdn: 'https://blog.csdn.net',
  juejin: 'https://juejin.cn'
})

onMounted(async () => {
  loading.value = true
  try {
    const [configRes, articlesRes, categoriesRes, tagsRes] = await Promise.all([
      getSiteConfig(),
      getArticles({ page: 1, pageSize: 1 }),
      getCategories(),
      getTags()
    ])
    const c = configRes.data || {}
    Object.assign(config, c)
    aboutHtml.value = marked(c.about_content || '')
    articleTotal.value = articlesRes.data?.total || 0
    categoryTotal.value = (categoriesRes.data || []).length
    tagTotal.value = (tagsRes.data || []).length

    // 从站点配置读取社交链接（如果后端有配置的话）
    if (c.social_github) social.github = c.social_github
    if (c.social_gitee) social.gitee = c.social_gitee
    if (c.social_csdn) social.csdn = c.social_csdn
    if (c.social_juejin) social.juejin = c.social_juejin
  } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.about-layout { display: flex; gap: 36px; margin-top: 36px; padding-bottom: 60px; }
.about-sidebar { width: 210px; flex-shrink: 0; display: flex; flex-direction: column; gap: 10px; }
.about-main { flex: 1; min-width: 0; }

/* 通用小组件卡片 */
.side-widget {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); padding: 14px 16px;
}

/* 作者卡片 */
.author-card { text-align: center; padding: 24px 16px; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); }
.author-avatar { background: var(--primary); color: #fff; margin-bottom: 10px; }
.author-name { font-size: 16px; font-weight: 700; color: var(--text); margin-bottom: 4px; }
.author-bio { font-size: 12px; color: var(--text-muted); line-height: 1.5; }

/* 时钟 */
.clock-widget { display: flex; justify-content: center; padding: 10px; }

/* 统计 */
.stats-widget { display: flex; flex-direction: column; gap: 8px; }
.stat-row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.stat-icon { flex-shrink: 0; font-size: 14px; }
.stat-label { color: var(--text-muted); flex: 1; }
.stat-value { color: var(--text); font-weight: 600; }

/* 导航 */
.nav-widget { padding: 0; overflow: hidden; }
.nav-item { display: block; padding: 10px 16px; font-size: 13px; color: var(--text-secondary); border-left: 3px solid transparent; transition: all .15s; }
.nav-item:hover, .nav-item.active { color: var(--primary); background: rgba(196,79,46,.04); border-left-color: var(--primary); }

/* 社交 */
.social-widget { display: flex; justify-content: center; gap: 14px; padding: 16px; }
.social-link { color: var(--text-muted); transition: color .2s; display: flex; align-items: center; }
.social-link:hover { color: var(--primary); }
.no-social { font-size: 12px; color: var(--text-muted); }

/* 主内容 */
.about-main { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); padding: 40px 48px; min-height: 400px; }
</style>
