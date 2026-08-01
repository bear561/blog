<template>
  <header class="m-appbar">
    <div class="m-appbar__row">
      <div class="m-appbar__left">
        <button v-if="back" class="m-iconbtn" aria-label="返回" @click="goBack">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <router-link v-else to="/" class="m-mark">{{ appStore.siteName }}</router-link>
      </div>
      <div class="m-appbar__right">
        <button v-if="!hideSearch" class="m-iconbtn" aria-label="搜索" @click="$router.push('/search')">
          <svg viewBox="0 0 24 24" width="21" height="21" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </button>
        <button class="m-iconbtn m-iconbtn--ai" aria-label="AI 问答" @click="aiStore.isOpen = true">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8z"/></svg>
        </button>
      </div>
    </div>
    <div v-if="$slots.below" class="m-appbar__below"><slot name="below" /></div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAiStore } from '@/stores/ai'

defineProps({
  back: { type: Boolean, default: false },
  hideSearch: { type: Boolean, default: false }
})

const router = useRouter()
const appStore = useAppStore()
const aiStore = useAiStore()

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/')
}
</script>

<style scoped>
.m-appbar {
  position: sticky; top: 0; z-index: 100;
  background: rgba(253, 250, 244, .92);
  -webkit-backdrop-filter: blur(10px); backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
}
.m-appbar__row {
  height: var(--header-height);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 10px 0 14px; gap: 8px;
}
.m-appbar__left { display: flex; align-items: center; min-width: 0; }
.m-appbar__right { display: flex; align-items: center; gap: 2px; }
.m-mark {
  font-family: var(--font-serif); font-weight: 700; font-size: 18px;
  color: var(--text); letter-spacing: -.3px; white-space: nowrap;
  overflow: hidden; text-overflow: ellipsis; max-width: 60vw;
}
.m-iconbtn {
  width: 40px; height: 40px; border: none; background: transparent;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-secondary); border-radius: 50%; cursor: pointer;
  transition: background .15s, color .15s;
}
.m-iconbtn:active { background: var(--bg-warm); }
.m-iconbtn--ai { color: var(--primary); }
</style>
