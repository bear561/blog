<template>
  <div class="m-phead">
    <h1 class="m-phead__t">{{ title }}</h1>
    <p class="m-phead__s" v-if="subtitle || $slots.sub">
      {{ subtitle }}<slot name="sub" />
    </p>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' }
})
</script>

<style scoped>
.m-phead { padding: 14px 16px 12px; }
.m-phead__t { font-family: var(--font-serif); font-size: 22px; font-weight: 700; color: var(--text); letter-spacing: -.01em; }
.m-phead__s { margin-top: 4px; font-size: 12.5px; color: var(--text-muted); }
</style>
