<template>
  <div class="comment-form">
    <h3 class="form-title">
      {{ replyTo ? `Reply to: @${replyTo.nickname}` : 'Leave a Comment' }}
      <el-button
        v-if="replyTo"
        text
        size="small"
        type="info"
        @click="$emit('cancel-reply')"
      >
        Cancel Reply
      </el-button>
    </h3>
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      @submit.prevent="handleSubmit"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item prop="nickname">
            <el-input
              v-model="formData.nickname"
              placeholder="Nickname *"
              size="large"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="email">
            <el-input
              v-model="formData.email"
              placeholder="Email *"
              size="large"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item prop="website">
        <el-input
          v-model="formData.website"
          placeholder="Website (optional)"
          size="large"
        />
      </el-form-item>
      <el-form-item prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="4"
          placeholder="Write your comment here... *"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          native-type="submit"
          :loading="submitting"
        >
          Submit Comment
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
  articleId: {
    type: [String, Number],
    required: true
  },
  replyTo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['submitted', 'cancel-reply'])

const formRef = ref(null)
const submitting = ref(false)

const formData = reactive({
  nickname: '',
  email: '',
  website: '',
  content: ''
})

const rules = {
  nickname: [
    { required: true, message: 'Please enter your nickname', trigger: 'blur' },
    { max: 20, message: 'Nickname too long', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' }
  ],
  content: [
    { required: true, message: 'Please enter your comment', trigger: 'blur' },
    { min: 2, message: 'Comment too short', trigger: 'blur' }
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
      ElMessage.success('Comment submitted successfully!')
      formData.nickname = ''
      formData.email = ''
      formData.website = ''
      formData.content = ''
      emit('submitted')
    } catch (e) {
      // error handled by interceptor
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.comment-form {
  margin-top: 40px;
  padding: 24px;
  background: var(--bg-white);
  border-radius: var(--radius-base);
  box-shadow: var(--shadow-light);
}

.form-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
