import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue')
  },
  {
    path: '/category/:slug',
    name: 'CategoryArticles',
    component: () => import('@/views/CategoryArticles.vue')
  },
  {
    path: '/tag/:slug',
    name: 'TagArticles',
    component: () => import('@/views/TagArticles.vue')
  },
  {
    path: '/archive',
    name: 'Archive',
    component: () => import('@/views/Archive.vue')
  },
  {
    path: '/friend-links',
    name: 'FriendLinks',
    component: () => import('@/views/FriendLinks.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
