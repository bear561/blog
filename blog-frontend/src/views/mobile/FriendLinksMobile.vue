<template>
  <div class="m-page">
    <MobileAppBar />
    <MobilePageHeader title="友链" subtitle="常来常往，互相成就" />
    <div class="m-links" v-if="!loading">
      <MobileLinkCard v-for="l in links" :key="l.id" :link="l" />
      <div v-if="!links.length" class="m-empty"><p>暂无友链</p></div>
    </div>
    <div v-else class="m-load"><el-skeleton :rows="6" animated /></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFriendLinks } from '@/api/friendLink'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import MobilePageHeader from '@/components/mobile/MobilePageHeader.vue'
import MobileLinkCard from '@/components/mobile/MobileLinkCard.vue'

const links = ref([])
const loading = ref(true)

onMounted(() => {
  getFriendLinks()
    .then(r => { links.value = r.data || [] })
    .catch(() => {})
    .finally(() => { loading.value = false })
})
</script>

<style scoped>
.m-links { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 0 14px 24px; }
.m-empty { grid-column: 1 / -1; text-align: center; color: var(--text-muted); font-size: 13px; padding: 40px 0; }
.m-load { padding: 8px 14px; }
</style>
