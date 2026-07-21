<template>
  <div class="home-page">
    <div class="home-inner">
      <aside class="sidebar">
        <div class="author-card">
          <el-avatar :size="56" class="author-avatar">
            <el-icon :size="26"><UserFilled /></el-icon>
          </el-avatar>
          <h3 class="author-name">{{ siteName || 'Blog' }}</h3>
          <p class="author-desc" v-if="siteDesc">{{ siteDesc }}</p>
        </div>

        <div class="side-card">
          <h4><el-icon><Folder /></el-icon>分类</h4>
          <ul class="side-list">
            <li v-for="cat in categories" :key="cat.id">
              <router-link :to="`/category/${cat.slug}`">{{ cat.name }}<span class="count">{{ cat.articleCount }}</span></router-link>
            </li>
            <li v-if="categories.length===0" class="empty-tip">暂无分类</li>
          </ul>
        </div>

        <div class="side-card">
          <h4><el-icon><PriceTag /></el-icon>标签</h4>
          <div class="tags-wrap">
            <router-link v-for="tag in tags" :key="tag.id" :to="`/tag/${tag.slug}`">
              <el-tag size="small">{{ tag.name }}</el-tag>
            </router-link>
            <span v-if="tags.length===0" class="empty-tip">暂无标签</span>
          </div>
        </div>
      </aside>

      <main class="main-content" v-loading="loading">
        <div class="card-grid">
          <article v-for="article in articles" :key="article.id" class="card"
            @click="$router.push(`/article/${article.id}`)">
            <div class="card-cover">
              <img v-if="article.coverImage" :src="article.coverImage" alt="" loading="lazy" />
              <div v-else class="cover-placeholder"><el-icon :size="36"><Document /></el-icon></div>
            </div>
            <div class="card-body">
              <div class="card-meta">
                <span class="meta-cat" v-if="article.categoryName">{{ article.categoryName }}</span>
                <span class="meta-date">{{ formatDate(article.createdAt, 'YYYY-MM-DD') }}</span>
              </div>
              <h2 class="card-title">{{ article.title }}</h2>
              <p class="card-summary" v-if="article.summary">{{ article.summary }}</p>
              <div class="card-footer">
                <div v-if="article.tags?.length" class="footer-tags">
                  <span class="tag-dot" v-for="tag in article.tags.slice(0,4)" :key="tag.id">#{{ tag.name }}</span>
                </div>
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
import { ref, onMounted } from 'vue'
import { UserFilled, Folder, PriceTag, Document } from '@element-plus/icons-vue'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { getSiteConfig } from '@/api/siteConfig'
import { formatDate } from '@/utils/date'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const siteName = ref('')
const siteDesc = ref('')

onMounted(async () => {
  fetchArticles()
  getCategories().then(r => categories.value = r.data || []).catch(() => {})
  getTags().then(r => tags.value = r.data || []).catch(() => {})
  getSiteConfig().then(r => { const c = r.data || {}; siteName.value = c.site_name || ''; siteDesc.value = c.site_description || '' }).catch(() => {})
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
.home-inner { max-width: 1440px; margin: 0 auto; display: flex; gap: 24px; padding-top: 32px; padding-bottom: 60px; }

/* sidebar */
.sidebar { width: 220px; flex-shrink: 0; }
.author-card { text-align: center; padding: 22px 14px; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); margin-bottom: 12px; }
.author-avatar { background: linear-gradient(135deg, var(--primary), var(--accent)); color: #fff; margin-bottom: 8px; }
.author-name { font-size: 15px; font-weight: 700; color: var(--text); margin-bottom: 2px; }
.author-desc { font-size: 11px; color: var(--text-secondary); }
.side-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 12px 14px; margin-bottom: 10px; }
.side-card h4 { display: flex; align-items: center; gap: 5px; font-size: 12px; font-weight: 600; color: var(--text); margin-bottom: 8px; }
.side-card h4 .el-icon { color: var(--primary); }
.side-list { list-style: none; }
.side-list li { padding: 1px 0; }
.side-list li a { font-size: 12px; color: var(--text-secondary); display: flex; justify-content: space-between; }
.side-list li a:hover { color: var(--primary); }
.count { font-size: 11px; color: var(--text-muted); }
.tags-wrap { display: flex; flex-wrap: wrap; gap: 4px; }
.empty-tip { font-size: 11px; color: var(--text-muted); }

/* main */
.main-content { flex: 1; min-width: 0; }
.card-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }

.card { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; cursor: pointer; transition: transform .2s, box-shadow .2s; }
.card:hover { transform: translateY(-2px); box-shadow: var(--shadow-lg); }

.card-cover { height: 170px; overflow: hidden; background: var(--bg); }
.card-cover img { width: 100%; height: 100%; object-fit: cover; transition: transform .4s; }
.card:hover .card-cover img { transform: scale(1.04); }
.cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: var(--text-muted); }

.card-body { padding: 16px 18px; }
.card-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.meta-cat { font-size: 11px; font-weight: 600; color: var(--primary); background: rgba(59,130,246,.08); padding: 1px 8px; border-radius: 100px; }
.meta-date { font-size: 11px; color: var(--text-muted); }
.card-title { font-size: 16px; font-weight: 700; color: var(--text); line-height: 1.35; margin-bottom: 6px; }
.card:hover .card-title { color: var(--primary); }
.card-summary { font-size: 13px; color: var(--text-secondary); line-height: 1.6; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.footer-tags { display: flex; gap: 6px; }
.tag-dot { font-size: 11px; color: var(--text-muted); }
.footer-views { font-size: 11px; color: var(--text-muted); }
.pagination-wrap { margin-top: 28px; display: flex; justify-content: center; }
</style>
