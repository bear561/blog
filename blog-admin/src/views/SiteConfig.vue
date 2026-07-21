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

      <el-card shadow="never" v-loading="loading">
        <el-table :data="configList" stripe style="width:100%">
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
            <template #default="{ $index }">
              <el-button type="danger" :icon="Delete" circle size="small" @click="configList.splice($index,1)" />
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && configList.length===0" description="暂无配置" />
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import request from '@/api'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/components/AdminLayout.vue'

const configList = ref([])
const loading = ref(false)
const saving = ref(false)

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

onMounted(loadConfig)
</script>

<style scoped>
.config-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; }
.header-actions { display: flex; gap: 12px; }
</style>
