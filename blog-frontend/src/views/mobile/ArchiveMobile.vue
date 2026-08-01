<template>
  <div class="m-page">
    <MobileAppBar />
    <MobilePageHeader title="归档" :subtitle="total ? `${total} 篇文章` : ''" />
    <div class="m-timeline" v-if="!loading">
      <div class="m-tl__group" v-for="g in groups" :key="g.year">
        <div class="m-tl__year">{{ g.year }}</div>
        <router-link class="m-tl__row" v-for="a in g.articles" :key="a.id" :to="`/article/${a.id}`">
          <span class="m-tl__date">{{ formatDate(a.createdAt, 'MM-DD') }}</span>
          <span class="m-tl__title">{{ a.title }}</span>
        </router-link>
      </div>
      <div v-if="!groups.length" class="m-empty"><p>暂无归档</p></div>
    </div>
    <div v-else class="m-load"><el-skeleton :rows="8" animated /></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArchive } from '@/api/article'
import { formatDate } from '@/utils/date'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import MobilePageHeader from '@/components/mobile/MobilePageHeader.vue'

const groups = ref([])
const loading = ref(true)
const total = computed(() => groups.value.reduce((n, g) => n + (g.articles ? g.articles.length : 0), 0))

onMounted(() => {
  getArchive()
    .then(r => { groups.value = r.data || [] })
    .catch(() => {})
    .finally(() => { loading.value = false })
})
</script>

<style scoped>
.m-timeline { padding: 0 16px 24px; }
.m-tl__year { font-family: var(--font-serif); font-size: 20px; font-weight: 700; color: var(--primary); margin: 18px 0 6px; }
.m-tl__row { display: flex; gap: 12px; align-items: baseline; padding: 10px 0; border-bottom: 1px solid var(--border); text-decoration: none; }
.m-tl__row:active { opacity: .6; }
.m-tl__date { font-size: 12px; color: var(--text-muted); font-variant-numeric: tabular-nums; flex-shrink: 0; width: 42px; }
.m-tl__title { font-size: 15px; color: var(--text); line-height: 1.4; }
.m-empty { text-align: center; color: var(--text-muted); font-size: 13px; padding: 40px 0; }
.m-load { padding: 8px 16px; }
</style>
