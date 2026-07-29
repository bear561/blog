import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/admin/dashboard'
  },
  {
    path: '/admin/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin',
    redirect: '/admin/dashboard'
  },
  {
    path: '/admin/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '仪表盘', requiresAuth: true }
  },
  {
    path: '/admin/articles',
    name: 'ArticleList',
    component: () => import('@/views/ArticleList.vue'),
    meta: { title: '文章管理', requiresAuth: true }
  },
  {
    path: '/admin/articles/edit/:id?',
    name: 'ArticleEdit',
    component: () => import('@/views/ArticleEdit.vue'),
    meta: { title: '编辑文章', requiresAuth: true }
  },
  {
    path: '/admin/comments',
    name: 'CommentList',
    component: () => import('@/views/CommentList.vue'),
    meta: { title: '评论管理', requiresAuth: true }
  },
  {
    path: '/admin/categories',
    name: 'CategoryManage',
    component: () => import('@/views/CategoryManage.vue'),
    meta: { title: '分类管理', requiresAuth: true }
  },
  {
    path: '/admin/tags',
    name: 'TagManage',
    component: () => import('@/views/TagManage.vue'),
    meta: { title: '标签管理', requiresAuth: true }
  },
  {
    path: '/admin/friend-links',
    name: 'FriendLinkManage',
    component: () => import('@/views/FriendLinkManage.vue'),
    meta: { title: '友链管理', requiresAuth: true }
  },
  {
    path: '/admin/site-config',
    name: 'SiteConfig',
    component: () => import('@/views/SiteConfig.vue'),
    meta: { title: '站点配置', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - Blog Admin` : 'Blog Admin'
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      return next('/admin/login')
    }
  }
  if (to.path === '/admin/login') {
    const token = localStorage.getItem('token')
    if (token) {
      return next('/admin/dashboard')
    }
  }
  next()
})

export default router
