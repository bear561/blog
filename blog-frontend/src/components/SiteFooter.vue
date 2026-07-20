<template>
  <el-footer class="site-footer" height="auto">
    <div class="footer-container">
      <div class="footer-info">
        <div class="footer-brand">
          <span class="footer-name">{{ appStore.siteName }}</span>
          <span class="footer-desc" v-if="appStore.siteDescription">{{ appStore.siteDescription }}</span>
        </div>
        <div class="footer-links">
          <router-link to="/">Home</router-link>
          <router-link to="/archive">Archive</router-link>
          <router-link to="/friend-links">Links</router-link>
          <router-link to="/about">About</router-link>
        </div>
      </div>
      <div class="footer-copyright">
        <p>&copy; {{ currentYear }} {{ appStore.siteName }}. All rights reserved.</p>
        <p class="footer-powered">Powered by Vue 3 + Element Plus</p>
      </div>
    </div>
  </el-footer>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.site-footer {
  background: var(--bg-white);
  border-top: 1px solid var(--border-light);
  padding: 32px 0 20px;
  margin-top: 60px;
}

.footer-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 20px;
}

.footer-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 20px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.footer-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.footer-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  font-size: 13px;
  color: var(--text-secondary);
}

.footer-links a:hover {
  color: var(--primary-color);
}

.footer-copyright {
  text-align: center;
}

.footer-copyright p {
  font-size: 12px;
  color: var(--text-placeholder);
  margin: 0;
  line-height: 1.8;
}

.footer-powered {
  font-size: 11px !important;
}

@media (max-width: 768px) {
  .footer-info {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
