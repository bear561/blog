<template>
  <div id="app-container">
    <SiteHeader />
    <router-view />
    <SiteFooter />
    <AIChatWidget />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'

const appStore = useAppStore()

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

#app-container > :nth-child(2) {
  flex: 1;
}
</style>
