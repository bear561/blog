<template>
  <div class="pagination-wrapper" v-if="total > pageSize">
    <el-pagination
      :current-page="current"
      :page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 30, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script setup>
defineProps({
  current: { type: Number, default: 1 },
  total: { type: Number, default: 0 },
  pageSize: { type: Number, default: 10 }
})
const emit = defineEmits(['page-change', 'size-change'])
function handlePageChange(page) { emit('page-change', page) }
function handleSizeChange(size) { emit('size-change', size) }
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: center; margin-top: 40px; padding: 20px 0; }
</style>
