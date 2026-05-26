import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'questions',
        name: 'QuestionList',
        component: () => import('../views/questions/QuestionList.vue')
      },
      {
        path: 'question/:id',
        name: 'QuestionDetail',
        component: () => import('../views/questions/QuestionDetail.vue')
      },
      {
        path: 'exams',
        name: 'ExamList',
        component: () => import('../views/exams/ExamList.vue')
      },
      {
        path: 'exam/:id',
        name: 'ExamDetail',
        component: () => import('../views/exams/ExamDetail.vue')
      },
      {
        path: 'classes',
        name: 'ClassManage',
        component: () => import('../views/classes/ClassManage.vue'),
        meta: { role: [1, 2] }
      },
      {
        path: 'submits',
        name: 'SubmitList',
        component: () => import('../views/judge/SubmitList.vue')
      },
      {
        path: 'community',
        name: 'CommunityList',
        component: () => import('../views/community/CommunityList.vue')
      },
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('../views/dashboard/TeacherDashboard.vue'),
        meta: { role: [1, 3] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const session = localStorage.getItem('session')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.meta.guest) {
    if (session) return next('/home')
    return next()
  }

  if (!session) return next('/login')

  if (to.meta.role && !to.meta.role.includes(user?.role)) {
    return next('/home')
  }

  next()
})

export default router
