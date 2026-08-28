<template>
  <!-- 手机端：点卡片不再直接外跳，而是通知父组件打开详情抽屉。 -->
  <button type="button" class="m-linkcard" @click="$emit('select', link)">
    <img v-if="link.avatar" :src="link.avatar" alt="" class="m-linkcard__avimg" />
    <span v-else class="m-linkcard__av">{{ (link.name || '?').trim().charAt(0) }}</span>
    <span class="m-linkcard__main">
      <span class="m-linkcard__name">{{ link.name }}</span>
      <span class="m-linkcard__desc" v-if="link.description">{{ link.description }}</span>
    </span>
    <!-- 详情箭头：表示「展开看」而不是「外跳」 -->
    <svg class="m-linkcard__chev" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6"/></svg>
  </button>
</template>

<script setup>
defineProps({ link: { type: Object, required: true } })
defineEmits(['select'])
</script>

<style scoped>
.m-linkcard {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; background: var(--bg-card);
  border: 1px solid var(--border); border-radius: var(--radius);
  /* 兜底：无论子元素内容多长，溢出都在卡片内截断，不会撑破外层 grid */
  overflow: hidden;
  text-align: left; cursor: pointer; font: inherit; color: inherit;
  transition: transform .12s, border-color .2s;
}
.m-linkcard:active { transform: scale(.985); border-color: var(--primary-light); }
.m-linkcard__avimg { width: 40px; height: 40px; border-radius: 10px; object-fit: cover; flex-shrink: 0; }
.m-linkcard__av {
  width: 40px; height: 40px; border-radius: 10px; flex-shrink: 0;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff; font-family: var(--font-serif); font-weight: 700; font-size: 18px;
  display: flex; align-items: center; justify-content: center;
}
.m-linkcard__main { flex: 1; min-width: 0; }
.m-linkcard__name {
  display: block; font-size: 15px; font-weight: 600; color: var(--text);
  /* 长站名截断 + 省略号，避免 1fr grid 列被内容撑开 */
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.m-linkcard__desc {
  display: block; margin-top: 2px; font-size: 12px; color: var(--text-muted); line-height: 1.4;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.m-linkcard__chev { color: var(--text-muted); flex-shrink: 0; }
</style>
