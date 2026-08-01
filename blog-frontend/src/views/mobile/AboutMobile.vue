<template>
  <div class="m-page">
    <MobileAppBar />
    <div class="m-about" v-if="!loading">
      <div class="m-about__hero">
        <div class="m-about__av">{{ (config.site_name || 'B').trim().charAt(0) }}</div>
        <h1 class="m-about__name">{{ config.site_name || 'Blog' }}</h1>
        <p class="m-about__desc" v-if="config.site_description">{{ config.site_description }}</p>
      </div>

      <div class="m-about__stats">
        <div class="m-stat"><b>{{ articleTotal }}</b><span>文章</span></div>
        <div class="m-stat"><b>{{ categoryTotal }}</b><span>分类</span></div>
      </div>

      <div class="m-reader m-about__body" v-if="aboutHtml"><ArticleContent :html="aboutHtml" /></div>

      <div class="m-about__social" v-if="socials.length">
        <a v-for="s in socials" :key="s.label" :href="s.url" target="_blank" rel="noopener noreferrer" class="m-soc">{{ s.label }}</a>
      </div>
    </div>
    <div v-else class="m-load"><el-skeleton :rows="8" animated /></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { marked } from 'marked'
import { getSiteConfig } from '@/api/siteConfig'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import ArticleContent from '@/components/ArticleContent.vue'

const config = ref({})
const aboutHtml = ref('')
const articleTotal = ref(0)
const categoryTotal = ref(0)
const loading = ref(true)

const socials = computed(() => {
  const c = config.value
  return [['github', 'GitHub'], ['gitee', 'Gitee'], ['csdn', 'CSDN'], ['juejin', '掘金']]
    .map(([k, label]) => ({ label, url: c['social_' + k] }))
    .filter(s => s.url)
})

onMounted(async () => {
  try {
    const [c, a, cat] = await Promise.all([
      getSiteConfig(),
      getArticles({ page: 1, pageSize: 1 }),
      getCategories()
    ])
    config.value = c.data || {}
    aboutHtml.value = marked(config.value.about_content || '')
    articleTotal.value = a.data?.total || 0
    categoryTotal.value = (cat.data || []).length
  } catch (e) { /* ignore */ } finally { loading.value = false }
})
</script>

<style scoped>
.m-about { padding: 8px 16px 32px; }
.m-about__hero { text-align: center; padding: 18px 0 14px; }
.m-about__av {
  width: 64px; height: 64px; border-radius: 50%; margin: 0 auto 12px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff; font-family: var(--font-serif); font-size: 26px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.m-about__name { font-family: var(--font-serif); font-size: 22px; font-weight: 700; color: var(--text); }
.m-about__desc { margin-top: 6px; font-size: 13px; color: var(--text-secondary); line-height: 1.6; }
.m-about__stats { display: flex; gap: 12px; margin: 14px 0 18px; }
.m-stat { flex: 1; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); padding: 12px; text-align: center; }
.m-stat b { display: block; font-family: var(--font-serif); font-size: 22px; color: var(--primary); }
.m-stat span { font-size: 11px; color: var(--text-muted); }
.m-reader :deep(.article-content) { font-size: 15.5px; line-height: 1.85; }
.m-about__social { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 20px; justify-content: center; }
.m-soc { font-size: 13px; color: var(--text-secondary); border: 1px solid var(--border); padding: 6px 14px; border-radius: 100px; text-decoration: none; }
.m-soc:active { background: var(--bg-warm); color: var(--primary); }
.m-load { padding: 8px 16px; }
</style>
