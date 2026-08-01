<template>
  <article class="m-card" @click="$router.push(`/article/${article.id}`)">
    <div class="m-card__cover">
      <img v-if="article.coverImage" :src="article.coverImage" alt="" loading="lazy" />
      <div v-else class="m-card__ph">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.6"><rect x="4" y="3" width="16" height="18" rx="2"/><line x1="8" y1="8" x2="16" y2="8"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
      </div>
    </div>
    <div class="m-card__body">
      <div class="m-card__meta">
        <span class="m-chip" v-if="article.categoryName">{{ article.categoryName }}</span>
        <span class="m-card__date">{{ formatDate(article.createdAt, 'YYYY-MM-DD') }}</span>
        <span class="m-card__read">· {{ readMin }} 分钟</span>
      </div>
      <h3 class="m-card__title">{{ article.title }}</h3>
      <p class="m-card__sum" v-if="article.summary">{{ article.summary }}</p>
      <div class="m-card__foot"><span>{{ article.viewCount || 0 }} 阅读</span></div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate } from '@/utils/date'
import { readingTime } from '@/utils/readingTime'

const props = defineProps({ article: { type: Object, required: true } })
const readMin = computed(() => readingTime(props.article.contentHtml || props.article.summary || ''))
</script>

<style scoped>
.m-card {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); overflow: hidden; margin-bottom: 14px;
  box-shadow: var(--shadow-sm);
  transition: transform .12s ease, border-color .2s, box-shadow .2s;
}
.m-card:active { transform: scale(.985); border-color: var(--primary-light); box-shadow: var(--shadow-md); }
.m-card__cover { height: 46vw; min-height: 150px; max-height: 210px; background: var(--bg-warm); overflow: hidden; }
.m-card__cover img { width: 100%; height: 100%; object-fit: cover; filter: sepia(.06); }
.m-card__ph { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: var(--text-muted); }
.m-card__body { padding: 12px 14px 14px; }
.m-card__meta { display: flex; align-items: center; gap: 8px; margin-bottom: 7px; }
.m-chip {
  font-size: 10.5px; font-weight: 600; color: var(--primary);
  border: 1px solid rgba(196, 79, 46, .25); padding: 1px 8px; border-radius: 100px;
}
.m-card__date, .m-card__read { font-size: 11px; color: var(--text-muted); }
.m-card__title {
  font-family: var(--font-serif); font-size: 18px; font-weight: 700; line-height: 1.4; color: var(--text);
  margin-bottom: 6px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.m-card__sum {
  font-size: 13px; color: var(--text-secondary); line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.m-card__foot { margin-top: 10px; font-size: 11px; color: var(--text-muted); }
</style>
