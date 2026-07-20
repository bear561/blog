<template>
  <el-container>
    <SiteHeader />
    <el-main>
      <div class="content-container">
        <div class="about-page">
          <div class="page-header">
            <h1 class="page-title">
              <el-icon :size="24"><InfoFilled /></el-icon>
              About
            </h1>
          </div>

          <div v-if="loading" class="loading-wrap">
            <el-skeleton :rows="6" animated />
          </div>

          <div v-else-if="aboutHtml" class="about-content">
            <el-card shadow="never" class="about-card">
              <ArticleContent :html="aboutHtml" />
            </el-card>
          </div>

          <el-empty v-else description="No content yet" />
        </div>
      </div>
    </el-main>
    <SiteFooter />
    <AIChatWidget />
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { getSiteConfig } from '@/api/siteConfig'
import { marked } from 'marked'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ArticleContent from '@/components/ArticleContent.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'

const aboutHtml = ref('')
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSiteConfig()
    const config = res.data || res
    if (config.aboutContent || config.about_content) {
      aboutHtml.value = marked(config.aboutContent || config.about_content || '')
    }
  } catch (e) {} finally {
    loading.value = false
  }
})
</script>

<style scoped>
.about-page {
  padding: 24px 0;
}

.page-header {
  text-align: center;
  padding-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.loading-wrap {
  padding: 20px 0;
}

.about-card {
  border-radius: var(--radius-base);
  padding: 8px;
}

.about-content {
  font-size: 16px;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
  }
}
</style>
