<template>
  <div class="daily-quote">
    <svg class="quote-mark" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" opacity="0.15">
      <path d="M10 11h-4v6h6v-8l-2-5h-2l2 5zm8 0h-4v6h6v-8l-2-5h-2l2 5z"/>
    </svg>
    <p class="quote-text">{{ quote.text }}</p>
    <p class="quote-author">— {{ quote.author }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const quotes = [
  { text: 'Stay hungry, stay foolish.', author: 'Steve Jobs' },
  { text: '代码是写给人看的，顺便能在机器上运行。', author: 'Harold Abelson' },
  { text: '简单是可靠的先决条件。', author: 'Edsger Dijkstra' },
  { text: 'Talk is cheap. Show me the code.', author: 'Linus Torvalds' },
  { text: 'First, solve the problem. Then, write the code.', author: 'John Johnson' },
  { text: '程序必须首先让人类可以理解，然后才能让计算机执行。', author: 'Donald Knuth' },
  { text: '任何足够先进的技术都与魔法无异。', author: 'Arthur C. Clarke' },
  { text: '优秀的代码本身就是最好的文档。', author: 'Steve McConnell' },
  { text: '大道至简。', author: '老子' },
  { text: '不积跬步，无以至千里。', author: '荀子' },
  { text: '软件是吃进去的数学，长出来的艺术。', author: '佚名' },
  { text: 'The best error message is the one that never shows up.', author: 'Thomas Fuchs' },
  { text: 'Debugging is twice as hard as writing the code.', author: 'Brian Kernighan' },
  { text: 'There are two ways of constructing a software design.', author: 'Tony Hoare' },
  { text: '予岂好辩哉，予不得已也。', author: '孟子' },
  { text: '万事开头难。', author: '谚语' },
  { text: '知之为知之，不知为不知，是知也。', author: '孔子' },
  { text: 'Practice makes progress, not perfect.', author: '未知' },
]

const quote = ref(quotes[0])

onMounted(() => {
  // 基于日期选择，同一天看到同一句
  const d = new Date()
  const idx = (d.getFullYear() * 10000 + (d.getMonth() + 1) * 100 + d.getDate()) % quotes.length
  quote.value = quotes[idx]
})
</script>

<style scoped>
.daily-quote {
  position: relative;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); padding: 18px 16px 14px;
  text-align: center;
}
.quote-mark { position: absolute; top: 8px; left: 10px; color: var(--accent); }
.quote-text { font-size: 13px; color: var(--text-secondary); line-height: 1.7; font-style: italic; margin-bottom: 6px; }
.quote-author { font-size: 11px; color: var(--text-muted); }
</style>
