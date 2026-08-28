<template>
  <!-- 友链详情底部抽屉：沿用 HomeMobile 的 .m-sheet-mask + .m-sheet 模式 -->
  <transition name="sheet" @after-leave="onAfterLeave">
    <div v-if="modelValue && !closing" class="m-sheet-mask" @click.self="close">
      <div class="m-sheet m-linkd-sheet" @click.stop>
        <div class="m-sheet__handle" />

        <div class="m-linkd" v-if="link">
          <img v-if="link.avatar" :src="link.avatar" class="m-linkd__av" alt="" />
          <span v-else class="m-linkd__av-fb">{{ (link.name || '?').trim().charAt(0) }}</span>

          <h2 class="m-linkd__name">{{ link.name }}</h2>

          <!-- 纯展示：URL 完整可读，长 URL 截断；不再作为复制入口，避免和下方按钮重复 -->
          <span class="m-linkd__url" :title="link.url">
            <span class="m-linkd__url-text">{{ link.url }}</span>
          </span>

          <p v-if="link.description" class="m-linkd__desc">{{ link.description }}</p>

          <div class="m-linkd__actions">
            <button class="m-linkd__btn m-linkd__btn--ghost" @click="copyUrl">
              {{ copied ? '已复制 ✓' : '复制链接' }}
            </button>
            <button class="m-linkd__btn m-linkd__btn--primary" @click="visit">
              访问网站 →
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  link: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'close'])

const copied = ref(false)
// 关闭动画进行中：避免父组件在 leave 期间换了 link 导致内容闪烁
const closing = ref(false)
let copyTimer = null
// 记录 body 滚动锁定前的 overflow 状态，关闭时还原
let prevBodyOverflow = ''

function clearCopyTimer() {
  if (copyTimer) { clearTimeout(copyTimer); copyTimer = null }
}

function lockBodyScroll(lock) {
  if (typeof document === 'undefined') return
  if (lock) {
    if (prevBodyOverflow === '') prevBodyOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = prevBodyOverflow
    prevBodyOverflow = ''
  }
}

// 跟随 v-model 显隐：开时锁滚动，外部 v-model 变 false 时解锁 + 标记 closing 拦截父级切 link
watch(() => props.modelValue, (v) => {
  if (v) {
    closing.value = false
    lockBodyScroll(true)
  } else {
    lockBodyScroll(false)
  }
})

function close() {
  if (!props.modelValue) return
  copied.value = false
  clearCopyTimer()
  closing.value = true
  emit('update:modelValue', false)
}

function onAfterLeave() {
  closing.value = false
  emit('close')
}

// 真正打开新标签：保留 SPA 原页面 history。弹出被拦截时不要静默关闭抽屉。
function visit() {
  if (!props.link?.url) return
  const win = window.open(props.link.url, '_blank', 'noopener,noreferrer')
  if (win) close()
}

async function copyUrl() {
  if (!props.link?.url) return
  const text = props.link.url
  let ok = false
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
      ok = true
    } else {
      // 旧浏览器 / 非安全上下文 fallback
      const ta = document.createElement('textarea')
      ta.value = text
      ta.style.position = 'fixed'; ta.style.opacity = '0'
      document.body.appendChild(ta)
      ta.select()
      ok = document.execCommand('copy')
      document.body.removeChild(ta)
    }
  } catch { ok = false }

  if (ok) {
    copied.value = true
    clearCopyTimer()
    copyTimer = setTimeout(() => { copied.value = false; copyTimer = null }, 2000)
  }
}

onBeforeUnmount(() => {
  clearCopyTimer()
  // 保险：万一组件在抽屉打开时被卸载，还原 body 滚动
  if (prevBodyOverflow !== '') document.body.style.overflow = prevBodyOverflow
})
</script>

<style scoped>
/* 与 HomeMobile 的 .m-sheet-mask / .m-sheet 完全同款 */
.m-sheet-mask {
  position: fixed; inset: 0; z-index: 1550;
  background: rgba(26, 16, 12, .4);
  display: flex; align-items: flex-end;
}
.m-sheet {
  width: 100%; background: var(--bg-card);
  border-radius: 16px 16px 0 0;
  padding: 12px 18px calc(18px + env(safe-area-inset-bottom));
  max-height: 70vh; overflow-y: auto;
}
.m-sheet__handle {
  width: 36px; height: 4px; border-radius: 2px;
  background: var(--border); margin: 0 auto 14px;
}

/* 友链详情内容 */
.m-linkd-sheet { padding-top: 8px; }
.m-linkd {
  display: flex; flex-direction: column; align-items: center;
  gap: 6px; text-align: center; padding: 0 2px;
}
.m-linkd__av,
.m-linkd__av-fb {
  width: 72px; height: 72px; border-radius: 16px;
  flex-shrink: 0; margin: 4px 0 4px;
}
.m-linkd__av { object-fit: cover; }
.m-linkd__av-fb {
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff; font-family: var(--font-serif); font-weight: 700; font-size: 30px;
  display: flex; align-items: center; justify-content: center;
}
.m-linkd__name {
  font-family: var(--font-serif); font-size: 22px; font-weight: 700;
  color: var(--text); margin: 0;
  max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
/* 纯展示：max-width 留出 6px padding 缓冲，避免贴边 */
.m-linkd__url {
  display: inline-flex; align-items: center; gap: 6px;
  font-family: var(--font-mono); font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-warm); border: 1px solid var(--border);
  padding: 4px 10px; border-radius: 100px;
  max-width: calc(100vw - 48px); /* 兜底：老 WebView 不支持 min() */
  margin: 0;
}
@media (min-width: 480px) {
  .m-linkd__url { max-width: 432px; }
}
.m-linkd__url-text {
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  max-width: calc(60vw - 60px); /* 留出 pill 自身 padding + 边框的空间 */
}
@media (min-width: 480px) {
  .m-linkd__url-text { max-width: 220px; }
}
.m-linkd__desc {
  font-size: 14px; line-height: 1.6; color: var(--text-secondary);
  text-align: left; width: 100%;
  margin: 10px 0 0;
  white-space: pre-wrap; word-break: break-word;
}
.m-linkd__actions {
  display: flex; gap: 10px; width: 100%;
  margin-top: 18px;
}
.m-linkd__btn {
  flex: 1; height: 44px; border-radius: 22px;
  font-size: 15px; font-weight: 600; border: none; cursor: pointer;
  transition: transform .12s, background .15s, color .15s;
}
.m-linkd__btn:active { transform: scale(.97); }
.m-linkd__btn--ghost {
  background: var(--bg-warm); color: var(--text);
}
.m-linkd__btn--ghost:active { background: var(--border); }
.m-linkd__btn--primary {
  background: var(--primary); color: #fff;
}
.m-linkd__btn--primary:active { background: var(--primary-dark); }

/* slide-up 过渡（与 HomeMobile 完全相同） */
.sheet-enter-active, .sheet-leave-active { transition: opacity .25s ease; }
.sheet-enter-active .m-sheet, .sheet-leave-active .m-sheet { transition: transform .3s cubic-bezier(.22, .61, .36, 1); }
.sheet-enter-from, .sheet-leave-to { opacity: 0; }
.sheet-enter-from .m-sheet, .sheet-leave-to .m-sheet { transform: translateY(100%); }
</style>
