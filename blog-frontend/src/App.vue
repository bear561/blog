<template>
  <div id="app-container">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

onMounted(async () => {
  await appStore.loadSiteConfig()
})
</script>

<style>
#app-container {
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-color);
}
</style>
