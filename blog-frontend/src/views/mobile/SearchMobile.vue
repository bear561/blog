<template>
  <div class="m-page">
    <MobileAppBar back hide-search />
    <div class="m-searchbar">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input v-model="kw" type="text" placeholder="搜索文章…" @keyup.enter="submit" />
    </div>
    <MobileArticleList ref="listRef" :fetcher="fetcher">
      <template #empty><p>{{ kw ? '没有找到相关文章' : '输入关键词开始搜索' }}</p></template>
    </MobileArticleList>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchArticles } from '@/api/article'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import MobileArticleList from '@/components/mobile/MobileArticleList.vue'

const route = useRoute()
const router = useRouter()
const kw = ref(route.query.keyword || '')
const listRef = ref(null)

const fetcher = (p) => searchArticles(kw.value, { page: p, pageSize: 10 }).then(r => r.data)

function submit() {
  const q = kw.value.trim()
  if (!q) return
  router.replace({ name: 'Search', query: { keyword: q } })
}

watch(() => route.query.keyword, (v) => { kw.value = v || ''; listRef.value?.reload() })
</script>

<style scoped>
.m-searchbar {
  position: sticky; top: var(--header-height); z-index: 90;
  display: flex; align-items: center; gap: 8px;
  margin: 8px 14px; padding: 9px 14px;
  background: var(--bg-card); border: 1px solid var(--border); border-radius: 100px;
}
.m-searchbar svg { color: var(--text-muted); flex-shrink: 0; }
.m-searchbar input { flex: 1; border: none; outline: none; background: transparent; font-size: 14px; color: var(--text); font-family: var(--font-sans); }
</style>
