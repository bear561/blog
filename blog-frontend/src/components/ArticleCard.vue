<template>
  <article class="article-card" @click="$router.push(`/article/${article.id}`)">
    <div class="card-cover" v-if="article.coverImage">
      <img :src="article.coverImage" alt="" loading="lazy" />
    </div>
    <div class="card-body">
      <div class="card-meta">
        <span class="meta-category" v-if="article.categoryName">
          {{ article.categoryName }}
        </span>
        <span class="meta-date">{{ formatDate(article.createdAt, 'YYYY-MM-DD') }}</span>
        <span class="meta-read" v-if="readMin > 0">约 {{ readMin }} 分钟</span>
      </div>
      <h2 class="card-title">{{ article.title }}</h2>
      <p class="card-summary" v-if="article.summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="tags" v-if="article.tags?.length">
          <span class="tag" v-for="tag in article.tags.slice(0,3)" :key="tag.id">{{ tag.name }}</span>
        </div>
        <span class="views">{{ article.viewCount || 0 }} 阅读</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate } from '@/utils/date'
import { readingTime } from '@/utils/readingTime'

const props = defineProps({ article: { type: Object, required: true } })

const readMin = computed(() => {
  const a = props.article
  const html = a.contentHtml || a.summary || ''
  return readingTime(html)
})
</script>

<style scoped>
.article-card {
  display: flex; gap: 24px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid var(--border);
  background: var(--bg-card);
  cursor: pointer; transition: border-color .2s;
}
.article-card:last-child { margin-bottom: 0; }
.article-card:hover { border-color: var(--primary-light); }
.article-card:hover .card-title { color: var(--primary); }

.card-cover {
  width: 260px; height: 170px; flex-shrink: 0;
  overflow: hidden; background: var(--bg-warm);
}
.card-cover img {
  width: 100%; height: 100%; object-fit: cover;
  filter: sepia(0.06); transition: transform .4s;
}
.article-card:hover .card-cover img { transform: scale(1.03); }

.card-body { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }

.card-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.meta-category {
  font-size: 11px; font-weight: 600; color: var(--primary);
  border: 1px solid rgba(196,79,46,.25); padding: 1px 8px; border-radius: 100px;
}
.meta-date { font-size: 12px; color: var(--text-muted); }
.meta-read { font-size: 11px; color: var(--text-muted); }

.card-title {
  font-size: 18px; font-weight: 700; line-height: 1.4; margin-bottom: 6px;
  color: var(--text); transition: color .2s;
}

.card-summary {
  font-size: 13px; color: var(--text-secondary); line-height: 1.65;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  margin-bottom: 10px;
}

.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.tags { display: flex; gap: 6px; }
.tag { font-size: 11px; color: var(--text-muted); background: var(--bg-warm); padding: 2px 8px; border-radius: 3px; }
.views { font-size: 12px; color: var(--text-muted); }
</style>
