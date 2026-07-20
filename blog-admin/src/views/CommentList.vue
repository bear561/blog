<template>
  <AdminLayout>
    <div class="comment-list-page">
      <div class="page-header">
        <h2 class="page-title">Comment Management</h2>
      </div>
      <el-card shadow="never">
        <div class="filter-bar">
          <el-radio-group v-model="filterStatus" @change="load">
            <el-radio-button :value="undefined">All</el-radio-button>
            <el-radio-button :value="0">Pending</el-radio-button>
            <el-radio-button :value="1">Approved</el-radio-button>
          </el-radio-group>
        </div>

        <el-table :data="comments" stripe v-loading="loading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="articleTitle" label="Article" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.articleTitle || row.article?.title || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="Nickname" width="120" />
          <el-table-column prop="content" label="Comment" min-width="220" show-overflow-tooltip />
          <el-table-column label="Status" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isReviewed ? 'success' : 'warning'" size="small">
                {{ row.isReviewed ? 'Approved' : 'Pending' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Date" width="160" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createdAt || row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="Actions" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="!row.isReviewed"
                size="small"
                type="success"
                @click="handleApprove(row)"
              >
                Approve
              </el-button>
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
const filterStatus = ref(undefined)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (filterStatus.value !== undefined) params.isReviewed = filterStatus.value
    const res = await request.get('/admin/comments', { params })
    comments.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await request.put(`/admin/comments/${row.id}/review`, null, { params: { approved: true } })
    ElMessage.success('Approved')
    load()
  } catch (e) {}
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

.filter-bar {
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
