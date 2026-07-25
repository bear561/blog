<template>
  <div class="reading-progress" :style="{ width: progress + '%' }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const progress = ref(0)

function update() {
  const h = document.documentElement
  const scrollTop = h.scrollTop || document.body.scrollTop
  const scrollHeight = h.scrollHeight || document.body.scrollHeight
  const clientHeight = h.clientHeight
  if (scrollHeight <= clientHeight) {
    progress.value = 0
    return
  }
  progress.value = Math.min(100, Math.round((scrollTop / (scrollHeight - clientHeight)) * 100))
}

onMounted(() => window.addEventListener('scroll', update, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', update))
</script>

<style scoped>
.reading-progress {
  position: fixed; top: var(--header-height); left: 0; z-index: 101;
  height: 2px; background: var(--primary);
  transition: width .15s linear;
  border-radius: 0 1px 1px 0;
}
</style>
