<template>
  <div class="home-page">
    <div class="home-inner">
      <!-- 侧边栏: 类似杂志目录 -->
      <aside class="sidebar">
        <div class="author-card">
          <el-avatar :size="48" class="author-avatar" :src="siteAvatar">
            <el-icon :size="22"><UserFilled /></el-icon>
          </el-avatar>
          <div class="author-text">
            <h3 class="author-name">{{ siteName || 'Blog' }}</h3>
            <p class="author-desc" v-if="siteDesc">{{ siteDesc }}</p>
          </div>
        </div>

        <div class="clock-wrap">
          <AnalogClock />
        </div>

        <div class="side-card">
          <h4>分类</h4>
          <ul class="side-list">
            <li v-for="cat in categories" :key="cat.id">
              <router-link :to="`/category/${cat.slug}`">
                {{ cat.name }}<span class="count">{{ cat.articleCount }}</span>
              </router-link>
            </li>
            <li v-if="categories.length===0" class="empty-tip">暂无分类</li>
          </ul>
        </div>

        <div class="side-card">
          <h4>标签</h4>
          <div class="tags-wrap">
            <router-link v-for="tag in tags" :key="tag.id" :to="`/tag/${tag.slug}`" class="tag-link">
              {{ tag.name }}
            </router-link>
            <span v-if="tags.length===0" class="empty-tip">暂无标签</span>
          </div>
        </div>

        <DailyQuote />
      </aside>

      <!-- 主内容: 杂志排版 -->
      <main class="main-content" v-loading="loading">
        <!-- Hero 首篇 大图横跨 -->
        <article
          v-if="heroArticle"
          class="card-hero"
          @click="$router.push(`/article/${heroArticle.id}`)"
        >
          <div class="hero-cover">
            <img v-if="heroArticle.coverImage" :src="heroArticle.coverImage" alt="" loading="lazy" />
            <div v-else class="cover-placeholder"><el-icon :size="40"><Document /></el-icon></div>
          </div>
          <div class="hero-body">
            <div class="card-meta">
              <span class="meta-cat" v-if="heroArticle.categoryName">{{ heroArticle.categoryName }}</span>
              <span class="meta-date">{{ formatDate(heroArticle.createdAt, 'YYYY-MM-DD') }}</span>
              <span class="meta-read">约 {{ heroReadMin }} 分钟</span>
            </div>
            <h2 class="hero-title">{{ heroArticle.title }}</h2>
            <p class="hero-summary" v-if="heroArticle.summary">{{ heroArticle.summary }}</p>
          </div>
        </article>

        <!-- 后续文章: 两栏错落 -->
        <div class="card-grid" v-if="gridArticles.length > 0">
          <article
            v-for="(article, idx) in gridArticles"
            :key="article.id"
            class="card"
            @click="$router.push(`/article/${article.id}`)"
          >
            <div class="card-cover">
              <img v-if="article.coverImage" :src="article.coverImage" alt="" loading="lazy" />
              <div v-else class="cover-placeholder-sm"><el-icon :size="24"><Document /></el-icon></div>
            </div>
            <div class="card-body">
              <div class="card-meta">
                <span class="meta-cat" v-if="article.categoryName">{{ article.categoryName }}</span>
                <span class="meta-date">{{ formatDate(article.createdAt, 'YYYY-MM-DD') }}</span>
                <span class="meta-read">约 {{ readMin(article) }} 分钟</span>
              </div>
              <h2 class="card-title">{{ article.title }}</h2>
              <p class="card-summary" v-if="article.summary">{{ article.summary }}</p>
              <div class="card-footer">
                <span class="footer-views">{{ article.viewCount || 0 }} 阅读</span>
              </div>
            </div>
          </article>
        </div>

        <el-empty v-if="!loading && articles.length===0" description="暂无文章" />
        <div v-if="total > pageSize" class="pagination-wrap">
          <el-pagination background :current-page="page" :page-size="pageSize" :total="total"
            layout="prev, pager, next" @current-change="handlePageChange" />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserFilled, Document } from '@element-plus/icons-vue'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { getSiteConfig } from '@/api/siteConfig'
import { formatDate } from '@/utils/date'
import { readingTime } from '@/utils/readingTime'
import AnalogClock from '@/components/AnalogClock.vue'
import DailyQuote from '@/components/DailyQuote.vue'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const siteName = ref('')
const siteDesc = ref('')
const siteAvatar = ref('')

const heroArticle = computed(() => articles.value.length > 0 ? articles.value[0] : null)
const heroReadMin = computed(() => {
  const a = heroArticle.value
  return a ? readingTime(a.contentHtml || a.summary || '') : 0
})
const gridArticles = computed(() => articles.value.slice(1))

function readMin(article) { return readingTime(article.contentHtml || article.summary || '') }

