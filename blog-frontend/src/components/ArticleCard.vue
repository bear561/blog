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
        <span class="meta-date">{{ formatDate(article.createdAt, 'yyyy-MM-dd') }}</span>
      </div>
      <h2 class="card-title">{{ article.title }}</h2>
      <p class="card-summary" v-if="article.summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="tags" v-if="article.tags?.length">
          <span class="tag" v-for="tag in article.tags.slice(0,3)" :key="tag.id">{{ tag.name }}</span>
        </div>
        <span class="views">{{ article.viewCount || 0 }} views</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { formatDate } from '@/utils/date'
defineProps({ article: { type: Object, required: true } })
</script>

<style scoped>
.article-card {
  background: var(--bg-card); border-radius: var(--radius);
  border: 1px solid var(--border); overflow: hidden;
  cursor: pointer; transition: all .25s;
}
.article-card:hover {
  box-shadow: var(--shadow-lg); transform: translateY(-2px);
  border-color: var(--primary-light);
}

.card-cover { height: 200px; overflow: hidden; }
.card-cover img { width: 100%; height: 100%; object-fit: cover; transition: transform .4s; }
.article-card:hover .card-cover img { transform: scale(1.04); }

.card-body { padding: 24px; }

.card-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.meta-category {
  font-size: 12px; font-weight: 600; color: var(--primary);
  background: rgba(59,130,246,.08); padding: 2px 10px; border-radius: 100px;
}
.meta-date { font-size: 13px; color: var(--text-muted); }

.card-title {
  font-size: 22px; font-weight: 700; line-height: 1.35; margin-bottom: 10px;
  color: var(--text);
}
.article-card:hover .card-title { color: var(--primary); }

.card-summary {
  font-size: 14px; color: var(--text-secondary); line-height: 1.7;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  margin-bottom: 16px;
}

.card-footer { display: flex; justify-content: space-between; align-items: center; }
.tags { display: flex; gap: 6px; }
.tag { font-size: 12px; color: var(--text-muted); background: var(--bg); padding: 2px 8px; border-radius: 4px; }
.views { font-size: 12px; color: var(--text-muted); }
</style>
