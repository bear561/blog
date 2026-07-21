<template>
  <div class="content-container">
    <article class="article-detail" v-if="article.id">
      <div class="article-cover" v-if="article.coverImage">
        <el-image :src="article.coverImage" fit="cover" class="cover-img" />
      </div>
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-item"><el-icon><User /></el-icon>Admin</span>
          <span class="meta-item"><el-icon><Clock /></el-icon>{{ formatDate(article.createdAt, 'YYYY-MM-DD HH:mm') }}</span>
          <span class="meta-item" v-if="article.categoryName">
            <el-icon><Folder /></el-icon>
            <router-link :to="`/category/${article.categoryName}`">{{ article.categoryName }}</router-link>
          </span>
          <span class="meta-item"><el-icon><View /></el-icon>{{ article.viewCount || 0 }} 次阅读</span>
        </div>
      </div>
      <div class="article-tags" v-if="article.tags?.length">
        <router-link v-for="tag in article.tags" :key="tag.id" :to="`/tag/${tag.slug || tag}`">
          <el-tag size="default" class="tag-link">{{ tag.name }}</el-tag>
        </router-link>
      </div>
      <ArticleContent :html="article.contentHtml" />
      <el-card class="copyright-card" shadow="never">
        <p>本博客所有文章均采用 CC BY-NC-SA 4.0 许可协议。</p>
      </el-card>
      <div class="comments-section">
        <el-divider content-position="left">
          <span class="comments-title">评论 ({{ comments.length }})</span>
        </el-divider>
        <CommentList :comments="comments" @reply="handleReply" />
        <CommentForm :article-id="article.id" :reply-to="replyTarget" @submitted="fetchComments" @cancel-reply="replyTarget = null" />
      </div>
    </article>
    <div v-if="loading" class="loading-container"><el-skeleton :rows="10" animated /></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { User, Clock, Folder, View } from '@element-plus/icons-vue'
import { getArticle } from '@/api/article'
import { getComments } from '@/api/comment'
import { formatDate } from '@/utils/date'
import ArticleContent from '@/components/ArticleContent.vue'
import CommentList from '@/components/CommentList.vue'
import CommentForm from '@/components/CommentForm.vue'

const route = useRoute()
const article = ref({})
const comments = ref([])
const loading = ref(true)
const replyTarget = ref(null)

async function fetchArticle() {
  loading.value = true
  try { const res = await getArticle(route.params.id); article.value = res.data || {} } catch (e) {} finally { loading.value = false }
}
async function fetchComments() {
  try { const res = await getComments(route.params.id); comments.value = res.data || [] } catch (e) { comments.value = [] }
}
function handleReply(comment) { replyTarget.value = comment }
onMounted(() => { fetchArticle(); fetchComments() })
</script>

<style scoped>
.article-detail { padding: 24px 0; }
.article-cover { margin-bottom: 32px; border-radius: 8px; overflow: hidden; max-height: 420px; }
.cover-img { width: 100%; max-height: 420px; }
.article-header { margin-bottom: 20px; }
.article-title { font-size: 32px; font-weight: 700; line-height: 1.4; color: #303133; margin-bottom: 16px; }
.article-meta { display: flex; flex-wrap: wrap; gap: 20px; }
.meta-item { font-size: 13px; color: #909399; display: flex; align-items: center; gap: 4px; }
.meta-item a { color: #909399; }
.meta-item a:hover { color: #409eff; }
.article-tags { margin-bottom: 32px; padding-bottom: 24px; border-bottom: 1px solid #ebeef5; display: flex; flex-wrap: wrap; gap: 8px; }
.tag-link { cursor: pointer; }
.copyright-card { margin-top: 40px; background: #f5f7fa; border: none; }
.copyright-card p { font-size: 13px; color: #909399; line-height: 2; margin: 0; }
.comments-section { margin-top: 40px; }
.comments-title { font-size: 18px; font-weight: 600; }
.loading-container { padding: 40px 0; }
</style>
