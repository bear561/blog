<template>
  <AdminLayout>
    <div class="site-config-page">
      <div class="page-header">
        <h2 class="page-title">Site Configuration</h2>
        <el-button type="primary" :loading="saving" @click="handleSave">
          Save All
        </el-button>
      </div>
      <el-card shadow="never" v-loading="loading">
        <el-form label-position="top" v-if="!loading">
          <div class="config-section" v-for="section in configSections" :key="section.title">
            <h3 class="section-title">{{ section.title }}</h3>
            <el-row :gutter="20">
              <el-col
                v-for="item in section.items"
                :key="item.configKey"
                :span="item.span || 12"
              >
                <el-form-item :label="item.label">
                  <el-input
                    v-if="item.type === 'text' || !item.type"
                    v-model="item.configValue"
                    :placeholder="item.placeholder || ''"
                  />
                  <el-input
                    v-else-if="item.type === 'textarea'"
                    v-model="item.configValue"
                    type="textarea"
                    :rows="item.rows || 4"
                    :placeholder="item.placeholder || ''"
                  />
                  <el-input-number
                    v-else-if="item.type === 'number'"
                    v-model="item.configValue"
                    style="width: 100%"
                  />
                  <el-switch
                    v-else-if="item.type === 'switch'"
                    v-model="item.configValue"
                    :active-value="1"
                    :inactive-value="0"
                  />
                  <div class="config-desc" v-if="item.description">{{ item.description }}</div>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-form>
        <el-empty v-if="!loading && configList.length === 0" description="No configuration found" />
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/components/AdminLayout.vue'

const configList = ref([])
const loading = ref(false)
const saving = ref(false)

const configMeta = {
  siteName: { label: 'Site Name', section: 'General', type: 'text', placeholder: 'Blog name', span: 12 },
  siteDescription: { label: 'Site Description', section: 'General', type: 'textarea', rows: 2, placeholder: 'Brief description', span: 12 },
  authorName: { label: 'Author Name', section: 'General', type: 'text', span: 12 },
  authorEmail: { label: 'Author Email', section: 'General', type: 'text', span: 12 },
  aboutContent: { label: 'About Page Content', section: 'Content', type: 'textarea', rows: 8, placeholder: 'Markdown content for About page', span: 24 },
  footerInfo: { label: 'Footer Info', section: 'Content', type: 'textarea', rows: 3, placeholder: 'Footer text', span: 24 },
  pageSize: { label: 'Articles Per Page', section: 'Display', type: 'number', span: 12 },
  enableComments: { label: 'Enable Comments', section: 'Display', type: 'switch', span: 12 },
  enableAiChat: { label: 'Enable AI Chat', section: 'Features', type: 'switch', span: 12 }
}

const configSections = computed(() => {
  const sections = {}
  configList.value.forEach(item => {
    const meta = configMeta[item.configKey] || {
      label: item.configKey,
      section: 'Other',
      type: 'text'
    }
    const key = meta.section
    if (!sections[key]) {
      sections[key] = { title: key, items: [] }
    }
    sections[key].items.push({
      ...item,
      ...meta
    })
  })
  return Object.values(sections)
})

async function loadConfig() {
  loading.value = true
  try {
    const res = await request.get('/admin/site-config')
    const data = res.data || []
    configList.value = Array.isArray(data)
      ? data.map(item => ({
          ...item,
          configValue: item.type === 'switch'
            ? (item.configValue === '1' || item.configValue === 1 ? 1 : 0)
            : item.configValue
        }))
      : Object.entries(data).map(([key, value]) => ({
          configKey: key,
          configValue: value
        }))
  } catch (e) {} finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const data = {}
    configList.value.forEach(item => {
      data[item.configKey] = item.configValue
    })
    await request.put('/admin/site-config', data)
    ElMessage.success('Configuration saved successfully')
  } catch (e) {} finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>

<style scoped>
.site-config-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.config-section {
  margin-bottom: 24px;
}

.config-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
}

.config-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
