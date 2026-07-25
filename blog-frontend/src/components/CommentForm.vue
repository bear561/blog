<template>
  <div class="comment-form">
    <h3 class="form-title">
      {{ replyTo ? `回复 @${replyTo.nickname}` : '发表评论' }}
      <el-button v-if="replyTo" text size="small" type="info" @click="$emit('cancel-reply')">
        取消回复
      </el-button>
    </h3>
    <el-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="handleSubmit">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item prop="nickname">
            <el-input v-model="formData.nickname" placeholder="昵称 *" size="large" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="email">
            <el-input v-model="formData.email" placeholder="邮箱 *" size="large" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item prop="website">
        <el-input v-model="formData.website" placeholder="网站 (选填)" size="large" />
      </el-form-item>
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
  nickname: '', email: '', website: '', content: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称过长', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
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
        nickname: formData.nickname,
        email: formData.email,
        website: formData.website,
        content: formData.content
      }
      if (props.replyTo) {
        data.parentId = props.replyTo.id
      }
      await postComment(data)
      ElMessage.success('评论成功')
      formData.nickname = ''; formData.email = ''; formData.website = ''; formData.content = ''
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
