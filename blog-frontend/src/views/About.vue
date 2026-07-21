<template>
  <div class="about-layout container">
    <aside class="about-sidebar">
      <div class="author-card">
        <el-avatar :size="80" class="author-avatar">
          <el-icon :size="40"><UserFilled /></el-icon>
        </el-avatar>
        <h2 class="author-name">{{ config.site_name || '博主' }}</h2>
        <p class="author-bio">{{ config.site_description || '' }}</p>
      </div>
      <nav class="about-nav">
        <a href="#about-content" class="nav-item active">关于本站</a>
        <router-link to="/archive" class="nav-item">文章归档</router-link>
        <router-link to="/friend-links" class="nav-item">友情链接</router-link>
      </nav>
    </aside>

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
import { marked } from 'marked'
import ArticleContent from '@/components/ArticleContent.vue'

const aboutHtml = ref('')
const loading = ref(false)
const config = reactive({ site_name: '', site_description: '' })

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSiteConfig()
    const c = res.data || {}
    Object.assign(config, c)
    aboutHtml.value = marked(c.about_content || '')
  } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.about-layout { display: flex; gap: 40px; margin-top: 40px; padding-bottom: 60px; }
.about-sidebar { width: 240px; flex-shrink: 0; }
.about-main { flex: 1; min-width: 0; }

.author-card { text-align: center; padding: 32px 20px; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); margin-bottom: 16px; }
.author-avatar { background: linear-gradient(135deg, var(--primary), var(--accent)); color: #fff; margin-bottom: 16px; }
.author-name { font-size: 18px; font-weight: 700; color: var(--text); margin-bottom: 8px; }
.author-bio { font-size: 13px; color: var(--text-secondary); line-height: 1.6; }

.about-nav { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; }
.nav-item { display: block; padding: 12px 20px; font-size: 14px; color: var(--text-secondary); border-left: 3px solid transparent; transition: all .15s; }
.nav-item:hover, .nav-item.active { color: var(--primary); background: rgba(59,130,246,.05); border-left-color: var(--primary); }

.about-main { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); padding: 40px 48px; min-height: 400px; }
</style>
