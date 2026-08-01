<template>
  <nav class="m-tabbar">
    <router-link
      v-for="t in tabs"
      :key="t.to"
      :to="t.to"
      class="m-tab"
      :class="{ active: isActive(t.to) }"
    >
      <span class="m-tab__ico" v-html="t.svg"></span>
      <span class="m-tab__lbl">{{ t.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const S = 'viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"'
const tabs = [
  { to: '/', label: '首页', svg: `<svg ${S}><path d="M3 9.5 12 3l9 6.5"/><path d="M5 10v10h14V10"/></svg>` },
  { to: '/archive', label: '归档', svg: `<svg ${S}><rect x="3" y="4" width="18" height="4" rx="1"/><path d="M5 8v11a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V8"/><line x1="10" y1="12" x2="14" y2="12"/></svg>` },
  { to: '/friend-links', label: '友链', svg: `<svg ${S}><path d="M10 13a5 5 0 0 0 7 0l3-3a5 5 0 0 0-7-7l-1 1"/><path d="M14 11a5 5 0 0 0-7 0l-3 3a5 5 0 0 0 7 7l1-1"/></svg>` },
  { to: '/about', label: '关于', svg: `<svg ${S}><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></svg>` }
]

function isActive(to) {
  return to === '/' ? route.path === '/' : route.path.startsWith(to)
}
</script>

<style scoped>
.m-tabbar {
  position: fixed; left: 0; right: 0; bottom: 0; z-index: 900;
  height: calc(var(--tabbar-h) + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(253, 250, 244, .96);
  -webkit-backdrop-filter: blur(12px); backdrop-filter: blur(12px);
  border-top: 1px solid var(--border);
  display: flex;
}
.m-tab {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 3px;
  color: var(--text-muted); text-decoration: none;
  transition: color .15s;
}
.m-tab:active { opacity: .65; }
.m-tab__ico { width: 24px; height: 24px; display: flex; transition: transform .15s; }
.m-tab__ico :deep(svg) { width: 23px; height: 23px; }
.m-tab__lbl { font-size: 11px; letter-spacing: .2px; }
.m-tab.active { color: var(--primary); }
.m-tab.active .m-tab__ico { transform: translateY(-1px) scale(1.05); }
</style>
