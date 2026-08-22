<template>
  <!-- 备案号（工信部要求外链，未配置则整行隐藏） -->
  <a v-if="appStore.icpNumber" href="https://beian.miit.gov.cn/" target="_blank" rel="noopener" class="icp-link">
    {{ appStore.icpNumber }}
  </a>
</template>

<script setup>
import { useAppStore } from '@/stores/app'
const appStore = useAppStore()
</script>

<style scoped>
.icp-link { color: var(--text-muted); text-decoration: none; }
.icp-link:hover { color: var(--primary); }
</style>