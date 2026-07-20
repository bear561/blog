<template>
  <div class="article-content markdown-body" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

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

const renderedHtml = computed(() => {
  if (props.html) return props.html
  if (props.markdown) {
    return marked(props.markdown, {
      breaks: true,
      gfm: true,
      highlight(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
          return hljs.highlight(code, { language: lang }).value
        }
        return hljs.highlightAuto(code).value
      }
    })
  }
  return ''
})

function highlightCodeBlocks() {
  nextTick(() => {
    document.querySelectorAll('.article-content pre code').forEach((block) => {
      if (!block.dataset.highlighted) {
        hljs.highlightElement(block)
        block.dataset.highlighted = 'true'
      }
    })
  })
}

onMounted(() => {
  highlightCodeBlocks()
})
</script>

<style scoped>
.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
  word-wrap: break-word;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4),
.article-content :deep(h5),
.article-content :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.4;
}

.article-content :deep(h1) { font-size: 28px; }
.article-content :deep(h2) { font-size: 24px; padding-bottom: 8px; border-bottom: 1px solid var(--border-light); }
.article-content :deep(h3) { font-size: 20px; }
.article-content :deep(h4) { font-size: 18px; }

.article-content :deep(p) {
  margin-bottom: 16px;
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-base);
  margin: 16px 0;
  box-shadow: var(--shadow-base);
}

.article-content :deep(blockquote) {
  margin: 16px 0;
  padding: 12px 20px;
  border-left: 4px solid var(--primary-color);
  background: rgba(64, 158, 255, 0.05);
  color: var(--text-regular);
  border-radius: 0 var(--radius-small) var(--radius-small) 0;
}

.article-content :deep(blockquote p) {
  margin-bottom: 0;
}

.article-content :deep(pre) {
  margin: 16px 0;
  border-radius: var(--radius-base);
  overflow-x: auto;
  box-shadow: var(--shadow-base);
}

.article-content :deep(pre code) {
  display: block;
  padding: 16px 20px;
  font-size: 14px;
  line-height: 1.6;
  font-family: 'Fira Code', 'Source Code Pro', Consolas, Monaco, monospace;
}

.article-content :deep(code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.9em;
  font-family: 'Fira Code', 'Source Code Pro', Consolas, Monaco, monospace;
}

.article-content :deep(pre code) {
  background: transparent;
  padding: 0;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.article-content :deep(li) {
  margin-bottom: 8px;
}

.article-content :deep(table) {
  width: 100%;
  margin: 16px 0;
  border-collapse: collapse;
}

.article-content :deep(th),
.article-content :deep(td) {
  padding: 10px 16px;
  border: 1px solid var(--border-light);
  text-align: left;
}

.article-content :deep(th) {
  background: var(--bg-color);
  font-weight: 600;
}

.article-content :deep(hr) {
  margin: 24px 0;
  border: none;
  border-top: 1px solid var(--border-light);
}

.article-content :deep(a) {
  color: var(--primary-color);
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.article-content :deep(a:hover) {
  border-bottom-color: var(--primary-color);
}
</style>
