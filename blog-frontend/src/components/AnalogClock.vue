<template>
  <div class="analog-clock">
    <svg viewBox="0 0 120 120" class="clock-svg">
      <defs>
        <filter id="clock-shadow">
          <feDropShadow dx="0" dy="1" stdDeviation="2" flood-opacity="0.08" />
        </filter>
      </defs>

      <!-- 外圈 -->
      <circle cx="60" cy="60" r="54" fill="var(--bg-card)" stroke="var(--border)" stroke-width="1.5" filter="url(#clock-shadow)" />

      <!-- 内圈装饰 -->
      <circle cx="60" cy="60" r="52" fill="none" stroke="var(--border)" stroke-width="0.5" />

      <!-- 数字 -->
      <g font-family="Inter, sans-serif" font-size="9" font-weight="500" fill="#5c5b59" text-anchor="middle" dominant-baseline="central">
        <text v-for="i in 12" :key="'num'+i"
          :x="60 + 34 * Math.cos((i*30-90)*Math.PI/180)"
          :y="60 + 34 * Math.sin((i*30-90)*Math.PI/180)"
        >{{ i }}</text>
      </g>

      <!-- 时刻度 -->
      <g v-for="i in 12" :key="'tick'+i">
        <line
          :x1="60 + 44 * Math.cos((i*30-90)*Math.PI/180)"
          :y1="60 + 44 * Math.sin((i*30-90)*Math.PI/180)"
          :x2="60 + (i%3===0 ? 38 : 42) * Math.cos((i*30-90)*Math.PI/180)"
          :y2="60 + (i%3===0 ? 38 : 42) * Math.sin((i*30-90)*Math.PI/180)"
          :stroke="i%3===0 ? '#c44f2e' : '#c4bfb6'"
          :stroke-width="i%3===0 ? 1.8 : 0.8"
          stroke-linecap="round"
        />
      </g>

      <!-- 时针 -->
      <line
        x1="60" y1="60"
        :x2="hx" :y2="hy"
        stroke="#1a1a1a" stroke-width="3.5" stroke-linecap="round"
      />

      <!-- 分针 -->
      <line
        x1="60" y1="60"
        :x2="mx" :y2="my"
        stroke="#1a1a1a" stroke-width="2" stroke-linecap="round"
      />

      <!-- 秒针（一根线穿过中心，尾短头长） -->
      <line
        :x1="stx" :y1="sty"
        :x2="sx" :y2="sy"
        stroke="#c44f2e" stroke-width="0.8" stroke-linecap="round"
      />

      <!-- 中心铆钉 -->
      <circle cx="60" cy="60" r="3.5" fill="#c44f2e" />
      <circle cx="60" cy="60" r="1.5" fill="#fff" />
    </svg>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const hours = ref(0)
const minutes = ref(0)
const seconds = ref(0)

// 时针
const hx = computed(() => 60 + 22 * Math.cos(((hours.value % 12) * 30 + minutes.value * 0.5 - 90) * Math.PI / 180))
const hy = computed(() => 60 + 22 * Math.sin(((hours.value % 12) * 30 + minutes.value * 0.5 - 90) * Math.PI / 180))

// 分针
const mx = computed(() => 60 + 34 * Math.cos((minutes.value * 6 - 90) * Math.PI / 180))
const my = computed(() => 60 + 34 * Math.sin((minutes.value * 6 - 90) * Math.PI / 180))

// 秒针尖端
const sx = computed(() => 60 + 40 * Math.cos((seconds.value * 6 - 90) * Math.PI / 180))
const sy = computed(() => 60 + 40 * Math.sin((seconds.value * 6 - 90) * Math.PI / 180))

// 秒针尾部（反方向 12 单位）
const stx = computed(() => 60 + 12 * Math.cos((seconds.value * 6 + 90) * Math.PI / 180))
const sty = computed(() => 60 + 12 * Math.sin((seconds.value * 6 + 90) * Math.PI / 180))

let timer = null

onMounted(() => {
  function tick() {
    const now = new Date()
    hours.value = now.getHours()
    minutes.value = now.getMinutes()
    seconds.value = now.getSeconds()
    timer = setTimeout(tick, 1000 - now.getMilliseconds())
  }
  tick()
})

onUnmounted(() => clearTimeout(timer))
</script>

<style scoped>
.analog-clock {
  width: 130px; height: 130px; margin: 0 auto;
}
.clock-svg { width: 100%; height: 100%; display: block; }
</style>
