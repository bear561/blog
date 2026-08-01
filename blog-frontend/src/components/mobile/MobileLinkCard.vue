<template>
  <a class="m-linkcard" :href="link.url" target="_blank" rel="noopener noreferrer">
    <img v-if="link.avatar" :src="link.avatar" alt="" class="m-linkcard__avimg" />
    <span v-else class="m-linkcard__av">{{ (link.name || '?').trim().charAt(0) }}</span>
    <span class="m-linkcard__main">
      <span class="m-linkcard__name">{{ link.name }}</span>
      <span class="m-linkcard__desc" v-if="link.description">{{ link.description }}</span>
    </span>
    <svg class="m-linkcard__ext" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 17 17 7"/><path d="M8 7h9v9"/></svg>
  </a>
</template>

<script setup>
defineProps({ link: { type: Object, required: true } })
</script>

<style scoped>
.m-linkcard {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; background: var(--bg-card);
  border: 1px solid var(--border); border-radius: var(--radius);
  text-decoration: none; transition: transform .12s, border-color .2s;
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
.m-linkcard__name { display: block; font-size: 15px; font-weight: 600; color: var(--text); }
.m-linkcard__desc {
  display: block; margin-top: 2px; font-size: 12px; color: var(--text-muted); line-height: 1.4;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.m-linkcard__ext { color: var(--text-muted); flex-shrink: 0; }
</style>
