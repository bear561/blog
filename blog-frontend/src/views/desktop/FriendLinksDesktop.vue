<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">友情链接</h1>
      <p class="page-subtitle">共 {{ links.length }} 位朋友</p>
    </div>
    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="links.length > 0" class="links-wall">
      <a
        v-for="link in links" :key="link.id"
        class="link-card"
        :href="link.url"
        target="_blank"
        rel="noopener"
      >
        <el-avatar :src="link.avatar" :size="48" class="link-avatar">
          {{ link.name?.charAt(0)?.toUpperCase() || 'L' }}
        </el-avatar>
        <div class="link-info">
          <span class="link-name">{{ link.name }}</span>
          <span class="link-desc" v-if="link.description">{{ link.description }}</span>
        </div>
      </a>
    </div>
    <el-empty v-else description="暂无友链" />
    <div class="exchange-notice" v-if="links.length > 0">
      <p>想交换友链？欢迎联系我！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFriendLinks } from '@/api/friendLink'

const links = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try { const res = await getFriendLinks(); links.value = res.data || [] } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.page-header { text-align: center; padding: 40px 0 32px; }
.page-title { font-size: 24px; font-weight: 700; color: var(--text); }
.page-subtitle { margin-top: 8px; font-size: 14px; color: var(--text-muted); }

/* 友链墙：flex-wrap 让 4-6 个友链在一行内均匀排开，max-width 800 防过宽 */
.links-wall {
  display: flex; flex-wrap: wrap; justify-content: center; align-items: flex-start;
  gap: 14px; max-width: 800px; margin: 0 auto;
}

/* 统一尺寸：所有卡片一致大小，避免 3 档尺寸循环带来的视觉跳动感 */
.link-card {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 20px;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius);
  text-align: left; cursor: pointer;
  text-decoration: none; color: inherit;
  transition: border-color .25s, transform .2s, box-shadow .2s;
}
.link-card:hover {
  border-color: var(--primary-light); transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(196,79,46,.06);
}
.link-avatar { flex-shrink: 0; }

.link-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.link-name {
  font-size: 14px; font-weight: 600; color: var(--text);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  max-width: 200px;
}
.link-desc {
  font-size: 12px; color: var(--text-muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  max-width: 200px;
}

.exchange-notice { max-width: 800px; margin: 40px auto 0; text-align: center; }
.exchange-notice p { font-size: 13px; color: var(--text-muted); padding: 14px; background: var(--bg-warm); border-radius: var(--radius); }
</style>
