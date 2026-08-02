<template>
  <div class="m-home">
    <MobileAppBar />

    <!-- 头条轮播：固定为最新 5 篇，独立于下方筛选 -->
    <div v-if="firstLoad && !hero.length" class="m-hero-skel">
      <el-skeleton animated><template #template><el-skeleton-item variant="image" style="width:100%;height:200px" /></template></el-skeleton>
    </div>
    <MobileHeroCarousel :items="hero" />

    <!-- 小组件：每日一句 + 作者卡（标签云已升级为筛选，故此处移除） -->
    <div class="m-home__disc">
      <DailyQuote />
      <section class="m-disc m-aboutcard" v-if="siteName || siteDesc">
        <img v-if="siteAvatar" :src="siteAvatar" class="m-aboutcard__av-img" alt="avatar" />
        <div v-else class="m-aboutcard__av">{{ (siteName || 'B').trim().charAt(0) }}</div>
        <div class="m-aboutcard__txt">
          <h4 class="m-aboutcard__name">{{ siteName || 'Blog' }}</h4>
          <p class="m-aboutcard__desc" v-if="siteDesc">{{ siteDesc }}</p>
        </div>
      </section>
    </div>

    <!-- 吸顶筛选条：分类 + 标签抽屉 + 排序 -->
    <div class="m-filter">
      <div class="m-filter__cats">
        <span class="m-chip2" :class="{ on: selCat === '' }" @click="selectCat('')">全部</span>
        <span
          class="m-chip2"
          v-for="c in categories"
          :key="c.id"
          :class="{ on: selCat === c.slug }"
          @click="selectCat(c.slug)"
        >{{ c.name }}</span>
      </div>
      <div class="m-filter__row">
        <div class="m-filter__left">
          <button class="m-fbtn" :class="{ on: !!selTag }" @click="tagSheetOpen = true">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.1"/></svg>
            {{ selTag ? tagName(selTag) : '标签' }}
          </button>
          <span v-if="selTag" class="m-fbtn__x" @click="clearTag" aria-label="清除标签">✕</span>
        </div>
        <button class="m-fbtn" @click="cycleSort">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18M6 12h12M10 18h4"/></svg>
          {{ SORTS[sortMode].label }}
        </button>
      </div>
    </div>

    <!-- 受控分页列表 -->
    <div class="m-home__feed">
      <template v-if="firstLoad">
        <div class="m-skel" v-for="n in 2" :key="n">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width:100%;height:150px;border-radius:6px 6px 0 0" />
              <div style="padding:12px 14px">
                <el-skeleton-item variant="h3" style="width:70%" />
                <el-skeleton-item variant="text" style="width:90%;margin-top:10px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </template>
      <template v-else>
        <MobileFeedCard v-for="a in feed" :key="a.id" :article="a" />
        <button v-if="!exhausted" class="m-more" :disabled="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中…' : '加载更多' }}
        </button>
        <div v-if="exhausted && (feed.length || hero.length)" class="m-end">— 已展示全部 {{ total }} 篇 —</div>
        <div v-if="!feed.length" class="m-empty">
          <p>{{ (selCat || selTag) ? '没有符合条件的文章' : '还没有文章，去后台写第一篇吧 ✍️' }}</p>
        </div>
      </template>
    </div>

    <!-- 标签筛选抽屉 -->
    <transition name="sheet">
      <div v-if="tagSheetOpen" class="m-sheet-mask" @click="tagSheetOpen = false">
        <div class="m-sheet" @click.stop>
          <div class="m-sheet__h">
            <span>标签筛选</span>
            <span class="m-sheet__clr" @click="clearTag">清除</span>
          </div>
          <div class="m-sheet__tags">
            <span class="m-chip2" :class="{ on: selTag === '' }" @click="selectTag('')">全部</span>
            <span
              class="m-chip2"
              v-for="t in tags"
              :key="t.id"
              :class="{ on: selTag === t.slug }"
              @click="selectTag(t.slug)"
            >{{ t.name }}</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { getSiteConfig } from '@/api/siteConfig'
