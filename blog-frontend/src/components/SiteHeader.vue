<template>
  <header class="site-header" :class="{ scrolled: isScrolled }">
    <div class="header-inner">
      <router-link to="/" class="logo">{{ appStore.siteName }}</router-link>

      <nav class="nav">
        <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
        <router-link to="/archive" class="nav-link" active-class="active">归档</router-link>
        <router-link to="/friend-links" class="nav-link" active-class="active">友链</router-link>
        <router-link to="/about" class="nav-link" active-class="active">关于</router-link>
      </nav>

      <div class="header-actions">
        <div class="search-wrap" :class="{ focused: searchFocused }">
          <el-icon :size="16" class="search-icon"><Search /></el-icon>
          <input
            ref="searchInput"
            v-model="keyword"
            type="text"
            placeholder="搜索文章..."
            class="search-input-native"
            @keyup.enter="handleSearch"
            @focus="searchFocused = true"
            @blur="searchFocused = false"
          />
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const router = useRouter()
const keyword = ref('')
const searchFocused = ref(false)
const isScrolled = ref(false)

function onScroll() {
  isScrolled.value = window.scrollY > 10
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

function handleSearch() {
  const q = keyword.value.trim()
  if (q) router.push({ name: 'Search', query: { keyword: q } })
}
</script>

<style scoped>
.site-header {
  position: sticky; top: 0; z-index: 100;
  background: rgba(249, 244, 235, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: border-color .3s, background .3s;
  height: var(--header-height);
}
.site-header.scrolled {
  border-bottom-color: var(--border);
  background: rgba(249, 244, 235, 0.97);
}

.header-inner {
  max-width: var(--max-width); margin: 0 auto;
  height: 100%; display: flex; align-items: center;
  padding: 0 24px; gap: 32px;
}

.logo {
  font-size: 18px; font-weight: 700; color: var(--text);
  letter-spacing: -.3px; white-space: nowrap;
  font-family: var(--font-serif);
}
.logo:hover { color: var(--primary); }

.nav { display: flex; gap: 0; }
.nav-link {
  position: relative;
  padding: 6px 16px; font-size: 14px; font-weight: 500;
  color: var(--text-secondary); transition: color .2s;
}
.nav-link::after {
  content: '';
  position: absolute; bottom: 2px; left: 50%; transform: translateX(-50%) scaleX(0);
  width: 16px; height: 2px; border-radius: 1px;
  background: var(--primary);
  transition: transform .2s;
}
.nav-link:hover { color: var(--text); }
.nav-link:hover::after { transform: translateX(-50%) scaleX(1); }
.nav-link.active { color: var(--primary); }
.nav-link.active::after { transform: translateX(-50%) scaleX(1); }

.header-actions { margin-left: auto; }
.search-wrap {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 12px;
  border-bottom: 1px solid var(--border);
  transition: border-color .25s;
}
.search-wrap.focused { border-bottom-color: var(--primary); }
.search-icon { color: var(--text-muted); flex-shrink: 0; }
.search-input-native {
  border: none; outline: none; background: transparent;
  font-size: 13px; color: var(--text); width: 160px;
  font-family: var(--font-sans);
}
.search-input-native::placeholder { color: var(--text-muted); }
</style>
