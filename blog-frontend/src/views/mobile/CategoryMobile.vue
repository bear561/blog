<template>
  <div class="m-page">
    <MobileAppBar back />
    <MobilePageHeader title="分类" :subtitle="name" />
    <MobileArticleList ref="listRef" :fetcher="fetcher">
      <template #empty><p>该分类下暂无文章</p></template>
    </MobileArticleList>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import MobilePageHeader from '@/components/mobile/MobilePageHeader.vue'
import MobileArticleList from '@/components/mobile/MobileArticleList.vue'

const route = useRoute()
const listRef = ref(null)
const name = ref(route.params.slug)

// 稳定引用：内部读取 route.params.slug 的实时值
const fetcher = (p) => getArticles({ page: p, pageSize: 10, categorySlug: route.params.slug }).then(r => r.data)

function resolveName(slug) {
  getCategories().then(r => {
    const c = (r.data || []).find(x => x.slug === slug)
    if (c) name.value = c.name
  }).catch(() => {})
}

watch(() => route.params.slug, (s) => { name.value = s; resolveName(s); listRef.value?.reload() })
onMounted(() => resolveName(route.params.slug))
</script>
