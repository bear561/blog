<template>
  <div class="m-art" v-if="article.id">
    <MobileAppBar back hide-search />
    <div class="m-art__cover" v-if="article.coverImage"><img :src="article.coverImage" alt="" /></div>
    <div class="m-art__body">
      <h1 class="m-art__title">{{ article.title }}</h1>
      <div class="m-art__meta">
        <router-link v-if="article.categoryName" :to="`/category/${article.categoryName}`" class="m-art__cat">{{ article.categoryName }}</router-link>
        <span>{{ formatDate(article.createdAt, 'YYYY-MM-DD') }}</span>
        <span class="dot">·</span><span>{{ readMin }} 分钟</span>
        <span class="dot">·</span><span>{{ article.viewCount || 0 }} 阅读</span>
      </div>
      <div class="m-art__tags" v-if="article.tags && article.tags.length">
        <router-link v-for="t in article.tags" :key="t.id" :to="`/tag/${t.slug || t}`" class="m-tag">{{ t.name }}</router-link>
      </div>

      <div ref="articleRef" class="m-reader"><ArticleContent :html="article.contentHtml" /></div>

      <div class="m-art__cc"><p>本文采用 CC BY-NC-SA 4.0 许可协议，转载请注明出处。</p></div>

      <section class="m-art__comments">
        <h3>评论 ({{ comments.length }})</h3>
        <CommentList :comments="comments" @reply="replyTarget = $event" />
        <CommentForm :article-id="article.id" :reply-to="replyTarget" @submitted="fetchComments" @cancel-reply="replyTarget = null" />
      </section>
    </div>
  </div>
  <div v-if="loading" class="m-art__load"><el-skeleton :rows="10" animated /></div>

  <!-- 右下角固定目录按钮，滚动时始终可见 -->
  <button v-if="tocHeadings.length >= 2" class="m-toc-fab" @click="drawerOpen = true">
    <el-icon :size="18"><Memo /></el-icon>
    <span>目录</span>
  </button>

  <el-drawer v-model="drawerOpen" title="目录" size="76%" class="m-toc-drawer">
    <div class="m-toc-list">
      <div
        v-for="(h, i) in tocHeadings"
        :key="i"
        class="m-toc-link"
        :class="{ 'is-h3': h.level === 3 }"
        @click="tocJump(i)"
      >{{ h.text }}</div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '@/api/article'
import { getComments } from '@/api/comment'
import { formatDate } from '@/utils/date'
import { readingMinutesFrom } from '@/utils/readingTime'
import { extractHeadings } from '@/utils/toc'
import { Memo } from '@element-plus/icons-vue'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import ArticleContent from '@/components/ArticleContent.vue'
import CommentList from '@/components/CommentList.vue'
import CommentForm from '@/components/CommentForm.vue'

const route = useRoute()
const article = ref({})
const comments = ref([])
const loading = ref(true)
const replyTarget = ref(null)
const articleRef = ref(null)
const drawerOpen = ref(false)
const readMin = computed(() => readingMinutesFrom(article.value))
const tocHeadings = computed(() => extractHeadings(article.value.contentHtml || ''))

function tocJump(i) {
  drawerOpen.value = false
  const el = articleRef.value?.querySelectorAll('h2, h3')[i]
  if (!el) return
  const top = el.getBoundingClientRect().top + window.scrollY - 70
  window.scrollTo({ top, behavior: 'smooth' })
}

async function fetchArticle() {
  loading.value = true
  try { article.value = (await getArticle(route.params.id)).data || {} } catch (e) { /* ignore */ } finally { loading.value = false }
}
async function fetchComments() {
  try { comments.value = (await getComments(route.params.id)).data || [] } catch (e) { comments.value = [] }
}

onMounted(() => { fetchArticle(); fetchComments() })
</script>

<style scoped>
.m-art__cover { width: 100%; height: 52vw; min-height: 200px; max-height: 320px; overflow: hidden; background: var(--bg-warm); }
.m-art__cover img { width: 100%; height: 100%; object-fit: cover; filter: sepia(.06); }
.m-art__body { padding: 16px 16px 32px; }
.m-art__title { font-family: var(--font-serif); font-size: 25px; font-weight: 700; line-height: 1.35; color: var(--text); letter-spacing: -.01em; margin-bottom: 12px; }
.m-art__meta { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; font-size: 12px; color: var(--text-muted); margin-bottom: 14px; }
.m-art__cat { color: var(--text-secondary); }
.dot { color: var(--border); }
.m-art__tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 18px; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
.m-tag { font-size: 12px; color: var(--text-secondary); background: var(--bg-warm); padding: 3px 12px; border-radius: 100px; }

/* 右下角固定目录按钮，滚动时始终可见 */
.m-toc-fab {
  position: fixed; right: 16px; bottom: 150px; z-index: 100;
  display: flex; align-items: center; gap: 5px;
  padding: 10px 14px;
  background: var(--primary); color: #fff;
  border: none; border-radius: 100px;
  font-size: 13px; font-weight: 600;
  box-shadow: 0 2px 10px rgba(0,0,0,.2);
  cursor: pointer;
}
.m-toc-fab:active { opacity: .85; }

/* 抽屉内目录列表 */
.m-toc-list { padding: 4px 0; }
.m-toc-link {
  padding: 10px 12px; font-size: 14px; line-height: 1.5;
  color: var(--text-secondary); border-radius: 8px;
}
.m-toc-link.is-h3 { padding-left: 26px; font-size: 13px; }
.m-toc-link:active { background: var(--bg-warm); color: var(--primary); }

/* 移动阅读字号（父级 :deep 调优 ArticleContent） */
.m-reader :deep(.article-content) { font-size: 16.5px; line-height: 1.85; }
.m-reader :deep(.article-content h1) { font-size: 22px; }
.m-reader :deep(.article-content h2) { font-size: 19px; }
.m-reader :deep(.article-content h3) { font-size: 17px; }
.m-reader :deep(.article-content > p:first-of-type::first-letter) { font-size: 3em; }

.m-art__cc { margin-top: 28px; padding: 16px 0; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); text-align: center; }
.m-art__cc p { font-size: 12px; color: var(--text-muted); margin: 0; }
.m-art__comments { margin-top: 24px; }
.m-art__comments h3 { font-size: 16px; font-weight: 600; margin-bottom: 10px; }
.m-art__load { padding: 24px 16px; }
</style>
