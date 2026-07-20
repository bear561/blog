<template>
  <el-container>
    <SiteHeader />
    <el-main>
      <div class="home-layout container">
        <div class="main-content">
          <div class="articles-grid">
            <ArticleCard
              v-for="article in articles"
              :key="article.id"
              :article="article"
            />
          </div>
          <el-empty v-if="!loading && articles.length === 0" description="No articles yet" />
          <Pagination
            :current="page"
            :total="total"
            :page-size="pageSize"
            @page-change="handlePageChange"
          />
        </div>
        <aside class="sidebar">
          <!-- Categories -->
          <el-card class="sidebar-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Folder /></el-icon>
                <span>Categories</span>
              </div>
            </template>
            <div class="category-list">
              <router-link
                v-for="cat in categories"
                :key="cat.id"
                :to="`/category/${cat.slug}`"
                class="category-item"
              >
                <span class="cat-name">{{ cat.name }}</span>
                <el-tag size="small" round>{{ cat.articleCount || 0 }}</el-tag>
              </router-link>
            </div>
          </el-card>

          <!-- Hot Articles -->
          <el-card class="sidebar-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><TrendCharts /></el-icon>
                <span>Hot Articles</span>
              </div>
            </template>
            <div class="hot-list">
              <router-link
                v-for="(item, index) in hotArticles"
                :key="item.id"
                :to="`/article/${item.id}`"
                class="hot-item"
              >
                <span class="hot-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</span>
                <span class="hot-title">{{ item.title }}</span>
              </router-link>
            </div>
          </el-card>

          <!-- Tags -->
          <el-card class="sidebar-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><PriceTag /></el-icon>
                <span>Tags</span>
              </div>
            </template>
            <div class="tags-cloud">
              <router-link
                v-for="tag in tags"
                :key="tag.id"
                :to="`/tag/${tag.slug}`"
              >
                <el-tag class="tag-cloud-item" size="default">
                  {{ tag.name }}
                </el-tag>
              </router-link>
            </div>
          </el-card>
        </aside>
      </div>
    </el-main>
    <SiteFooter />
    <AIChatWidget />
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Folder, TrendCharts, PriceTag } from '@element-plus/icons-vue'
import { getArticles, getHot } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import Pagination from '@/components/Pagination.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'

const articles = ref([])
const hotArticles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticles({ page: page.value, pageSize: pageSize.value })
    articles.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function fetchHotArticles() {
  try {
    const res = await getHot()
    hotArticles.value = res.data || []
  } catch (e) {}
}

async function fetchCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {}
}

async function fetchTags() {
  try {
    const res = await getTags()
    tags.value = res.data || []
  } catch (e) {}
}

function handlePageChange(p) {
  page.value = p
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  fetchArticles()
  fetchHotArticles()
  fetchCategories()
  fetchTags()
})
</script>

<style scoped>
.home-layout {
  display: flex;
  gap: 24px;
  margin-top: 24px;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  border-radius: var(--radius-base);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-header .el-icon {
  color: var(--primary-color);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: var(--radius-small);
  color: var(--text-regular);
  font-size: 14px;
  transition: background 0.2s;
}

.category-item:hover {
  background: rgba(64, 158, 255, 0.06);
  color: var(--primary-color);
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: var(--radius-small);
  color: var(--text-regular);
  font-size: 13px;
  transition: background 0.2s;
}

.hot-item:hover {
  background: rgba(64, 158, 255, 0.06);
  color: var(--primary-color);
}

.hot-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: var(--bg-color);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.hot-rank.rank-1 {
  background: linear-gradient(135deg, #f56c6c, #e64242);
  color: #fff;
}

.hot-rank.rank-2 {
  background: linear-gradient(135deg, #e6a23c, #d48806);
  color: #fff;
}

.hot-rank.rank-3 {
  background: linear-gradient(135deg, #409eff, #3a8ee6);
  color: #fff;
}

.hot-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-cloud-item {
  cursor: pointer;
}

@media (max-width: 1024px) {
  .home-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .articles-grid {
    grid-template-columns: 1fr;
  }
}
</style>
