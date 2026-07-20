<template>
  <el-container style="min-height:100vh">
    <el-aside width="220px" style="background:#304156">
      <div class="logo">
        <h2>Blog Admin</h2>
      </div>
      <el-menu
        :default-active="currentRoute"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><FolderOpened /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/friend-links">
          <el-icon><Link /></el-icon>
          <span>友链管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/site-config">
          <el-icon><Setting /></el-icon>
          <span>站点配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;display:flex;align-items:center;justify-content:flex-end;border-bottom:1px solid #e6e6e6">
        <span style="margin-right:16px;color:#606266">{{ authStore.nickname }}</span>
        <el-button type="danger" text @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { DataAnalysis, Document, ChatDotRound, FolderOpened, PriceTag, Link, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentRoute = computed(() => route.path)

const handleLogout = () => {
  authStore.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.logo { padding: 20px; text-align: center; }
.logo h2 { color: #fff; font-size: 18px; }
.el-aside { overflow: hidden; }
</style>
