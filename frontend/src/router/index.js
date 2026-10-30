import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'DashboardHome',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页', icon: 'House' }
      }
    ]
  },
  {
    path: '/dismissal',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Dismissal',
        component: () => import('../views/Dismissal.vue'),
        meta: { title: '放学管理', icon: 'School' }
      }
    ]
  },
  {
    path: '/classes',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Classes',
        component: () => import('../views/Classes.vue'),
        meta: { title: '班级管理', icon: 'Reading' }
      }
    ]
  },
  {
    path: '/students',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Students',
        component: () => import('../views/Students.vue'),
        meta: { title: '学生管理', icon: 'User' }
      }
    ]
  },
  {
    path: '/notices',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Notices',
        component: () => import('../views/Notices.vue'),
        meta: { title: '通知公告', icon: 'Bell' }
      }
    ]
  },
  {
    path: '/users',
    component: () => import('../views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Users',
        component: () => import('../views/Users.vue'),
        meta: { title: '用户管理', icon: 'Avatar' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta && to.meta.title) {
    document.title = `${to.meta.title} - 可视化放学系统`
  } else {
    document.title = '可视化放学系统'
  }

  // 检查是否需要登录
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false // 默认需要登录

  if (requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到首页
    next('/dashboard')
  } else {
    next()
  }
})

export default router
