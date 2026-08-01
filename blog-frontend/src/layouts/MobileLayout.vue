<template>
  <div class="layout">
    <main class="layout-main m-layout-main"><router-view /></main>
    <ReadingProgress />
    <MobileTabBar />
    <BackToTop bottom="calc(var(--tabbar-h) + env(safe-area-inset-bottom) + 12px)" />
  </div>
</template>

<script setup>
import MobileTabBar from '@/components/mobile/MobileTabBar.vue'
import ReadingProgress from '@/components/ReadingProgress.vue'
import BackToTop from '@/components/BackToTop.vue'
</script>

<style scoped>
/* 让出底部 TabBar + 安全区，避免内容被遮挡 */
.m-layout-main {
  padding-bottom: calc(var(--tabbar-h) + env(safe-area-inset-bottom));
}
</style>
