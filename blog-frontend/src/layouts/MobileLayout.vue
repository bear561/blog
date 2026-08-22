<template>
  <div class="layout">
    <main class="layout-main m-layout-main">
      <router-view />
      <!-- 备案号：置于内容流末尾，靠 m-layout-main 的底部 padding 避开悬浮 TabBar -->
      <p v-if="appStore.icpNumber" class="m-footer-icp">
        <IcpLink />
      </p>
    </main>
    <ReadingProgress />
    <MobileTabBar />
    <BackToTop bottom="calc(var(--tabbar-h) + env(safe-area-inset-bottom) + 12px)" />
  </div>
</template>

<script setup>
import MobileTabBar from '@/components/mobile/MobileTabBar.vue'
import ReadingProgress from '@/components/ReadingProgress.vue'
import BackToTop from '@/components/BackToTop.vue'
import IcpLink from '@/components/IcpLink.vue'
import { useAppStore } from '@/stores/app'
const appStore = useAppStore()
</script>

<style scoped>
/* 让出底部 TabBar + 安全区，避免内容被遮挡 */
.m-layout-main {
  padding-bottom: calc(var(--tabbar-h) + env(safe-area-inset-bottom));
}
.m-footer-icp {
  font-size: 12px; color: var(--text-muted);
  margin: 28px 0 0; text-align: center;
}
</style>
