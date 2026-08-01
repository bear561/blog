<template>
  <div class="m-page">
    <MobileAppBar />
    <div class="m-404">
      <p class="m-404__k">404</p>
      <h1 class="m-404__t">页面走丢了</h1>
      <p class="m-404__s">你访问的页面不存在，也许它从未存在过。</p>
      <router-link to="/" class="m-404__btn">回到首页</router-link>
    </div>
  </div>
</template>

<script setup>
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
</script>

<style scoped>
.m-404 { text-align: center; padding: 80px 24px; }
.m-404__k { font-family: var(--font-mono); font-size: 13px; letter-spacing: 4px; color: var(--text-muted); }
.m-404__t { font-family: var(--font-serif); font-size: 30px; font-weight: 700; color: var(--text); margin: 14px 0 10px; }
.m-404__s { font-size: 14px; color: var(--text-secondary); line-height: 1.7; margin-bottom: 28px; }
.m-404__btn {
  display: inline-block; padding: 10px 28px; border: 1px solid var(--primary);
  border-radius: 100px; color: var(--primary); font-size: 14px; text-decoration: none; transition: all .2s;
}
.m-404__btn:active { background: var(--primary); color: #fff; }
</style>
