import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/renew',
      name: 'renew',
      component: () => import('../views/RenewView.vue'),
      meta: { requiresAuth: true, role: 'user' }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/UserDashBoard.vue'),
      meta: { requiresAuth: true, role: 'user' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminDashBoard.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
    } else if (to.meta.role && role !== to.meta.role) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
