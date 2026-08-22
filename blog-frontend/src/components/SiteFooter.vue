<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-links">
        <router-link to="/">首页</router-link>
        <router-link to="/archive">归档</router-link>
        <router-link to="/friend-links">友链</router-link>
        <router-link to="/about">关于</router-link>
      </div>
      <p class="copyright">&copy; {{ currentYear }} {{ appStore.siteName }}</p>
      <p v-if="appStore.icpNumber" class="icp">
        <IcpLink />
      </p>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import IcpLink from '@/components/IcpLink.vue'
const appStore = useAppStore()
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.site-footer {
  margin-top: 80px; padding: 36px 24px;
  border-top: 1px solid var(--border);
  background: var(--bg-warm);
}
.footer-inner { max-width: var(--max-width); margin: 0 auto; text-align: center; }
.footer-links { display: flex; justify-content: center; gap: 28px; margin-bottom: 14px; }
.footer-links a { font-size: 13px; color: var(--text-secondary); transition: color .2s; }
.footer-links a:hover { color: var(--primary); }
.copyright { font-size: 12px; color: var(--text-muted); margin: 0; }
.icp { font-size: 12px; color: var(--text-muted); margin: 6px 0 0; }
</style>
