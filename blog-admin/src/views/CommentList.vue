<template>
  <AdminLayout>
    <div class="comment-list-page">
      <div class="page-header">
        <h2 class="page-title">Comment Management</h2>
      </div>
      <el-card shadow="never">
        <el-table :data="comments" stripe v-loading="loading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="articleTitle" label="Article" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.articleTitle || row.article?.title || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="Nickname" width="140">
            <template #default="{ row }">
              {{ row.nickname }}
              <el-tag v-if="row.isAnonymous === 1" size="small" type="info" effect="plain" style="margin-left: 6px">
                匿名
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="Comment" min-width="220" show-overflow-tooltip />
          <el-table-column label="Date" width="160" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createdAt || row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="Actions" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-popconfirm
                title="Delete this comment?"
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

        <div class="pagination-wrapper" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="(s) => { pageSize = s; page = 1; load() }"
            @current-change="(p) => { page = p; load() }"
          />
        </div>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api'
import { formatDate } from '@/utils/date'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/components/AdminLayout.vue'

const comments = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    const res = await request.get('/admin/comments', { params })
    comments.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  try {
    await request.delete(`/admin/comments/${id}`)
    ElMessage.success('Deleted')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.comment-list-page {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
