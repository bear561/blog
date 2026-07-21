<template>
  <div class="search-page container">
    <div class="search-header">
      <div class="search-input-wrapper">
        <el-input v-model="keyword" size="large" placeholder="搜索文章..." :prefix-icon="Search" clearable @keyup.enter="handleSearch">
          <template #append>
            <el-button :icon="Search" @click="handleSearch" :loading="loading">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div class="search-info" v-if="searched">
        <p v-if="total > 0">找到 <strong>{{ total }}</strong> 条结果，关键词 "<strong>{{ searchedKeyword }}</strong>"</p>
        <p v-else>未找到 "<strong>{{ searchedKeyword }}</strong>" 相关文章</p>
      </div>
    </div>
    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="5" animated /></div>
    <div v-else-if="searched && articles.length > 0">
      <div class="articles-grid"><ArticleCard v-for="article in articles" :key="article.id" :article="article" /></div>
      <Pagination :current="page" :total="total" :page-size="pageSize" @page-change="handlePageChange" />
    </div>
    <el-empty v-else-if="searched && articles.length === 0" description="未找到相关文章" />
    <div v-else class="search-prompt">
      <el-icon :size="48" class="prompt-icon"><Search /></el-icon>
      <p>输入关键词搜索文章</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { searchArticles } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import Pagination from '@/components/Pagination.vue'

const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword || '')
const searchedKeyword = ref('')
const articles = ref([])
const loading = ref(false)
const searched = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  searchedKeyword.value = kw; searched.value = true; loading.value = true
  try { const res = await searchArticles(kw); articles.value = res.data.records || []; total.value = res.data.total || 0 } catch (e) {} finally { loading.value = false }
}
function handleSearch() { page.value = 1; router.replace({ name: 'Search', query: { keyword: keyword.value } }); doSearch() }
function handlePageChange(p) { page.value = p; doSearch(); window.scrollTo({ top: 0, behavior: 'smooth' }) }
onMounted(() => { if (keyword.value) doSearch() })
</script>

<style scoped>
.search-page { max-width: 900px; margin: 24px auto; padding: 0 20px; }
.search-header { margin-bottom: 32px; }
.search-input-wrapper { max-width: 600px; margin: 0 auto; }
.search-info { text-align: center; margin-top: 20px; font-size: 14px; color: #909399; }
.search-info strong { color: #409eff; }
.loading-wrap { padding: 20px 0; }
.articles-grid { display: flex; flex-direction: column; gap: 20px; }
.search-prompt { text-align: center; padding: 80px 0; }
.prompt-icon { color: #c0c4cc; margin-bottom: 16px; }
.search-prompt p { font-size: 15px; color: #909399; }
</style>
