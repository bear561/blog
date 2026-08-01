<template>
  <div class="content-container">
    <article class="article-detail" v-if="article.id">
      <div class="article-cover" v-if="article.coverImage">
        <img :src="article.coverImage" alt="" class="cover-img" />
      </div>

      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span>Admin</span>
          <span class="meta-sep">·</span>
          <span>{{ formatDate(article.createdAt, 'YYYY-MM-DD') }}</span>
          <span class="meta-sep" v-if="article.categoryName">·</span>
          <router-link v-if="article.categoryName" :to="`/category/${article.categoryName}`" class="meta-link">
            {{ article.categoryName }}
          </router-link>
          <span class="meta-sep">·</span>
          <span>{{ article.viewCount || 0 }} 次阅读</span>
          <span class="meta-sep">·</span>
          <span>约 {{ readMin }} 分钟</span>
        </div>
      </div>

      <div class="article-tags" v-if="article.tags?.length">
        <router-link v-for="tag in article.tags" :key="tag.id" :to="`/tag/${tag.slug || tag}`">
          <span class="tag-pill">{{ tag.name }}</span>
        </router-link>
      </div>

      <ArticleContent :html="article.contentHtml" />

      <div class="copyright-card">
        <p>本文采用 CC BY-NC-SA 4.0 许可协议，转载请注明出处。</p>
      </div>

      <div class="comments-section">
        <h3 class="comments-title">评论 ({{ comments.length }})</h3>
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
import { getArticle } from '@/api/article'
import { getComments } from '@/api/comment'
import { formatDate } from '@/utils/date'
import { readingTime } from '@/utils/readingTime'
import ArticleContent from '@/components/ArticleContent.vue'
import CommentList from '@/components/CommentList.vue'
import CommentForm from '@/components/CommentForm.vue'

const route = useRoute()
const article = ref({})
const comments = ref([])
const loading = ref(true)
const replyTarget = ref(null)

const readMin = computed(() => readingTime(article.value.contentHtml || ''))

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
.article-detail { padding: 32px 0; }

.article-cover { margin-bottom: 36px; overflow: hidden; max-height: 480px; }
.article-cover img { width: 100%; height: auto; max-height: 480px; object-fit: cover; }

.article-header { margin-bottom: 16px; }
.article-title {
  font-size: 34px; font-weight: 700; line-height: 1.4; color: var(--text);
  margin-bottom: 16px; letter-spacing: -.02em;
}
.article-meta {
  display: flex; flex-wrap: wrap; align-items: center; gap: 6px;
  font-size: 13px; color: var(--text-muted);
}
.meta-sep { color: var(--border); }
.meta-link { color: var(--text-secondary); }
.meta-link:hover { color: var(--primary); }

.article-tags {
  display: flex; flex-wrap: wrap; gap: 8px;
  margin-bottom: 36px; padding-bottom: 24px;
  border-bottom: 1px solid var(--border);
}
.tag-pill {
  font-size: 12px; color: var(--text-secondary); background: var(--bg-warm);
  padding: 3px 12px; border-radius: 100px; transition: all .15s;
}
.tag-pill:hover { color: var(--primary); background: #fdf0eb; }

.copyright-card {
  margin-top: 48px; padding: 20px 0;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  text-align: center;
}
.copyright-card p { font-size: 13px; color: var(--text-muted); margin: 0; }

.comments-section { margin-top: 48px; }
.comments-title { font-size: 17px; font-weight: 600; color: var(--text); margin-bottom: 8px; }
.loading-container { padding: 40px 0; }
</style>
