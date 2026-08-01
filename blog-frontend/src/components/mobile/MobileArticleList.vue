<template>
  <div class="m-list">
    <template v-if="loading">
      <div class="m-skel" v-for="n in 3" :key="n">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width:100%;height:150px;border-radius:6px 6px 0 0" />
            <div style="padding:12px 14px">
              <el-skeleton-item variant="h3" style="width:70%" />
              <el-skeleton-item variant="text" style="width:100%;margin-top:10px" />
              <el-skeleton-item variant="text" style="width:50%;margin-top:8px" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </template>
    <template v-else>
      <MobileFeedCard v-for="a in items" :key="a.id" :article="a" />
      <button v-if="!exhausted" class="m-more" :disabled="loadingMore" @click="loadMore">
        {{ loadingMore ? '加载中…' : '加载更多' }}
      </button>
      <div v-if="exhausted && items.length" class="m-end">— 没有更多了 —</div>
      <div v-if="!items.length" class="m-empty">
        <slot name="empty"><p>暂无内容</p></slot>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileFeedCard from './MobileFeedCard.vue'

const props = defineProps({
  fetcher: { type: Function, required: true },
  pageSize: { type: Number, default: 10 }
})

const items = ref([])
const page = ref(0)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const exhausted = ref(false)

async function load(reset = false) {
  if (reset) {
    items.value = []; page.value = 0; total.value = 0; exhausted.value = false
    loading.value = true
  } else {
    loadingMore.value = true
  }
  try {
    const next = page.value + 1
    const res = (await props.fetcher(next)) || {}
    const records = res.records || []
    items.value = reset ? records : items.value.concat(records)
    page.value = next
    total.value = res.total || 0
    if (records.length < props.pageSize || items.value.length >= total.value) exhausted.value = true
  } catch (e) {
    if (reset) exhausted.value = true
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (!loadingMore.value && !exhausted.value) load(false)
}

// 父组件在筛选条件（分类/标签/关键词）变化时调用，重新拉取
function reload() { load(true) }

onMounted(() => load(true))
defineExpose({ reload })
</script>

<style scoped>
.m-list { padding: 0 14px; }
.m-skel {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); overflow: hidden; margin-bottom: 14px;
}
.m-more {
  display: block; width: 100%; margin: 6px 0 18px;
  padding: 12px; border: 1px solid var(--border); border-radius: 100px;
  background: var(--bg-card); color: var(--text-secondary);
  font-size: 13px; cursor: pointer; transition: all .15s;
}
.m-more:active:not(:disabled) { background: var(--bg-warm); color: var(--primary); border-color: var(--primary-light); }
.m-more:disabled { opacity: .6; cursor: default; }
.m-end { text-align: center; color: var(--text-muted); font-size: 12px; padding: 8px 0 22px; }
.m-empty { text-align: center; color: var(--text-muted); font-size: 13px; padding: 48px 0; }
</style>
