<template>
  <div class="comment-list">
    <div v-if="!comments || comments.length === 0" class="empty-comments">
      <el-empty description="暂无评论，来说点什么吧" />
    </div>
    <div v-else class="comments-container">
      <div v-for="root in comments" :key="root.id" class="thread">
        <!-- 顶层评论 -->
        <div class="comment-item">
          <div class="comment-avatar">
            <el-avatar :size="40" :src="root.avatar">
              {{ root.nickname?.charAt(0)?.toUpperCase() || 'U' }}
            </el-avatar>
          </div>
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-nickname">
                {{ root.nickname }}
                <span v-if="root.isAdmin" class="admin-tag">博主</span>
              </span>
              <span class="comment-time">{{ timeAgo(root.createdAt || root.createTime) }}</span>
            </div>
            <div class="comment-content">{{ root.content }}</div>
            <div class="comment-actions">
              <el-button text size="small" type="primary" @click="$emit('reply', root)">回复</el-button>
            </div>
          </div>
        </div>

        <!-- 扁平楼中楼：所有回复平铺在顶层评论下，不无限缩进 -->
        <div v-if="flattenReplies(root).length" class="reply-thread">
          <div v-for="reply in visibleReplies(root)" :key="reply.id" class="reply-item">
            <div class="reply-body">
              <div class="reply-header">
                <span class="reply-nickname">
                  {{ reply.nickname }}
                  <span v-if="reply.isAdmin" class="admin-tag">博主</span>
                </span>
                <span v-if="replyToName(reply)" class="reply-target">回复 @{{ replyToName(reply) }}</span>
                <span class="comment-time">{{ timeAgo(reply.createdAt || reply.createTime) }}</span>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
              <div class="comment-actions">
                <el-button text size="small" type="primary" @click="$emit('reply', reply)">回复</el-button>
              </div>
            </div>
          </div>
          <div v-if="collapsedCount(root) > 0" class="more-replies">
            <el-button text size="small" @click="toggleExpand(root.id)">
              展开其余 {{ collapsedCount(root) }} 条回复
              <el-icon class="el-icon--right" :class="{ expanded: isExpanded(root.id) }"><ArrowDown /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { timeAgo } from '@/utils/date'

const props = defineProps({
  comments: { type: Array, default: () => [] }
})
defineEmits(['reply'])

// 每条顶层评论默认展示的回复条数，其余折叠
const REPLIES_SHOWN = 3
const expandedRoots = reactive(new Set())

// 把某条评论的所有后代回复扁平化成单层数组（BFS，保持先后顺序）
function flattenReplies(comment) {
  const out = []
  const stack = comment.replies ? [...comment.replies] : []
  while (stack.length) {
    const c = stack.shift()
    out.push(c)
    if (c.replies && c.replies.length) stack.push(...c.replies)
  }
  return out
}

// 全量评论的 id -> 昵称 映射，用于解析“回复 @谁”
const nicknameMap = computed(() => {
  const map = {}
  const walk = (list) => {
    for (const c of list || []) {
      map[c.id] = c.nickname
      if (c.replies && c.replies.length) walk(c.replies)
    }
  }
  walk(props.comments)
  return map
})

function replyToName(reply) {
  return nicknameMap.value[reply.replyToId] || nicknameMap.value[reply.parentId] || ''
}

function visibleReplies(root) {
  const all = flattenReplies(root)
  if (expandedRoots.has(root.id)) return all
  return all.slice(0, REPLIES_SHOWN)
}

function collapsedCount(root) {
  const n = flattenReplies(root).length
  return expandedRoots.has(root.id) ? 0 : Math.max(0, n - REPLIES_SHOWN)
}

function isExpanded(id) {
  return expandedRoots.has(id)
}

function toggleExpand(id) {
  if (expandedRoots.has(id)) expandedRoots.delete(id)
  else expandedRoots.add(id)
}
</script>

<style scoped>
.comment-list { margin-top: 24px; }
.comments-container { display: flex; flex-direction: column; }

.thread { margin-bottom: 8px; }

.comment-item {
  display: flex; gap: 12px;
  padding: 18px 0;
  border-bottom: 1px solid var(--border);
}

.comment-avatar { flex-shrink: 0; }
.comment-body { flex: 1; min-width: 0; }

.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.comment-nickname { font-size: 14px; font-weight: 600; color: var(--text); }
.admin-tag {
  margin-left: 6px; font-size: 10px; font-weight: 500;
  color: #fff; background: var(--accent);
  padding: 1px 6px; border-radius: 3px;
}
.comment-time { font-size: 12px; color: var(--text-muted); }
.comment-content {
  font-size: 14px; line-height: 1.75; color: var(--text-secondary);
  margin-bottom: 6px; word-break: break-word;
}
.comment-actions { display: flex; gap: 8px; }

/* —— 扁平楼中楼 —— */
.reply-thread {
  margin-left: 52px;             /* 对齐顶层评论正文 */
  border-left: 2px solid var(--border);
  padding-left: 16px;
  margin-top: 4px;
}
.reply-item { padding: 12px 0; }
.reply-item + .reply-item { border-top: 1px dashed var(--border); }
.reply-body { min-width: 0; }

.reply-header {
  display: flex; align-items: baseline; flex-wrap: wrap; gap: 8px;
  margin-bottom: 4px;
}
.reply-nickname { font-size: 13px; font-weight: 600; color: var(--text); }
.reply-target { font-size: 12px; color: var(--primary); }
.reply-content {
  font-size: 14px; line-height: 1.7; color: var(--text-secondary);
  word-break: break-word;
}

.more-replies { padding: 8px 0; }
.more-replies .el-icon { transition: transform .2s; }
.more-replies .el-icon.expanded { transform: rotate(180deg); }

/* 移动端窄屏：回复标题换行，减少缩进 */
@media (max-width: 480px) {
  .reply-thread { margin-left: 12px; padding-left: 12px; }
}
</style>