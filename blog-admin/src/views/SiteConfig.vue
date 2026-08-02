<template>
  <AdminLayout>
    <div class="config-page">
      <div class="page-header">
        <h2>站点配置</h2>
        <div class="header-actions">
          <el-button type="primary" @click="addRow">新增配置</el-button>
          <el-button :loading="saving" @click="handleSave">保存全部</el-button>
        </div>
      </div>

      <!-- 头像上传区域 -->
      <el-card shadow="never" class="avatar-card">
        <template #header><span>站点头像</span></template>
        <div class="avatar-section">
          <div class="avatar-preview">
            <el-avatar :size="100" :src="avatarUrl">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
          </div>
          <div class="avatar-actions">
            <el-upload
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <el-button type="primary" :loading="uploading">上传头像</el-button>
            </el-upload>
            <el-button v-if="avatarUrl" @click="handleRemoveAvatar" :loading="removing" type="danger" plain>
              移除头像
            </el-button>
            <div class="avatar-tip">支持 jpg、png、gif 格式，大小不超过 2MB</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" v-loading="loading" style="margin-top:16px">
        <el-table :data="tableConfigList" stripe style="width:100%">
          <el-table-column prop="configKey" label="Key" width="240">
            <template #default="{ row }">
              <el-input v-model="row.configKey" size="small" placeholder="key" />
            </template>
          </el-table-column>
          <el-table-column prop="configValue" label="Value">
            <template #default="{ row }">
              <el-input v-if="row.configKey === 'about_content'"
                v-model="row.configValue" type="textarea" :rows="4" size="small" />
              <el-input v-else-if="row.configKey === 'ai_suggestions'"
                v-model="row.configValue" type="textarea" :rows="2" size="small" />
              <el-input v-else-if="(row.configValue || '').length > 80"
                v-model="row.configValue" type="textarea" :rows="2" size="small" />
              <el-input v-else v-model="row.configValue" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" width="200">
            <template #default="{ row }">
              <el-input v-model="row.description" size="small" placeholder="可选" />
            </template>
          </el-table-column>
          <el-table-column width="60" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" :icon="Delete" circle size="small" @click="removeRow(row)" />
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && tableConfigList.length===0" description="暂无配置" />
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Delete, UserFilled } from '@element-plus/icons-vue'
import request from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminLayout from '@/components/AdminLayout.vue'

const configList = ref([])
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const removing = ref(false)

// 过滤掉 site_avatar，它由头像卡片单独管理
const tableConfigList = computed(() =>
  configList.value.filter(row => row.configKey !== 'site_avatar')
)

// 从 configList 中提取当前头像URL
const avatarUrl = computed(() => {
  const row = configList.value.find(r => r.configKey === 'site_avatar')
  return row?.configValue || ''
})

async function loadConfig() {
  loading.value = true
  try {
    const res = await request.get('/admin/site-config')
    const data = res.data || []
    configList.value = Array.isArray(data) ? [...data] : Object.entries(data).map(([k,v]) => ({ configKey: k, configValue: v }))
  } catch (e) {} finally { loading.value = false }
}

function addRow() {
  configList.value.push({ configKey: '', configValue: '', description: '' })
}

function removeRow(row) {
  const idx = configList.value.findIndex(r => r.configKey === row.configKey && r.configValue === row.configValue)
  if (idx !== -1) configList.value.splice(idx, 1)
}

async function handleSave() {
  saving.value = true
  try {
    const map = {}
    configList.value.forEach(item => { if (item.configKey) map[item.configKey] = item.configValue })
    await request.put('/admin/site-config', map)
    ElMessage.success('保存成功')
    await loadConfig()
  } catch (e) {} finally { saving.value = false }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

async function handleAvatarUpload({ file }) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post('/admin/site-config/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const url = res.data?.url || ''
    // 更新本地列表中的 site_avatar
    const existing = configList.value.find(r => r.configKey === 'site_avatar')
    if (existing) {
      existing.configValue = url
    } else {
      configList.value.push({ configKey: 'site_avatar', configValue: url, description: '站点头像' })
    }
    ElMessage.success('头像上传成功')
  } catch (e) {} finally { uploading.value = false }
}

async function handleRemoveAvatar() {
  try {
    await ElMessageBox.confirm('确定要移除头像吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  removing.value = true
  try {
    await request.put('/admin/site-config', { site_avatar: '' })
    const row = configList.value.find(r => r.configKey === 'site_avatar')
    if (row) row.configValue = ''
    ElMessage.success('头像已移除')
  } catch (e) {} finally { removing.value = false }
}

onMounted(loadConfig)
</script>

<style scoped>
.config-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
.header-actions { display: flex; gap: 12px; }

.avatar-card { margin-bottom: 0; }
.avatar-section { display: flex; align-items: center; gap: 24px; }
.avatar-preview {
  flex-shrink: 0;
  width: 100px; height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0f2f5;
  display: flex; align-items: center; justify-content: center;
}
.avatar-actions { display: flex; flex-direction: column; gap: 10px; align-items: flex-start; }
.avatar-tip { font-size: 12px; color: #999; }
</style>
