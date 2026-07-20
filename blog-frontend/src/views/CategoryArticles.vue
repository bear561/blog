<template>
  <el-container>
    <SiteHeader />
    <el-main>
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">
            <el-icon :size="24"><Folder /></el-icon>
            Category: {{ slug }}
          </h1>
          <p class="page-subtitle">{{ total }} article(s) found</p>
        </div>

        <div class="articles-grid" v-if="!loading && articles.length > 0">
          <ArticleCard
            v-for="article in articles"
            :key="article.id"
            :article="article"
          />
        </div>

        <el-empty v-if="!loading && articles.length === 0" description="No articles in this category" />

        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="5" animated />
        </div>

        <Pagination
          :current="page"
          :total="total"
          :page-size="pageSize"
          @page-change="handlePageChange"
        />
      </div>
    </el-main>
    <SiteFooter />
    <AIChatWidget />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Folder } from '@element-plus/icons-vue'
import { getArticles } from '@/api/article'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import Pagination from '@/components/Pagination.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'

const route = useRoute()
const slug = computed(() => route.params.slug)
const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticles({
      page: page.value,
      pageSize: pageSize.value,
      categorySlug: slug.value
    })
    articles.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

function handlePageChange(p) {
  page.value = p
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(fetchArticles)
</script>

<style scoped>
.page-header {
  text-align: center;
  padding: 40px 0 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-subtitle {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.loading-wrap {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .articles-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 22px;
  }
}
</style>
