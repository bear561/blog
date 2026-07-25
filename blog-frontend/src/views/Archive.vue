<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">归档</h1>
      <p class="page-subtitle">{{ totalArticles }} 篇文章</p>
    </div>
    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="8" animated /></div>
    <div v-else-if="archiveList.length === 0"><el-empty description="暂无文章" /></div>
    <div v-else class="timeline">
      <div v-for="group in archiveList" :key="group.year" class="timeline-year">
        <!-- 年份标记 -->
        <div class="year-marker">
          <span class="year-dot"></span>
          <h2 class="year-label">{{ group.year }}</h2>
          <span class="year-count">{{ group.articles?.length || 0 }} 篇</span>
        </div>

        <!-- 文章列表 -->
        <div class="year-articles">
          <div v-for="article in group.articles" :key="article.id" class="timeline-item">
            <div class="timeline-line">
              <span class="line-dot"></span>
            </div>
            <router-link :to="`/article/${article.id}`" class="timeline-card">
              <span class="card-date">{{ formatDate(article.createdAt, 'MM-DD') }}</span>
              <span class="card-title">{{ article.title }}</span>
              <span class="card-meta-right">
                <span class="card-cat" v-if="article.categoryName">{{ article.categoryName }}</span>
                <span class="card-read">约 {{ readMin(article) }} 分钟</span>
              </span>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArchive } from '@/api/article'
import { formatDate } from '@/utils/date'
import { readingTime } from '@/utils/readingTime'

const archiveList = ref([])
const loading = ref(false)
const totalArticles = computed(() => archiveList.value.reduce((sum, g) => sum + (g.articles?.length || 0), 0))

function readMin(article) { return readingTime(article.contentHtml || article.summary || '') }

onMounted(async () => {
  loading.value = true
  try { const res = await getArchive(); archiveList.value = res.data || [] } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.page-header { text-align: center; padding: 40px 0 32px; }
.page-title { font-size: 24px; font-weight: 700; color: var(--text); }
.page-subtitle { margin-top: 8px; font-size: 14px; color: var(--text-muted); }

.timeline { max-width: 680px; margin: 0 auto; padding-bottom: 60px; position: relative; }

/* 年份 */
.timeline-year { margin-bottom: 40px; }
.year-marker {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 16px; padding-left: 6px;
}
.year-dot {
  width: 12px; height: 12px; border-radius: 50%;
  background: var(--primary); flex-shrink: 0;
}
.year-label {
  font-family: var(--font-mono);
  font-size: 26px; font-weight: 500; color: var(--text);
}
.year-count { font-size: 13px; color: var(--text-muted); font-family: var(--font-sans); }

/* 文章 */
.year-articles { position: relative; padding-left: 24px; }
/* 竖线 */
.year-articles::before {
  content: ''; position: absolute; left: 5px; top: 8px; bottom: 8px;
  width: 1.5px; background: var(--border);
}

.timeline-item { display: flex; align-items: center; position: relative; }
.timeline-line { position: absolute; left: -19px; top: 50%; transform: translateY(-50%); }
.line-dot {
  display: block; width: 8px; height: 8px; border-radius: 50%;
  background: var(--bg-card); border: 2px solid var(--primary);
}

.timeline-card {
  display: flex; align-items: center; gap: 16px; width: 100%;
  padding: 12px 18px; margin: 3px 0;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius);
  transition: border-color .2s, transform .15s;
  color: inherit; text-decoration: none;
}
.timeline-card:hover {
  border-color: var(--primary-light);
  transform: translateX(4px);
}

.card-date {
  font-family: var(--font-mono); font-size: 13px;
  color: var(--primary); flex-shrink: 0; width: 42px;
}
.card-title {
  font-size: 14px; color: var(--text); flex: 1;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.card-meta-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.card-cat {
  font-size: 11px; color: var(--text-muted);
  border: 1px solid var(--border); padding: 1px 8px; border-radius: 100px;
}
.card-read { font-size: 11px; color: var(--text-muted); }
</style>
