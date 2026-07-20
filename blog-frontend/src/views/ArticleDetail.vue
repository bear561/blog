<template>
  <el-container>
    <SiteHeader />
    <el-main>
      <div class="content-container">
        <article class="article-detail" v-if="article.id">
          <!-- Cover Image -->
          <div class="article-cover" v-if="article.coverImage">
            <el-image :src="article.coverImage" fit="cover" class="cover-img" />
          </div>

          <!-- Header meta -->
          <div class="article-header">
            <h1 class="article-title">{{ article.title }}</h1>
            <div class="article-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ article.author || 'Admin' }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatDate(article.createdAt || article.createTime, 'YYYY-MM-DD HH:mm') }}
              </span>
              <span class="meta-item" v-if="article.category">
                <el-icon><Folder /></el-icon>
                <router-link :to="`/category/${article.category.slug || article.category}`">
                  {{ article.category.name || article.category }}
                </router-link>
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ article.viewCount || article.views || 0 }} views
              </span>
            </div>
          </div>

          <!-- Tags -->
          <div class="article-tags" v-if="article.tags && article.tags.length">
            <router-link
              v-for="tag in article.tags"
              :key="tag.id || tag"
              :to="`/tag/${tag.slug || tag}`"
            >
              <el-tag size="default" class="tag-link">{{ tag.name || tag }}</el-tag>
            </router-link>
          </div>

          <!-- Content -->
          <ArticleContent
            :html="article.contentHtml || article.content"
            :markdown="article.contentMd || article.contentMarkdown"
          />

          <!-- Copyright -->
          <el-card class="copyright-card" shadow="never">
            <p>Author: {{ article.author || 'Admin' }}</p>
            <p>Original link: {{ currentUrl }}</p>
            <p>Copyright: All articles are licensed under CC BY-NC-SA 4.0 unless otherwise stated.</p>
          </el-card>

          <!-- Comments -->
          <div class="comments-section">
            <el-divider content-position="left">
              <span class="comments-title">
                Comments ({{ comments.length }})
              </span>
            </el-divider>

            <CommentList
              :comments="comments"
              @reply="handleReply"
            />

            <CommentForm
              :article-id="article.id"
              :reply-to="replyTarget"
              @submitted="fetchComments"
              @cancel-reply="replyTarget = null"
            />
          </div>
        </article>

        <!-- Loading -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>
      </div>
    </el-main>
    <SiteFooter />
    <AIChatWidget />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { User, Clock, Folder, View } from '@element-plus/icons-vue'
import { getArticle } from '@/api/article'
import { getComments } from '@/api/comment'
import { formatDate } from '@/utils/date'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ArticleContent from '@/components/ArticleContent.vue'
import CommentList from '@/components/CommentList.vue'
import CommentForm from '@/components/CommentForm.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'

const route = useRoute()

const article = ref({})
const comments = ref([])
const loading = ref(true)
const replyTarget = ref(null)

const currentUrl = computed(() => window.location.href)

async function fetchArticle() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getArticle(id)
    article.value = res.data || res
  } catch (e) {
    article.value = {}
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  try {
    const id = route.params.id
    const res = await getComments(id)
    comments.value = res.data || []
  } catch (e) {
    comments.value = []
  }
}

function handleReply(comment) {
  replyTarget.value = comment
  // Scroll to comment form
  const el = document.querySelector('.comment-form')
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(() => {
  fetchArticle()
  fetchComments()
})
</script>

<style scoped>
.article-detail {
  padding: 24px 0;
}

.article-cover {
  margin-bottom: 32px;
  border-radius: var(--radius-base);
  overflow: hidden;
  max-height: 420px;
}

.cover-img {
  width: 100%;
  max-height: 420px;
}

.article-header {
  margin-bottom: 20px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.meta-item {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item a {
  color: var(--text-secondary);
}

.meta-item a:hover {
  color: var(--primary-color);
}

.article-tags {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-link {
  cursor: pointer;
}

.copyright-card {
  margin-top: 40px;
  background: var(--bg-color);
  border: none;
}

.copyright-card p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 2;
  margin: 0;
}

.comments-section {
  margin-top: 40px;
}

.comments-title {
  font-size: 18px;
  font-weight: 600;
}

.loading-container {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .article-title {
    font-size: 24px;
  }

  .article-meta {
    gap: 12px;
  }
}
</style>
