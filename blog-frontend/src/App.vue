<template>
  <div id="app-container">
    <component :is="isMobile ? MobileLayout : DesktopLayout" />
    <AIChatWidget />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import { useDeviceMode } from '@/composables/useDeviceMode'
import AIChatWidget from '@/components/AIChatWidget.vue'
import DesktopLayout from '@/layouts/DesktopLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'

const appStore = useAppStore()
const { isMobile } = useDeviceMode()

onMounted(async () => {
  await appStore.loadSiteConfig()
})
</script>

<style>
#app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 布局根占满剩余高度，保证页脚贴底（替代旧的 :nth-child(2) 规则，避免误命中 AIChatWidget 根节点） */
#app-container > .layout {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.layout-main {
  flex: 1;
}
</style>
