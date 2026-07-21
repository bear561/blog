<template>
  <header class="site-header">
    <div class="header-inner">
      <router-link to="/" class="logo">{{ appStore.siteName }}</router-link>
      <nav class="nav">
        <router-link to="/" class="nav-link" exact-active-class="active">Home</router-link>
        <router-link to="/archive" class="nav-link" active-class="active">Archive</router-link>
        <router-link to="/friend-links" class="nav-link" active-class="active">Links</router-link>
        <router-link to="/about" class="nav-link" active-class="active">About</router-link>
      </nav>
      <div class="header-actions">
        <el-input v-model="keyword" size="small" placeholder="Search..." :prefix-icon="Search"
          @keyup.enter="handleSearch" clearable class="search-input" />
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const router = useRouter()
const keyword = ref('')

function handleSearch() {
  const q = keyword.value.trim()
  if (q) router.push({ name: 'Search', query: { keyword: q } })
}
</script>

<style scoped>
.site-header {
  position: sticky; top: 0; z-index: 100;
  background: rgba(15, 23, 42, .92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255,255,255,.06);
  height: var(--header-height);
}

.header-inner {
  max-width: var(--max-width); margin: 0 auto;
  height: 100%; display: flex; align-items: center;
  padding: 0 24px; gap: 32px;
}

.logo {
  font-size: 20px; font-weight: 800; color: #fff; letter-spacing: -.5px;
  white-space: nowrap;
}
.logo:hover { color: var(--primary-light); }

.nav { display: flex; gap: 4px; }
.nav-link {
  padding: 6px 16px; border-radius: 8px; font-size: 14px;
  color: rgba(255,255,255,.7); transition: all .2s;
}
.nav-link:hover, .nav-link.active { color: #fff; background: rgba(255,255,255,.1); }

.header-actions { margin-left: auto; }
.search-input { width: 200px; }
.search-input :deep(.el-input__wrapper) {
  background: rgba(255,255,255,.1); border: none; box-shadow: none;
  border-radius: 8px;
}
.search-input :deep(.el-input__inner) { color: #fff; }
.search-input :deep(.el-input__inner::placeholder) { color: rgba(255,255,255,.4); }
.search-input :deep(.el-input__prefix) { color: rgba(255,255,255,.4); }
</style>
