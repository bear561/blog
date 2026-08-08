<template>
  <div ref="contentRef" class="article-content markdown-body" v-html="renderedHtml"></div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark-dimmed.css'

const props = defineProps({
  html: {
    type: String,
    default: ''
  },
  markdown: {
    type: String,
    default: ''
  }
})

const contentRef = ref(null)

const renderedHtml = computed(() => {
  if (props.html) return props.html
  if (props.markdown) {
    // marked v5+ 已移除 highlight 选项，代码高亮统一交给下面的 hljs.highlightElement
    return marked(props.markdown, { breaks: true, gfm: true })
  }
  return ''
})

function highlightCodeBlocks() {
  // 只查询本组件内的代码块，避免影响页面其他实例
  contentRef.value?.querySelectorAll('pre code').forEach((block) => {
    hljs.highlightElement(block)
  })
}

// flush: 'post' 保证回调在 v-html 更新 DOM 之后执行；
// immediate: true 覆盖挂载时已有内容的场景，异步加载完成后内容变化也会重新触发
watch(renderedHtml, highlightCodeBlocks, { immediate: true, flush: 'post' })
</script>

<style scoped>
/* ===== 文章正文 ===== */
.article-content {
  font-family: var(--font-serif);
  font-size: 16px;
  line-height: 1.9;
  color: var(--text);
  word-wrap: break-word;
}

/* ===== 标题 ===== */
.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  font-family: var(--font-sans);
  margin-top: 36px;
  margin-bottom: 16px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text);
}

.article-content :deep(h1) { font-size: 26px; }
.article-content :deep(h2) {
  font-size: 22px;
  padding-left: 14px;
  border-left: 3px solid var(--primary);
}
.article-content :deep(h3) { font-size: 19px; color: var(--accent); }
.article-content :deep(h4) { font-size: 17px; color: var(--text-secondary); }

/* ===== 段落 + 首字下沉 ===== */
.article-content :deep(p) { margin-bottom: 18px; }

.article-content :deep(> p:first-of-type::first-letter),
.article-content :deep(> *:first-child > p:first-of-type::first-letter),
.article-content :deep(> p:first-child::first-letter) {
  float: left;
  font-size: 3.4em;
  font-weight: 700;
  line-height: 0.85;
  margin-right: 10px;
  margin-top: 2px;
  color: var(--primary);
  font-family: var(--font-sans);
}

/* ===== 图片 ===== */
.article-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius);
  margin: 20px 0;
}

/* ===== 引用块 ===== */
.article-content :deep(blockquote) {
  margin: 24px 0;
  padding: 20px 24px;
  border-left: 3px solid var(--primary);
  background: var(--bg-warm);
  color: var(--text-secondary);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  font-style: italic;
}
.article-content :deep(blockquote p) { margin-bottom: 0; }

/* ===== 代码 ===== */
.article-content :deep(pre) {
  margin: 20px 0;
  border-radius: var(--radius);
  overflow-x: auto;
  background: #1e1e1e;
}
.article-content :deep(pre code) {
  display: block;
  padding: 18px 22px;
  font-size: 13px;
  line-height: 1.7;
  font-family: var(--font-mono);
  background: transparent;
  color: #d4d4d4;
}
.article-content :deep(code):not(pre code) {
  background: var(--bg-code);
  color: var(--primary-dark);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.88em;
  font-family: var(--font-mono);
}

/* ===== 列表 ===== */
.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}
.article-content :deep(li) { margin-bottom: 8px; }

/* ===== 表格 =====
   列压缩自适应：表格始终占满容器宽度，列自动分配；
   单元格内容过长时强制换行，保证任何屏宽都不超出屏幕、无需横向滑动 */
.article-content :deep(table) {
  width: 100%; margin: 20px 0; border-collapse: collapse;
  table-layout: auto;
  font-size: 14px; font-family: var(--font-sans);
}
.article-content :deep(th),
.article-content :deep(td) {
  padding: 10px 16px; border: 1px solid var(--border); text-align: left;
  word-break: break-word;
  overflow-wrap: anywhere;
}
.article-content :deep(th) { background: var(--bg-warm); font-weight: 600; }

/* ===== 分割线 ===== */
.article-content :deep(hr) {
  margin: 28px 0; border: none; border-top: 1px solid var(--border);
}

/* ===== 链接 ===== */
.article-content :deep(a) {
  color: var(--primary);
  text-decoration: underline;
  text-decoration-color: transparent;
  text-underline-offset: 3px;
  transition: text-decoration-color .2s;
}
.article-content :deep(a:hover) {
  text-decoration-color: var(--primary);
}
</style>
