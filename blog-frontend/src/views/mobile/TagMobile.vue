<template>
  <div class="m-page">
    <MobileAppBar back />
    <MobilePageHeader title="标签" :subtitle="name" />
    <MobileArticleList ref="listRef" :fetcher="fetcher">
      <template #empty><p>该标签下暂无文章</p></template>
    </MobileArticleList>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '@/api/article'
import { getTags } from '@/api/tag'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import MobilePageHeader from '@/components/mobile/MobilePageHeader.vue'
import MobileArticleList from '@/components/mobile/MobileArticleList.vue'

const route = useRoute()
const listRef = ref(null)
const name = ref(route.params.slug)

const fetcher = (p) => getArticles({ page: p, pageSize: 10, tagSlug: route.params.slug }).then(r => r.data)

function resolveName(slug) {
  getTags().then(r => {
    const t = (r.data || []).find(x => x.slug === slug)
    if (t) name.value = t.name
  }).catch(() => {})
}

watch(() => route.params.slug, (s) => { name.value = s; resolveName(s); listRef.value?.reload() })
onMounted(() => resolveName(route.params.slug))
</script>
