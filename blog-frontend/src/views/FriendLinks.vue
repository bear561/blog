<template>
  <el-container>
    <SiteHeader />
    <el-main>
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">
            <el-icon :size="24"><Link /></el-icon>
            Friend Links
          </h1>
          <p class="page-subtitle">{{ links.length }} friend(s)</p>
        </div>

        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else-if="links.length > 0" class="links-grid">
          <el-card
            v-for="link in links"
            :key="link.id"
            class="link-card"
            shadow="hover"
            @click="openLink(link.url)"
          >
            <div class="link-content">
              <el-avatar
                :src="link.avatar"
                :size="56"
                class="link-avatar"
              >
                {{ link.name?.charAt(0)?.toUpperCase() || 'L' }}
              </el-avatar>
              <div class="link-info">
                <span class="link-name">{{ link.name }}</span>
                <span class="link-desc">{{ link.description || 'No description' }}</span>
              </div>
              <el-icon class="link-arrow"><ArrowRight /></el-icon>
            </div>
          </el-card>
        </div>

        <el-empty v-else description="No friend links yet" />

        <div class="exchange-notice" v-if="links.length > 0">
          <el-card shadow="never" class="notice-card">
            <p>Want to exchange links? Feel free to contact me!</p>
          </el-card>
        </div>
      </div>
    </el-main>
    <SiteFooter />
    <AIChatWidget />
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Link, ArrowRight } from '@element-plus/icons-vue'
import { getFriendLinks } from '@/api/friendLink'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'

const links = ref([])
const loading = ref(false)

function openLink(url) {
  if (url) {
    window.open(url, '_blank')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getFriendLinks()
    links.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-header {
  text-align: center;
  padding: 40px 0 32px;
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

.page-subtitle {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.loading-wrap {
  padding: 20px 0;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  max-width: 900px;
  margin: 0 auto;
}

.link-card {
  cursor: pointer;
  border-radius: var(--radius-base);
  transition: transform 0.2s, box-shadow 0.2s;
}

.link-card:hover {
  transform: translateY(-2px);
}

.link-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.link-avatar {
  flex-shrink: 0;
}

.link-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.link-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.link-desc {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-arrow {
  color: var(--text-placeholder);
  flex-shrink: 0;
  transition: transform 0.2s, color 0.2s;
}

.link-card:hover .link-arrow {
  transform: translateX(4px);
  color: var(--primary-color);
}

.exchange-notice {
  max-width: 900px;
  margin: 32px auto 0;
}

.notice-card {
  text-align: center;
  background: var(--bg-color);
  border: none;
}

.notice-card p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

@media (max-width: 768px) {
  .links-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 22px;
  }
}
</style>
