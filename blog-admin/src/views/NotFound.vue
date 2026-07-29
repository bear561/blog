<template>
  <div class="not-found-page">
    <el-result icon="warning" title="404" sub-title="抱歉，你访问的页面不存在">
      <template #extra>
        <el-button type="primary" @click="$router.push('/admin/dashboard')">返回仪表盘</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
</style>
