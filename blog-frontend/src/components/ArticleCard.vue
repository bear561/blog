<template>
  <el-card class="article-card" :body-style="{ padding: '0' }" shadow="hover">
    <div class="card-cover" v-if="article.coverImage">
      <router-link :to="`/article/${article.id}`">
        <el-image
          :src="article.coverImage"
          fit="cover"
          class="cover-image"
          lazy
        >
          <template #error>
            <div class="image-placeholder">
              <el-icon :size="48"><PictureFilled /></el-icon>
            </div>
          </template>
        </el-image>
      </router-link>
    </div>
    <div class="card-body">
      <div class="card-meta">
        <span class="meta-category" v-if="article.category">
          <router-link :to="`/category/${article.category.slug || article.category}`">
            <el-tag size="small" type="primary" effect="plain">
              {{ article.category.name || article.category }}
            </el-tag>
          </router-link>
        </span>
        <span class="meta-date">
          <el-icon :size="14"><Clock /></el-icon>
          {{ formatDate(article.createdAt || article.createTime, 'YYYY-MM-DD') }}
        </span>
      </div>
      <h2 class="card-title">
        <router-link :to="`/article/${article.id}`">
          {{ article.title }}
        </router-link>
      </h2>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-footer">
        <div class="footer-tags" v-if="article.tags && article.tags.length">
          <router-link
            v-for="tag in article.tags"
            :key="tag.id || tag"
            :to="`/tag/${tag.slug || tag}`"
          >
            <el-tag size="small" class="tag-item">
              {{ tag.name || tag }}
            </el-tag>
          </router-link>
        </div>
        <div class="footer-stats">
          <span class="stat-item">
            <el-icon :size="14"><View /></el-icon>
            {{ article.viewCount || article.views || 0 }}
          </span>
          <span class="stat-item">
            <el-icon :size="14"><ChatLineSquare /></el-icon>
            {{ article.commentCount || article.comments || 0 }}
          </span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { Clock, View, ChatLineSquare, PictureFilled } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/date'

defineProps({
  article: {
    type: Object,
    required: true,
    default: () => ({})
  }
})
</script>

<style scoped>
.article-card {
  border-radius: var(--radius-base);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.article-card:hover {
  transform: translateY(-2px);
}

.card-cover {
  height: 200px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.article-card:hover .cover-image {
  transform: scale(1.05);
}

.image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.card-body {
  padding: 20px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.meta-date {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 10px;
}

.card-title a {
  color: var(--text-primary);
}

.card-title a:hover {
  color: var(--primary-color);
}

.card-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-item {
  cursor: pointer;
}

.footer-stats {
  display: flex;
  gap: 14px;
}

.stat-item {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
