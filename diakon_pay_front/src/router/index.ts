import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/transactions' },
  {
    path: '/login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    component: () => import('@/pages/RegisterPage.vue'),
    meta: { public: true },
  },
  {
    path: '/transactions',
    component: () => import('@/pages/TransactionsPage.vue'),
  },
  {
    path: '/analytics',
    component: () => import('@/pages/AnalyticsPage.vue'),
  },
  {
    path: '/profile',
    component: () => import('@/pages/ProfilePage.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const isAuthenticated = !!localStorage.getItem('accessToken')
  if (!to.meta.public && !isAuthenticated) return '/login'
  if (to.meta.public && isAuthenticated) return '/transactions'
})

export default router