import MobileAppBar from '@/components/mobile/MobileAppBar.vue'
import MobileHeroCarousel from '@/components/mobile/MobileHeroCarousel.vue'
import MobileFeedCard from '@/components/mobile/MobileFeedCard.vue'
import DailyQuote from '@/components/DailyQuote.vue'

const PAGE = 10
const SORTS = [
  { label: '最新', sortBy: 'createdAt', order: 'desc' },
  { label: '最早', sortBy: 'createdAt', order: 'asc' },
  { label: '最热', sortBy: 'viewCount', order: 'desc' }
]

const hero = ref([])
const feed = ref([])
const categories = ref([])
const tags = ref([])
const siteName = ref('')
const siteDesc = ref('')
const siteAvatar = ref('')
const page = ref(1)
const total = ref(0)
const exhausted = ref(false)
const loadingMore = ref(false)
const firstLoad = ref(true)

const selCat = ref('')
const selTag = ref('')
const sortMode = ref(0)
const tagSheetOpen = ref(false)

function tagName(slug) {
  const t = tags.value.find(x => x.slug === slug)
  return t ? t.name : slug
}

// 头条轮播：固定拉最新 5 篇，不受筛选/排序影响
async function loadLatest() {
  try {
    const r = (await getArticles({ page: 1, pageSize: 5 })).data || {}
    hero.value = (r.records || []).slice(0, 5)
  } catch (e) { /* ignore */ }
}

// 下方列表：按 分类+标签+排序 对“所有文章”做分页查询
async function fetchPage(p, reset = false) {
  const s = SORTS[sortMode.value]
  const params = { page: p, pageSize: PAGE, sortBy: s.sortBy, order: s.order }
  if (selCat.value) params.categorySlug = selCat.value
  if (selTag.value) params.tagSlug = selTag.value
  const r = (await getArticles(params)).data || {}
  const recs = r.records || []
  if (reset) {
    feed.value = recs
    page.value = 1
  } else {
    feed.value = feed.value.concat(recs)
    page.value = p
  }
  total.value = r.total || 0
  if (recs.length < PAGE || feed.value.length >= total.value) exhausted.value = true
}

async function reload() {
  exhausted.value = false
  firstLoad.value = true
  try { await fetchPage(1, true) } catch (e) { /* ignore */ } finally { firstLoad.value = false }
}

async function loadMore() {
  if (loadingMore.value || exhausted.value) return
  loadingMore.value = true
  try { await fetchPage(page.value + 1, false) } catch (e) { /* ignore */ } finally { loadingMore.value = false }
}

function selectCat(slug) { selCat.value = (selCat.value === slug ? '' : slug); reload() }
function selectTag(slug) { selTag.value = slug; tagSheetOpen.value = false; reload() }
function clearTag() { selTag.value = ''; tagSheetOpen.value = false; reload() }
function cycleSort() { sortMode.value = (sortMode.value + 1) % SORTS.length; reload() }

onMounted(() => {
  loadLatest()
  reload()
  getCategories().then(r => { categories.value = r.data || [] }).catch(() => {})
  getTags().then(r => { tags.value = r.data || [] }).catch(() => {})
  getSiteConfig().then(r => {
    const c = r.data || {}
    siteName.value = c.site_name || ''
    siteDesc.value = c.site_description || ''
    siteAvatar.value = c.site_avatar || ''
  }).catch(() => {})
})
</script>

