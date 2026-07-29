<template>
  <div class="not-found">
    <p class="nf-kicker">ERROR · 404</p>
    <h1 class="nf-title">页面走丢了</h1>
    <p class="nf-text">你访问的页面不存在，也许它从未存在过。</p>
    <router-link to="/" class="nf-link">回到首页</router-link>
  </div>
</template>

<style scoped>
.not-found {
  max-width: 640px;
  margin: 0 auto;
  padding: 120px 24px;
  text-align: center;
}

.nf-kicker {
  font-family: var(--font-mono, monospace);
  font-size: 12px;
  letter-spacing: 4px;
  color: var(--text-muted);
  margin-bottom: 20px;
}

.nf-title {
  font-family: var(--font-serif);
  font-size: 42px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 16px;
}

.nf-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-secondary);
  margin-bottom: 36px;
}

.nf-link {
  display: inline-block;
  padding: 10px 28px;
  border: 1px solid var(--primary);
  border-radius: 100px;
  color: var(--primary);
  text-decoration: none;
  font-size: 14px;
  transition: all .2s;
}

.nf-link:hover {
  background: var(--primary);
  color: #fff;
}
</style>
