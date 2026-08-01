import { ref, onMounted } from 'vue'

// 模块级单例：所有组件共享同一个 isMobile，只挂一次监听，避免泄漏。
// 以"能力"判断：窄视口，或带粗指针（触屏）的中小屏 → 移动态。
const QUERY = '(max-width: 900px), (pointer: coarse) and (max-width: 1024px)'

const isMobile = ref(false)
let mql = null
let bound = false

if (typeof window !== 'undefined' && window.matchMedia) {
  mql = window.matchMedia(QUERY)
  isMobile.value = mql.matches
}

function sync() {
  isMobile.value = mql.matches
}
function ensureListener() {
  if (bound || !mql) return
  bound = true
  if (mql.addEventListener) mql.addEventListener('change', sync)
  else if (mql.addListener) mql.addListener(sync) // 旧 Safari 兜底
}

export function useDeviceMode() {
  onMounted(ensureListener)
  return { isMobile }
}
