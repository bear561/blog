<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title"><el-icon :size="24"><Link /></el-icon>友情链接</h1>
      <p class="page-subtitle">共 {{ links.length }} 位朋友</p>
    </div>
    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="links.length > 0" class="links-grid">
      <el-card v-for="link in links" :key="link.id" class="link-card" shadow="hover" @click="openLink(link.url)">
        <div class="link-content">
          <el-avatar :src="link.avatar" :size="56" class="link-avatar">{{ link.name?.charAt(0)?.toUpperCase() || 'L' }}</el-avatar>
          <div class="link-info">
            <span class="link-name">{{ link.name }}</span>
            <span class="link-desc">{{ link.description || 'No description' }}</span>
          </div>
          <el-icon class="link-arrow"><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无友链" />
    <div class="exchange-notice" v-if="links.length > 0">
      <el-card shadow="never" class="notice-card"><p>想交换友链？欢迎联系我！</p></el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Link, ArrowRight } from '@element-plus/icons-vue'
import { getFriendLinks } from '@/api/friendLink'

const links = ref([])
const loading = ref(false)

function openLink(url) { if (url) window.open(url, '_blank') }

onMounted(async () => {
  loading.value = true
  try { const res = await getFriendLinks(); links.value = res.data || [] } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.page-header { text-align: center; padding: 40px 0 32px; }
.page-title { font-size: 28px; font-weight: 700; color: #303133; display: flex; align-items: center; justify-content: center; gap: 12px; }
.page-subtitle { margin-top: 8px; font-size: 14px; color: #909399; }
.loading-wrap { padding: 20px 0; }
.links-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; max-width: 900px; margin: 0 auto; }
.link-card { cursor: pointer; border-radius: 8px; transition: transform .2s, box-shadow .2s; }
.link-card:hover { transform: translateY(-2px); }
.link-content { display: flex; align-items: center; gap: 16px; }
.link-avatar { flex-shrink: 0; }
.link-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.link-name { font-size: 16px; font-weight: 600; color: #303133; }
.link-desc { font-size: 13px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.link-arrow { color: #c0c4cc; flex-shrink: 0; transition: transform .2s, color .2s; }
.link-card:hover .link-arrow { transform: translateX(4px); color: #409eff; }
.exchange-notice { max-width: 900px; margin: 32px auto 0; }
.notice-card { text-align: center; background: #f5f7fa; border: none; }
.notice-card p { font-size: 14px; color: #909399; margin: 0; }
</style>
