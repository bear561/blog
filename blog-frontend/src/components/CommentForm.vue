<template>
  <div class="comment-form">
    <h3 class="form-title">
      {{ replyTo ? `回复 @${replyTo.nickname}` : '发表评论' }}
      <el-button v-if="replyTo" text size="small" type="info" @click="$emit('cancel-reply')">
        取消回复
      </el-button>
    </h3>
    <el-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="handleSubmit">
      <el-form-item>
        <el-checkbox v-model="formData.isAnonymous">匿名发表</el-checkbox>
      </el-form-item>

      <el-row :gutter="16" v-show="!formData.isAnonymous">
        <el-col :span="12">
          <el-form-item prop="nickname">
            <el-input v-model="formData.nickname" placeholder="昵称 *" size="large" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="email">
            <el-input v-model="formData.email" placeholder="邮箱 (选填)" size="large" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item prop="content">
        <el-input
          v-model="formData.content" type="textarea" :rows="4"
          placeholder="写下你的想法..." maxlength="1000" show-word-limit
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" native-type="submit" :loading="submitting">
          提交评论
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { postComment } from '@/api/comment'

const props = defineProps({
  articleId: { type: [String, Number], required: true },
  replyTo: { type: Object, default: null }
})
const emit = defineEmits(['submitted', 'cancel-reply'])

const formRef = ref(null)
const submitting = ref(false)

const formData = reactive({
  isAnonymous: false,
  nickname: '', email: '', content: ''
})

const rules = {
  nickname: [
    {
      validator: (rule, value, cb) => {
        if (formData.isAnonymous) return cb()
        if (!value || !value.trim()) return cb(new Error('请输入昵称'))
        if (value.length > 20) return cb(new Error('昵称过长'))
        cb()
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (rule, value, cb) => {
        if (!value) return cb()
        const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
        ok ? cb() : cb(new Error('邮箱格式不正确'))
      },
      trigger: 'blur'
    }
  ],
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 2, message: '评论太短了', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const data = {
        articleId: props.articleId,
        isAnonymous: formData.isAnonymous,
        content: formData.content
      }
      if (!formData.isAnonymous) {
        data.nickname = formData.nickname
        if (formData.email) data.email = formData.email
      }
      if (props.replyTo) {
        data.parentId = props.replyTo.id
        data.replyToId = props.replyTo.id
      }
      await postComment(data)
      ElMessage.success('评论成功')
      formData.isAnonymous = false
      formData.nickname = ''
      formData.email = ''
      formData.content = ''
      emit('submitted')
    } catch (e) {} finally { submitting.value = false }
  })
}
</script>

<style scoped>
.comment-form {
  margin-top: 32px; padding: 24px;
  background: var(--bg-warm); border-radius: var(--radius);
  border: 1px solid var(--border);
}
.form-title { font-size: 15px; font-weight: 600; margin-bottom: 18px; color: var(--text); display: flex; align-items: center; gap: 12px; }
</style>
