<template>
  <transition name="fade-up">
    <div v-if="visible" class="back-to-top" :style="{ bottom }" @click="scrollTop" title="回到顶部">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
        stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="18 15 12 9 6 15" />
      </svg>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 默认 88px 与历史像素一致；移动端由布局传入“TabBar + 安全区”高度，避免被底栏遮挡
defineProps({
  bottom: { type: String, default: '88px' }
})

const visible = ref(false)

function update() {
  visible.value = window.scrollY > 400
}

function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => window.addEventListener('scroll', update, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', update))
</script>

<style scoped>
.back-to-top {
  position: fixed; bottom: 88px; right: 26px; z-index: 999;
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--bg-card); border: 1px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--text-muted);
  transition: all .2s;
}
.back-to-top:hover { color: var(--primary); border-color: var(--primary-light); transform: translateY(-2px); }

.fade-up-enter-active, .fade-up-leave-active { transition: all .3s ease; }
.fade-up-enter-from, .fade-up-leave-to { opacity: 0; transform: translateY(8px); }
</style>
