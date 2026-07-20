<template>
  <el-container>
    <SiteHeader />
    <el-main>
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">
            <el-icon :size="24"><Clock /></el-icon>
            Archive
          </h1>
          <p class="page-subtitle">{{ totalArticles }} article(s) in total</p>
        </div>

        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="8" animated />
        </div>

        <div v-else-if="archiveList.length === 0">
          <el-empty description="No articles yet" />
        </div>

        <div v-else class="timeline">
          <div
            v-for="group in archiveList"
            :key="group.year"
            class="timeline-year"
          >
            <div class="year-header">
              <span class="year-label">{{ group.year }}</span>
              <span class="year-count">{{ group.articles?.length || 0 }} articles</span>
            </div>
            <div class="month-list">
              <div
                v-for="article in group.articles"
                :key="article.id"
                class="timeline-item"
              >
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <span class="timeline-date">
                    {{ formatDate(article.createdAt || article.createTime, 'MM-DD') }}
                  </span>
                  <router-link
                    :to="`/article/${article.id}`"
                    class="timeline-title"
                  >
                    {{ article.title }}
                  </router-link>
                  <span class="timeline-category" v-if="article.category">
                    <el-tag size="small" effect="plain">
                      {{ article.category.name || article.category }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-main>
    <SiteFooter />
    <AIChatWidget />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import { getArchive } from '@/api/article'
import { formatDate } from '@/utils/date'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'

const archiveList = ref([])
const loading = ref(false)

const totalArticles = computed(() => {
  return archiveList.value.reduce((sum, group) => {
    return sum + (group.articles?.length || 0)
  }, 0)
})

async function fetchArchive() {
  loading.value = true
  try {
    const res = await getArchive()
    const data = res.data || res
    if (Array.isArray(data)) {
      archiveList.value = data
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

onMounted(fetchArchive)
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

.loading-wrap {
  padding: 20px 0;
}

.timeline {
  max-width: 700px;
  margin: 0 auto;
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--border-light);
}

.timeline-year {
  margin-bottom: 32px;
}

.year-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.year-label {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.year-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.month-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--primary-color);
  border: 2px solid var(--bg-white);
  box-shadow: 0 0 0 2px var(--primary-color);
  flex-shrink: 0;
  margin-left: -29px;
  z-index: 1;
}

.timeline-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 18px;
  background: var(--bg-white);
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-light);
  flex: 1;
  transition: box-shadow 0.2s;
}

.timeline-content:hover {
  box-shadow: var(--shadow-card);
}

.timeline-date {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: monospace;
  flex-shrink: 0;
}

.timeline-title {
  font-size: 15px;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.timeline-title:hover {
  color: var(--primary-color);
}

.timeline-category {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }

  .timeline-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    padding: 12px 14px;
  }
}
</style>
