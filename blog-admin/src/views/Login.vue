<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-wrapper">
      <el-card class="login-card" shadow="always">
        <div class="login-header">
          <div class="login-icon">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
          <h2 class="login-title">Blog Admin</h2>
          <p class="login-subtitle">Sign in to your admin panel</p>
        </div>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="Username"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="Password"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              size="large"
              style="width: 100%"
              @click="handleLogin"
            >
              Sign In
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <p class="login-footer">Blog Admin Panel v1.0</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please enter your username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter your password', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    router.push('/admin/dashboard')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%);
}

.login-wrapper {
  position: relative;
  z-index: 1;
  width: 420px;
}

.login-card {
  border-radius: 12px;
  overflow: hidden;
}

.login-header {
  text-align: center;
  padding: 32px 0 24px;
}

.login-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  padding: 0 8px 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

@media (max-width: 480px) {
  .login-wrapper {
    width: 90%;
  }
}
</style>
