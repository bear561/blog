<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-links">
        <router-link to="/">Home</router-link>
        <router-link to="/archive">Archive</router-link>
        <router-link to="/friend-links">Links</router-link>
        <router-link to="/about">About</router-link>
      </div>
      <p class="copyright">&copy; {{ currentYear }} {{ appStore.siteName }}</p>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
const appStore = useAppStore()
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.site-footer { margin-top: 80px; padding: 32px 24px; border-top: 1px solid var(--border); background: var(--bg-card); }
.footer-inner { max-width: var(--max-width); margin: 0 auto; text-align: center; }
.footer-links { display: flex; justify-content: center; gap: 24px; margin-bottom: 16px; }
.footer-links a { font-size: 13px; color: var(--text-secondary); transition: color .2s; }
.footer-links a:hover { color: var(--primary); }
.copyright { font-size: 12px; color: var(--text-muted); margin: 0; }
</style>
