<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title"><el-icon :size="24"><PriceTag /></el-icon>标签: {{ slug }}</h1>
      <p class="page-subtitle">共 {{ total }} 篇文章</p>
    </div>
    <div class="articles-grid" v-if="!loading && articles.length > 0">
      <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
    </div>
    <el-empty v-if="!loading && articles.length === 0" description="该标签下暂无文章" />
    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="5" animated /></div>
    <Pagination :current="page" :total="total" :page-size="pageSize" @page-change="handlePageChange" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { PriceTag } from '@element-plus/icons-vue'
import { getArticles } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import Pagination from '@/components/Pagination.vue'

const route = useRoute()
const slug = computed(() => route.params.slug)
const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(async () => {
  loading.value = true
  try { const res = await getArticles({ page: page.value, pageSize: pageSize.value, tagSlug: slug.value }); articles.value = res.data.records || []; total.value = res.data.total || 0 } catch (e) {} finally { loading.value = false }
})

function handlePageChange(p) { page.value = p; onMounted; window.scrollTo({ top: 0, behavior: 'smooth' }) }
</script>

<style scoped>
.page-header { text-align: center; padding: 40px 0 32px; }
.page-title { font-size: 28px; font-weight: 700; color: #303133; display: flex; align-items: center; justify-content: center; gap: 12px; }
.page-subtitle { margin-top: 8px; font-size: 14px; color: #909399; }
.articles-grid { display: flex; flex-direction: column; gap: 20px; max-width: 800px; margin: 0 auto; }
.loading-wrap { padding: 20px 0; }
</style>