onMounted(async () => {
  fetchArticles()
  getCategories().then(r => categories.value = r.data || []).catch(() => {})
  getTags().then(r => tags.value = r.data || []).catch(() => {})
  getSiteConfig().then(r => { const c = r.data || {}; siteName.value = c.site_name || ''; siteDesc.value = c.site_description || ''; siteAvatar.value = c.site_avatar || '' }).catch(() => {})
})

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticles({ page: page.value, pageSize: pageSize.value })
    articles.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {} finally { loading.value = false }
}

function handlePageChange(p) { page.value = p; fetchArticles(); window.scrollTo({ top: 0, behavior: 'smooth' }) }
</script>

<style scoped>
.home-page { padding: 0 32px; }
.home-inner { max-width: var(--max-width); margin: 0 auto; display: flex; gap: 28px; padding-top: 32px; padding-bottom: 60px; }

/* 侧边栏 */
.sidebar { width: 200px; flex-shrink: 0; }
.author-card {
  display: flex; align-items: center; gap: 10px;
  padding: 16px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); margin-bottom: 12px;
}
.author-avatar { background: var(--primary); color: #fff; flex-shrink: 0; }
.author-text { min-width: 0; }
.author-name { font-size: 14px; font-weight: 600; color: var(--text); line-height: 1.3; }
.author-desc { font-size: 11px; color: var(--text-muted); margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.clock-wrap { display: flex; justify-content: center; padding: 8px 0 14px; }
.side-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); padding: 14px 16px; margin-bottom: 10px; }
.side-card h4 { font-size: 11px; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: .5px; margin-bottom: 10px; }
.side-list { list-style: none; }
.side-list li { padding: 2px 0; }
.side-list li a { font-size: 13px; color: var(--text-secondary); display: flex; justify-content: space-between; }
.side-list li a:hover { color: var(--primary); }
.count { font-size: 11px; color: var(--text-muted); }
.tags-wrap { display: flex; flex-wrap: wrap; gap: 4px; }
.tag-link { font-size: 12px; color: var(--text-secondary); padding: 2px 6px; border-radius: 3px; background: var(--bg-warm); transition: all .15s; }
.tag-link:hover { color: var(--primary); background: #fdf0eb; }
.empty-tip { font-size: 11px; color: var(--text-muted); }

/* 主内容 */
.main-content { flex: 1; min-width: 0; }

/* Hero 首篇 */
.card-hero {
  margin-bottom: 28px; cursor: pointer;
  border: 1px solid var(--border);
  overflow: hidden; background: var(--bg-card);
  transition: border-color .2s;
}
.card-hero:hover { border-color: var(--primary-light); }
.card-hero:hover .hero-title { color: var(--primary); }
.hero-cover {
  height: 300px; overflow: hidden; background: var(--bg-warm);
  margin-bottom: 16px;
}
.hero-cover img { width: 100%; height: 100%; object-fit: cover; filter: sepia(0.06); transition: transform .5s; }
.card-hero:hover .hero-cover img { transform: scale(1.02); }
.cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: var(--text-muted); }
.hero-body { padding: 16px 18px 18px; }
.hero-title { font-size: 24px; font-weight: 700; color: var(--text); line-height: 1.35; margin-bottom: 8px; transition: color .2s; }
.hero-summary { font-size: 14px; color: var(--text-secondary); line-height: 1.7; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* 两栏错落网格 */
.card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 24px;
  align-items: start;
}

.card {
  cursor: pointer;
  border: 1px solid var(--border);
  overflow: hidden; background: var(--bg-card);
  transition: border-color .2s;
  display: flex; flex-direction: column;
}
.card:hover { border-color: var(--primary-light); }
/* 右列卡片下沉，制造错落感 */
.card:nth-child(even) { margin-top: 36px; }
.card:hover .card-title { color: var(--primary); }
.card-cover {
  height: 180px; overflow: hidden; background: var(--bg-warm);
  margin-bottom: 14px;
}
.card-cover img { width: 100%; height: 100%; object-fit: cover; filter: sepia(0.06); transition: transform .4s; }
.card:hover .card-cover img { transform: scale(1.03); }
.cover-placeholder-sm { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: var(--text-muted); }

.card-body { padding: 0 16px 16px; flex: 1; display: flex; flex-direction: column; }
.card-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.meta-cat {
  font-size: 11px; font-weight: 600; color: var(--primary);
  border: 1px solid rgba(196,79,46,.25); padding: 1px 8px; border-radius: 100px;
}
.meta-date { font-size: 11px; color: var(--text-muted); }
.meta-read { font-size: 10px; color: var(--text-muted); }
.card-title { font-size: 17px; font-weight: 700; color: var(--text); line-height: 1.4; margin-bottom: 6px; transition: color .2s; }
.card-summary { font-size: 13px; color: var(--text-secondary); line-height: 1.65; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 8px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.footer-views { font-size: 11px; color: var(--text-muted); }
.pagination-wrap { margin-top: 32px; display: flex; justify-content: center; }
</style>
