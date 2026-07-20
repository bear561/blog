<template>
  <AdminLayout>
    <div class="manage-page">
      <div class="page-header">
        <h2 class="page-title">Tag Management</h2>
        <el-button type="primary" :icon="Plus" @click="openDialog()">New Tag</el-button>
      </div>
      <el-card shadow="never">
        <el-table :data="list" stripe v-loading="loading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="Name" min-width="200" />
          <el-table-column prop="slug" label="Slug" min-width="200" show-overflow-tooltip />
          <el-table-column prop="articleCount" label="Articles" width="100" align="center" />
          <el-table-column label="Actions" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openDialog(row)">Edit</el-button>
              <el-popconfirm
                title="Delete this tag?"
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
        <el-empty v-if="!loading && list.length === 0" description="No tags yet" />
      </el-card>

      <el-dialog
        v-model="dialogVisible"
        :title="editingId ? 'Edit Tag' : 'New Tag'"
        width="500px"
        destroy-on-close
      >
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="Name" prop="name">
            <el-input v-model="form.name" placeholder="Tag name" />
          </el-form-item>
          <el-form-item label="Slug" prop="slug">
            <el-input v-model="form.slug" placeholder="URL slug" />
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api'
import AdminLayout from '@/components/AdminLayout.vue'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', slug: '' })
const rules = {
  name: [{ required: true, message: 'Please enter a name', trigger: 'blur' }],
  slug: [{ required: true, message: 'Please enter a slug', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/admin/tags')
    list.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

function openDialog(row) {
  editingId.value = row?.id || null
  form.name = row?.name || ''
  form.slug = row?.slug || ''
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await request.put(`/admin/tags/${editingId.value}`, form)
    } else {
      await request.post('/admin/tags', form)
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
    await request.delete(`/admin/tags/${id}`)
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
