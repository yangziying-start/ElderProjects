import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'service',
        name: 'Service',
        component: () => import('@/views/service/index.vue'),
        meta: { title: '服务管理' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'emergency',
        name: 'Emergency',
        component: () => import('@/views/emergency/index.vue'),
        meta: { title: '应急指挥中心' }
      },
      {
        path: 'meal',
        name: 'Meal',
        component: () => import('@/views/meal/index.vue'),
        meta: { title: '餐饮管理' }
      },
      {
        path: 'cleaning',
        name: 'Cleaning',
        component: () => import('@/views/cleaning/index.vue'),
        meta: { title: '保洁管理' }
      },
      {
        path: 'medical',
        name: 'Medical',
        component: () => import('@/views/medical/index.vue'),
        meta: { title: '医疗管理' }
      },
      {
        path: 'building',
        name: 'Building',
        component: () => import('@/views/building/index.vue'),
        meta: { title: '楼栋管理' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/index.vue'),
        meta: { title: '管理员管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
