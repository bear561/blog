<template>
  <AdminLayout>
    <div class="dashboard">
      <h2 class="page-title">Dashboard</h2>

      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="6" v-for="item in stats" :key="item.label">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" :style="{ background: item.color }">
                <el-icon :size="24"><component :is="item.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-label">{{ item.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 24px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span class="card-header-title">Quick Actions</span>
            </template>
            <div class="quick-actions">
              <el-button
                v-for="action in quickActions"
                :key="action.label"
                :icon="action.icon"
                :type="action.type || 'default'"
                @click="$router.push(action.route)"
              >
                {{ action.label }}
              </el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span class="card-header-title">System Info</span>
            </template>
            <div class="system-info">
              <div class="info-item">
                <span class="info-label">Frontend</span>
                <span class="info-value">Vue 3 + Element Plus</span>
              </div>
              <div class="info-item">
                <span class="info-label">Version</span>
                <span class="info-value">1.0.0</span>
              </div>
              <div class="info-item">
                <span class="info-label">Dashboard</span>
                <span class="info-value">Admin Panel</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Document, ChatDotRound, Clock, FolderOpened, Plus, Edit, ChatLineSquare } from '@element-plus/icons-vue'
import request from '@/api'
import AdminLayout from '@/components/AdminLayout.vue'

const stats = reactive([
  { label: 'Articles', value: 0, icon: Document, color: 'linear-gradient(135deg, #409eff, #3a8ee6)' },
  { label: 'Comments', value: 0, icon: ChatDotRound, color: 'linear-gradient(135deg, #67c23a, #5daf34)' },
  { label: 'Pending', value: 0, icon: Clock, color: 'linear-gradient(135deg, #e6a23c, #d48806)' },
  { label: 'Categories', value: 0, icon: FolderOpened, color: 'linear-gradient(135deg, #f56c6c, #e64242)' }
])

const quickActions = [
  { label: 'New Article', icon: Plus, route: '/admin/articles/edit', type: 'primary' },
  { label: 'Manage Articles', icon: Edit, route: '/admin/articles' },
  { label: 'Review Comments', icon: ChatLineSquare, route: '/admin/comments' },
  { label: 'Site Config', icon: FolderOpened, route: '/admin/site-config' }
]

onMounted(async () => {
  try {
    const res = await request.get('/admin/dashboard')
    const d = res.data
    stats[0].value = d.articleCount || 0
    stats[1].value = d.commentCount || 0
    stats[2].value = d.pendingCommentCount || 0
    stats[3].value = d.categoryCount || 0
  } catch (e) {}
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 24px 0;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  border-radius: 8px;
  cursor: default;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.card-header-title {
  font-weight: 600;
  font-size: 15px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
  font-size: 13px;
}

.info-value {
  color: #303133;
  font-size: 13px;
  font-weight: 500;
}
</style>