<style scoped>
.m-home__disc { padding: 12px 14px 0; display: flex; flex-direction: column; gap: 14px; }
.m-disc { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); padding: 14px 16px; }
.m-aboutcard { display: flex; align-items: center; gap: 12px; }
.m-aboutcard__av {
  width: 44px; height: 44px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff; font-family: var(--font-serif); font-weight: 700; font-size: 18px;
  display: flex; align-items: center; justify-content: center;
}
.m-aboutcard__av-img {
  width: 44px; height: 44px; border-radius: 50%; flex-shrink: 0; object-fit: cover;
}
.m-aboutcard__txt { min-width: 0; }
.m-aboutcard__name { font-size: 14px; font-weight: 600; color: var(--text); }
.m-aboutcard__desc {
  font-size: 12px; color: var(--text-muted); margin-top: 2px; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

/* 吸顶筛选条 */
.m-filter {
  position: sticky; top: var(--header-height); z-index: 90;
  background: var(--bg); border-bottom: 1px solid var(--border);
  padding: 8px 14px 10px;
}
.m-filter__cats {
  display: flex; gap: 8px; overflow-x: auto; padding-bottom: 8px;
  scrollbar-width: none;
}
.m-filter__cats::-webkit-scrollbar { display: none; }
.m-filter__row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.m-filter__left { display: flex; align-items: center; gap: 6px; min-width: 0; }
.m-chip2 {
  flex-shrink: 0; font-size: 13px; padding: 6px 14px; border-radius: 100px;
  background: var(--bg-warm); color: var(--text-secondary); white-space: nowrap;
  cursor: pointer; transition: all .15s;
}
.m-chip2.on { background: var(--primary); color: #fff; }
.m-chip2:not(.on):active { background: #fdf0eb; color: var(--primary); }
.m-fbtn {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 12.5px; color: var(--text-secondary);
  background: var(--bg-card); border: 1px solid var(--border);
  padding: 5px 11px; border-radius: 100px; cursor: pointer; transition: all .15s;
  max-width: 46vw; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.m-fbtn svg { flex-shrink: 0; }
.m-fbtn.on { color: var(--primary); border-color: var(--primary-light); background: #fdf0eb; }
.m-fbtn:active { transform: scale(.97); }
.m-fbtn__x {
  width: 20px; height: 20px; border-radius: 50%; flex-shrink: 0;
  display: inline-flex; align-items: center; justify-content: center;
  background: var(--bg-warm); color: var(--text-muted); font-size: 11px; cursor: pointer;
}

.m-hero-skel { margin: 0 14px 12px; border-radius: var(--radius); overflow: hidden; }
.m-home__feed { padding: 14px 14px 4px; }
.m-skel {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); overflow: hidden; margin-bottom: 14px;
}
.m-more {
  display: block; width: 100%; margin: 6px 0 18px; padding: 12px;
  border: 1px solid var(--border); border-radius: 100px; background: var(--bg-card);
  color: var(--text-secondary); font-size: 13px; cursor: pointer; transition: all .15s;
}
.m-more:active:not(:disabled) { background: var(--bg-warm); color: var(--primary); border-color: var(--primary-light); }
.m-more:disabled { opacity: .6; }
.m-end { text-align: center; color: var(--text-muted); font-size: 12px; padding: 8px 0 22px; }
.m-empty { text-align: center; color: var(--text-muted); font-size: 13px; padding: 48px 0; }

/* 标签抽屉 */
.m-sheet-mask {
  position: fixed; inset: 0; z-index: 1550;
  background: rgba(26, 16, 12, .4);
  display: flex; align-items: flex-end;
}
.m-sheet {
  width: 100%; background: var(--bg-card);
  border-radius: 16px 16px 0 0; padding: 16px 16px calc(16px + env(safe-area-inset-bottom));
  max-height: 70vh; overflow-y: auto;
}
.m-sheet__h { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.m-sheet__h span:first-child { font-size: 15px; font-weight: 600; color: var(--text); }
.m-sheet__clr { font-size: 13px; color: var(--primary); cursor: pointer; }
.m-sheet__tags { display: flex; flex-wrap: wrap; gap: 8px; }

.sheet-enter-active, .sheet-leave-active { transition: opacity .25s ease; }
.sheet-enter-active .m-sheet, .sheet-leave-active .m-sheet { transition: transform .3s cubic-bezier(.22, .61, .36, 1); }
.sheet-enter-from, .sheet-leave-to { opacity: 0; }
.sheet-enter-from .m-sheet, .sheet-leave-to .m-sheet { transform: translateY(100%); }
</style>
