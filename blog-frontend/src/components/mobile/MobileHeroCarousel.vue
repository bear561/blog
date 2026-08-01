<template>
  <div v-if="items.length" class="m-hero">
    <div
      class="m-hero__track"
      :style="{ transform: `translateX(-${index * 100}%)` }"
      @pointerdown="onDown"
      @pointermove="onMove"
      @pointerup="onEnd"
      @pointercancel="onEnd"
      @pointerleave="onEnd"
    >
      <div class="m-hero__slide" v-for="a in items" :key="a.id" @click="go(a)">
        <img v-if="a.coverImage" :src="a.coverImage" alt="" class="m-hero__img" loading="lazy" />
        <div v-else class="m-hero__ph">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="4" y="3" width="16" height="18" rx="2"/><line x1="8" y1="8" x2="16" y2="8"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="8" y1="16" x2="13" y2="16"/></svg>
        </div>
        <div class="m-hero__scrim"></div>
        <div class="m-hero__cap">
          <span class="m-hero__cat" v-if="a.categoryName">{{ a.categoryName }}</span>
          <h2 class="m-hero__title">{{ a.title }}</h2>
          <p class="m-hero__sum" v-if="a.summary">{{ a.summary }}</p>
        </div>
      </div>
    </div>
    <div class="m-hero__dots" v-if="items.length > 1">
      <span
        v-for="(a, i) in items"
        :key="a.id"
        class="m-dot"
        :class="{ on: i === index }"
        @click.stop="setIndex(i)"
      ></span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  items: { type: Array, default: () => [] }
})

const router = useRouter()
const index = ref(0)
let startX = 0
let dragging = false
let timer = null

let lastX = 0
function go(a) {
  if (Math.abs(startX - lastX) > 6) return // 滑动后误触点击，忽略
  router.push(`/article/${a.id}`)
}

function setIndex(i) {
  index.value = i
  restart()
}
function onDown(e) {
  dragging = true
  startX = lastX = e.clientX
  stop()
}
function onMove(e) {
  if (dragging) lastX = e.clientX
}
function onEnd() {
  if (!dragging) return
  dragging = false
  const dx = lastX - startX
  const len = props.items.length
  if (dx < -40 && index.value < len - 1) index.value++
  else if (dx > 40 && index.value > 0) index.value--
  restart()
}
function tick() {
  if (props.items.length > 1) index.value = (index.value + 1) % props.items.length
}
function start() {
  stop()
  if (props.items.length > 1) timer = setInterval(tick, 5000)
}
function stop() {
  if (timer) { clearInterval(timer); timer = null }
}
function restart() { start() }

watch(() => props.items, () => { index.value = 0; start() })
onMounted(start)
onUnmounted(stop)
</script>

<style scoped>
.m-hero { position: relative; overflow: hidden; }
.m-hero__track {
  display: flex; touch-action: pan-y;
  transition: transform .42s cubic-bezier(.22, .61, .36, 1);
  will-change: transform;
}
.m-hero__slide {
  position: relative; flex: 0 0 100%;
  height: 58vw; min-height: 200px; max-height: 340px;
}
.m-hero__img { width: 100%; height: 100%; object-fit: cover; filter: sepia(.06); }
.m-hero__ph {
  width: 100%; height: 100%;
  background: linear-gradient(135deg, var(--bg-warm), var(--bg-code));
  display: flex; align-items: center; justify-content: center; color: var(--text-muted);
}
.m-hero__scrim {
  position: absolute; inset: 0;
  background: linear-gradient(to top, rgba(20, 16, 12, .74), rgba(20, 16, 12, .06) 56%, transparent);
}
.m-hero__cap { position: absolute; left: 0; right: 0; bottom: 0; padding: 16px 16px 20px; color: #fff; }
.m-hero__cat {
  display: inline-block; font-size: 11px; font-weight: 600;
  padding: 2px 9px; border-radius: 100px; background: var(--primary); margin-bottom: 8px;
}
.m-hero__title {
  font-family: var(--font-serif); font-size: 21px; font-weight: 700; line-height: 1.3; color: #fff;
  text-shadow: 0 1px 10px rgba(0, 0, 0, .3);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.m-hero__sum {
  margin-top: 6px; font-size: 12.5px; color: rgba(255, 255, 255, .86); line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.m-hero__dots { position: absolute; bottom: 12px; right: 14px; display: flex; gap: 5px; }
.m-dot { width: 6px; height: 6px; border-radius: 50%; background: rgba(255, 255, 255, .5); transition: all .25s; }
.m-dot.on { width: 16px; border-radius: 3px; background: #fff; }
</style>
