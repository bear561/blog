<template>
  <div class="comment-list">
    <div v-if="!comments || comments.length === 0" class="empty-comments">
      <el-empty description="No comments yet. Be the first to comment!" />
    </div>
    <div v-else class="comments-container">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
        :class="{ 'is-reply': comment.parentId }"
      >
        <div class="comment-avatar">
          <el-avatar :size="40" :src="comment.avatar">
            {{ comment.nickname?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
        </div>
        <div class="comment-body">
          <div class="comment-header">
            <span class="comment-nickname">
              {{ comment.nickname }}
              <el-tag v-if="comment.isAdmin" size="small" type="danger" class="admin-tag">Admin</el-tag>
            </span>
            <span class="comment-time">{{ timeAgo(comment.createdAt || comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-actions">
            <el-button
              text
              size="small"
              type="primary"
              @click="$emit('reply', comment)"
            >
              <el-icon><ChatLineSquare /></el-icon>
              Reply
            </el-button>
          </div>
          <!-- Nested replies -->
          <div v-if="comment.children && comment.children.length" class="nested-comments">
            <CommentList
              :comments="comment.children"
              @reply="$emit('reply', $event)"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ChatLineSquare } from '@element-plus/icons-vue'
import { timeAgo } from '@/utils/date'

defineProps({
  comments: {
    type: Array,
    default: () => []
  }
})

defineEmits(['reply'])
</script>

<style scoped>
.comment-list {
  margin-top: 32px;
}

.comments-container {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item.is-reply {
  margin-left: 52px;
  padding-left: 0;
  background: rgba(245, 247, 250, 0.5);
  border-radius: var(--radius);
  padding: 12px 16px;
  margin-top: 8px;
  border-bottom: none;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-nickname {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.admin-tag {
  margin-left: 6px;
  font-size: 10px;
}

.comment-time {
  font-size: 12px;
  color: var(--text-muted);
}

.comment-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.nested-comments {
  margin-top: 8px;
}
</style>
