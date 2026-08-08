<template>
  <nav class="article-toc" aria-label="内容目录">
    <div class="toc-title">目录</div>
    <ul class="toc-list">
      <li
        v-for="(item, i) in headings"
        :key="i"
        class="toc-item"
        :class="{ 'is-h3': item.level === 3, 'is-active': i === activeIndex }"
      >
        <a class="toc-link" @click="$emit('scroll-to', i)">{{ item.text }}</a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
defineProps({
  headings: { type: Array, default: () => [] },
  activeIndex: { type: Number, default: -1 }
})
defineEmits(['scroll-to'])
</script>

<style scoped>
.article-toc { font-family: var(--font-sans); }
.toc-title {
  font-size: 13px; font-weight: 600; color: var(--text-muted);
  letter-spacing: .05em; margin-bottom: 10px;
}
.toc-list { list-style: none; margin: 0; padding: 0; }
.toc-item { position: relative; }
.toc-link {
  display: block;
  padding: 5px 10px;
  font-size: 13px; line-height: 1.5;
  color: var(--text-secondary);
  cursor: pointer;
  border-left: 2px solid transparent;
  transition: color .15s, background .15s;
  word-break: break-word;
}
.toc-item.is-h3 .toc-link { padding-left: 22px; }
.toc-link:hover { color: var(--primary); }
.toc-item.is-active .toc-link {
  color: var(--primary);
  border-left-color: var(--primary);
  background: rgba(196, 79, 46, 0.05);
}
</style>