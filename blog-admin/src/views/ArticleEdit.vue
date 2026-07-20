<template>
  <AdminLayout>
    <div class="article-edit-page">
      <div class="page-header">
        <h2 class="page-title">{{ isEdit ? 'Edit Article' : 'New Article' }}</h2>
        <div class="header-actions">
          <el-button @click="$router.back()">Cancel</el-button>
          <el-button type="success" @click="handleSave(false)" :loading="submitting">
            Save Draft
          </el-button>
          <el-button type="primary" @click="handleSave(true)" :loading="submitting">
            Publish
          </el-button>
        </div>
      </div>

      <el-card shadow="never" v-loading="loading">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="article-form"
        >
          <el-form-item label="Title" prop="title">
            <el-input
              v-model="form.title"
              placeholder="Enter article title"
              size="large"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Category">
                <el-select
                  v-model="form.categoryId"
                  placeholder="Select category"
                  clearable
                  style="width: 100%"
                  size="large"
                >
                  <el-option
                    v-for="c in categories"
                    :key="c.id"
                    :label="c.name"
                    :value="c.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Tags">
                <el-select
                  v-model="form.tagIds"
                  multiple
                  placeholder="Select tags"
                  style="width: 100%"
                  size="large"
                >
                  <el-option
                    v-for="t in tags"
                    :key="t.id"
                    :label="t.name"
                    :value="t.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="Summary" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="2"
              placeholder="Brief summary of the article"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="Cover Image">
            <div class="cover-upload">
              <el-input
                v-model="form.coverImage"
                placeholder="Cover image URL"
                style="flex: 1"
              >
                <template #prepend>URL</template>
              </el-input>
              <el-upload
                :show-file-list="false"
                :before-upload="handleCoverUpload"
                accept="image/*"
                style="flex-shrink: 0"
              >
                <el-button :icon="Upload" type="primary" plain>Upload</el-button>
              </el-upload>
            </div>
            <div class="cover-preview" v-if="form.coverImage">
              <el-image
                :src="form.coverImage"
                fit="contain"
                style="max-height: 200px"
              />
            </div>
          </el-form-item>

          <el-form-item label="Content (Markdown)" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="18"
              placeholder="Write your article in Markdown format..."
              class="markdown-editor"
            />
          </el-form-item>

          <el-form-item>
            <el-row style="width: 100%">
              <el-col :span="12">
                <el-checkbox v-model="form.isTop" label="Pin to top" />
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api'
import AdminLayout from '@/components/AdminLayout.vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const categories = ref([])
const tags = ref([])

const form = reactive({
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: null,
  tagIds: [],
  isPublished: false,
  isTop: false
})

const rules = {
  title: [{ required: true, message: 'Please enter a title', trigger: 'blur' }],
  content: [{ required: true, message: 'Please enter content', trigger: 'blur' }]
}

async function handleCoverUpload(file) {
  try {
    const res = await request.post('/admin/upload', { file }, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.coverImage = res.data?.url || res.data
    ElMessage.success('Image uploaded')
  } catch (e) {}
  return false
}

async function loadFormData() {
  loading.value = true
  try {
    const [catRes, tagRes] = await Promise.all([
      request.get('/categories'),
      request.get('/tags')
    ])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []

    if (isEdit.value) {
      const res = await request.get(`/admin/articles/${route.params.id}`)
      const a = res.data
      Object.assign(form, {
        title: a.title || '',
        summary: a.summary || '',
        content: a.content || '',
        coverImage: a.coverImage || '',
        categoryId: a.categoryId || null,
        tagIds: a.tags?.map(t => t.id) || a.tagIds || [],
        isPublished: a.isPublished === 1 || a.isPublished === true,
        isTop: a.isTop === 1 || a.isTop === true
      })
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

async function handleSave(publish) {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      ...form,
      isPublished: publish ? 1 : 0,
      isTop: form.isTop ? 1 : 0
    }
    if (isEdit.value) {
      await request.put(`/admin/articles/${route.params.id}`, data)
    } else {
      await request.post('/admin/articles', data)
    }
    ElMessage.success(isEdit.value ? 'Updated successfully' : 'Created successfully')
    router.push('/admin/articles')
  } catch (e) {} finally {
    submitting.value = false
  }
}

onMounted(loadFormData)
</script>

<style scoped>
.article-edit-page {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.article-form {
  max-width: 100%;
}

.cover-upload {
  display: flex;
  gap: 12px;
  width: 100%;
}

.cover-preview {
  margin-top: 12px;
  padding: 12px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  text-align: center;
}

.markdown-editor :deep(.el-textarea__inner) {
  font-family: 'Fira Code', 'Source Code Pro', Consolas, Monaco, monospace;
  font-size: 14px;
  line-height: 1.7;
}
</style>
