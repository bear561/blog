<template>
  <div class="layout">
    <SiteHeader />
    <main class="layout-main"><router-view /></main>
    <SiteFooter />
    <ReadingProgress />
    <BackToTop />
  </div>
</template>

<script setup>
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ReadingProgress from '@/components/ReadingProgress.vue'
import BackToTop from '@/components/BackToTop.vue'
</script>
