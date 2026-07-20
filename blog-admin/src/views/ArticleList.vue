<template>
  <AdminLayout>
    <div class="article-list-page">
      <div class="page-toolbar">
        <h2 class="page-title">Article Management</h2>
        <div class="toolbar-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="Search articles..."
            :prefix-icon="Search"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-button type="primary" :icon="Plus" @click="$router.push('/admin/articles/edit')">
            New Article
          </el-button>
        </div>
      </div>

      <el-card shadow="never">
        <el-table
          :data="articles"
          v-loading="loading"
          style="width: 100%"
          stripe
          @sort-change="handleSortChange"
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="title" label="Title" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link type="primary" @click="$router.push(`/admin/articles/edit/${row.id}`)">
                {{ row.title }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="Category" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.categoryName || row.category?.name" size="small" effect="plain">
                {{ row.categoryName || row.category?.name }}
              </el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="Status" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="(row.status === 1 || row.isPublished) ? 'success' : 'warning'"
                size="small"
              >
                {{ (row.status === 1 || row.isPublished) ? 'Published' : 'Draft' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Views" width="80" align="center" sortable="custom" prop="viewCount">
            <template #default="{ row }">
              {{ row.viewCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="Date" width="160" align="center" sortable="custom" prop="createdAt">
            <template #default="{ row }">
              {{ formatDate(row.createdAt || row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="Actions" width="280" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="!row.isPublished && row.status !== 1"
                text
                type="success"
                size="small"
                @click="togglePublish(row)"
              >
                Publish
              </el-button>
              <el-button
                text
                type="primary"
                size="small"
                @click="$router.push(`/admin/articles/edit/${row.id}`)"
              >
                Edit
              </el-button>
              <el-popconfirm
                title="Delete this article?"
                confirm-button-text="Delete"
                @confirm="handleDelete(row.id)"
              >
                <template #reference>
                  <el-button text type="danger" size="small">Delete</el-button>
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
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api'
import { formatDate } from '@/utils/date'
import AdminLayout from '@/components/AdminLayout.vue'

const articles = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sortField = ref('createdAt')
const sortOrder = ref('desc')

async function load() {
  loading.value = true
  try {
    const res = await request.get('/admin/articles', {
      params: {
        page: page.value,
        size: pageSize.value,
        keyword: searchKeyword.value || undefined,
        sortField: sortField.value,
        sortOrder: sortOrder.value
      }
    })
    articles.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  load()
}

function handlePageChange(p) {
  page.value = p
  load()
}

function handleSizeChange(size) {
  pageSize.value = size
  page.value = 1
  load()
}

function handleSortChange({ prop, order }) {
  sortField.value = prop || 'createdAt'
  sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  load()
}

async function togglePublish(row) {
  try {
    await request.put(`/admin/articles/${row.id}/publish`, null, { params: { publish: true } })
    ElMessage.success('Published successfully')
    load()
  } catch (e) {}
}

async function handleDelete(id) {
  try {
    await request.delete(`/admin/articles/${id}`)
    ElMessage.success('Deleted successfully')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.article-list-page {
  padding: 0;
}

.page-toolbar {
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

.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.text-muted {
  color: #c0c4cc;
}
</style>
