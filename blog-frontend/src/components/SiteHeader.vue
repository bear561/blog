<template>
  <el-header class="site-header" height="64px">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="site-logo">
          <h1 class="site-name">{{ appStore.siteName }}</h1>
        </router-link>
        <nav class="nav-links">
          <router-link to="/" class="nav-item" active-class="nav-active">Home</router-link>
          <router-link to="/archive" class="nav-item" active-class="nav-active">Archive</router-link>
          <router-link to="/friend-links" class="nav-item" active-class="nav-active">Links</router-link>
          <router-link to="/about" class="nav-item" active-class="nav-active">About</router-link>
        </nav>
      </div>
      <div class="header-right">
        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="Search articles..."
            :prefix-icon="Search"
            size="default"
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          />
        </div>
      </div>
    </div>
  </el-header>
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
  const trimmed = keyword.value.trim()
  if (trimmed) {
    router.push({ name: 'Search', query: { keyword: trimmed } })
  }
}
</script>

<style scoped>
.site-header {
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-light);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: var(--max-width);
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.site-logo {
  text-decoration: none;
}

.site-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  white-space: nowrap;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  color: var(--text-regular);
  padding: 8px 16px;
  border-radius: var(--radius-small);
  font-size: 14px;
  transition: all 0.2s;
}

.nav-item:hover {
  color: var(--primary-color);
  background-color: rgba(64, 158, 255, 0.06);
}

.nav-active {
  color: var(--primary-color);
  font-weight: 500;
}

.search-input {
  width: 220px;
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .search-input {
    width: 160px;
  }

  .header-left {
    gap: 12px;
  }
}
</style>
