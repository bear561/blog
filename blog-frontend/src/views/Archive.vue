<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title"><el-icon :size="24"><Clock /></el-icon>Archive</h1>
      <p class="page-subtitle">{{ totalArticles }} 篇文章</p>
    </div>
    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="8" animated /></div>
    <div v-else-if="archiveList.length === 0"><el-empty description="暂无文章" /></div>
    <div v-else class="timeline">
      <div v-for="group in archiveList" :key="group.year" class="timeline-year">
        <div class="year-header">
          <span class="year-label">{{ group.year }}</span>
          <span class="year-count">{{ group.articles?.length || 0 }} 篇</span>
        </div>
        <div class="month-list">
          <div v-for="article in group.articles" :key="article.id" class="timeline-item">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <span class="timeline-date">{{ formatDate(article.createdAt, 'MM-DD') }}</span>
              <router-link :to="`/article/${article.id}`" class="timeline-title">{{ article.title }}</router-link>
              <span class="timeline-category" v-if="article.categoryName">
                <el-tag size="small" effect="plain">{{ article.categoryName }}</el-tag>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import { getArchive } from '@/api/article'
import { formatDate } from '@/utils/date'

const archiveList = ref([])
const loading = ref(false)
const totalArticles = computed(() => archiveList.value.reduce((sum, g) => sum + (g.articles?.length || 0), 0))

onMounted(async () => {
  loading.value = true
  try { const res = await getArchive(); archiveList.value = res.data || [] } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.page-header { text-align: center; padding: 40px 0 32px; }
.page-title { font-size: 28px; font-weight: 700; color: #303133; display: flex; align-items: center; justify-content: center; gap: 12px; }
.page-subtitle { margin-top: 8px; font-size: 14px; color: #909399; }
.loading-wrap { padding: 20px 0; }
.timeline { max-width: 700px; margin: 0 auto; position: relative; padding-left: 24px; }
.timeline::before { content: ''; position: absolute; left: 8px; top: 0; bottom: 0; width: 2px; background: #ebeef5; }
.timeline-year { margin-bottom: 32px; }
.year-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.year-label { font-size: 22px; font-weight: 700; color: #303133; }
.year-count { font-size: 12px; color: #909399; }
.month-list { display: flex; flex-direction: column; gap: 8px; }
.timeline-item { display: flex; align-items: center; gap: 16px; position: relative; }
.timeline-dot { width: 10px; height: 10px; border-radius: 50%; background: #409eff; border: 2px solid #fff; box-shadow: 0 0 0 2px #409eff; flex-shrink: 0; margin-left: -29px; z-index: 1; }
.timeline-content { display: flex; align-items: center; gap: 12px; padding: 10px 18px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,.06); flex: 1; transition: box-shadow .2s; }
.timeline-content:hover { box-shadow: 0 4px 16px rgba(0,0,0,.08); }
.timeline-date { font-size: 13px; color: #909399; font-family: monospace; flex-shrink: 0; }
.timeline-title { font-size: 15px; color: #303133; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.timeline-title:hover { color: #409eff; }
.timeline-category { flex-shrink: 0; }
</style>
