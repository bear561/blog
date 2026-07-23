<template>
  <AdminLayout>
    <div class="manage-page">
      <div class="page-header">
        <h2 class="page-title">Friend Link Management</h2>
        <el-button type="primary" :icon="Plus" @click="openDialog()">New Link</el-button>
      </div>
      <el-card shadow="never">
        <el-table :data="list" stripe v-loading="loading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="Avatar" width="70" align="center">
            <template #default="{ row }">
              <el-avatar :src="row.avatar" :size="32">{{ row.name?.charAt(0) }}</el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="Name" width="140" />
          <el-table-column prop="url" label="URL" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link :href="row.url" target="_blank" type="primary" :underline="false">
                {{ row.url }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="Description" min-width="180" show-overflow-tooltip />
          <el-table-column label="Visible" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isVisible ? 'success' : 'info'" size="small">
                {{ row.isVisible ? 'Yes' : 'No' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="Order" width="70" align="center" />
          <el-table-column label="Actions" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openDialog(row)">Edit</el-button>
              <el-popconfirm
                title="Delete this link?"
                confirm-button-text="Delete"
                @confirm="handleDelete(row.id)"
              >
                <template #reference>
                  <el-button size="small" type="danger">Delete</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && list.length === 0" description="No friend links yet" />
      </el-card>

      <el-dialog
        v-model="dialogVisible"
        :title="editingId ? 'Edit Friend Link' : 'New Friend Link'"
        width="520px"
        destroy-on-close
      >
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-row :gutter="16">
            <el-col :span="14">
              <el-form-item label="Name" prop="name">
                <el-input v-model="form.name" placeholder="Site name" />
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="Sort Order">
                <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="URL" prop="url">
            <el-input v-model="form.url" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="Avatar">
            <div style="display: flex; gap: 12px; width: 100%">
              <el-input v-model="form.avatar" placeholder="Avatar image URL" style="flex: 1">
                <template #prepend>URL</template>
              </el-input>
              <el-upload
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button :icon="Upload" type="primary" plain>Upload</el-button>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="Description">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="Brief description" />
          </el-form-item>
          <el-form-item label="Visibility">
            <el-switch
              v-model="form.isVisible"
              active-text="Visible"
              inactive-text="Hidden"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">Save</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api'
import AdminLayout from '@/components/AdminLayout.vue'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  name: '',
  url: '',
  avatar: '',
  description: '',
  sortOrder: 0,
  isVisible: true
})
const rules = {
  name: [{ required: true, message: 'Please enter a name', trigger: 'blur' }],
  url: [{ required: true, message: 'Please enter a URL', trigger: 'blur' }]
}

async function handleAvatarUpload(options) {
  const fd = new FormData()
  fd.append('file', options.file)
  try {
    const res = await request.post('/admin/upload', fd)
    form.avatar = res.data.url
    ElMessage.success('上传成功')
    options.onSuccess()
  } catch (e) {
    ElMessage.error('上传失败')
    options.onError(e)
  }
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/admin/friend-links')
    list.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

function openDialog(row) {
  editingId.value = row?.id || null
  if (row) {
    Object.assign(form, {
      name: row.name,
      url: row.url,
      avatar: row.avatar || '',
      description: row.description || '',
      sortOrder: row.sortOrder || 0,
      isVisible: row.isVisible === 1 || row.isVisible === true
    })
  } else {
    Object.assign(form, {
      name: '', url: '', avatar: '', description: '',
      sortOrder: 0, isVisible: true
    })
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = { ...form, isVisible: form.isVisible ? 1 : 0 }
    if (editingId.value) {
      await request.put(`/admin/friend-links/${editingId.value}`, data)
    } else {
      await request.post('/admin/friend-links', data)
    }
    ElMessage.success('Saved')
    dialogVisible.value = false
    load()
  } catch (e) {} finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await request.delete(`/admin/friend-links/${id}`)
    ElMessage.success('Deleted')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.manage-page {
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
</style>
