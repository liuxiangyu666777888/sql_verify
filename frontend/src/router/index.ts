import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import StudentDashboard from '../views/student/StudentDashboard.vue'
import QuestionViewer from '../views/QuestionViewer.vue'
import ExamWizard from '../views/teacher/ExamWizard.vue'
import TeacherDashboard from '../views/teacher/TeacherDashboard.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView },
  { path: '/student/dashboard', component: StudentDashboard },
  { path: '/problems/:id', component: QuestionViewer },
  { path: '/teacher/dashboard', component: TeacherDashboard },
  { path: '/teacher/exams/new', component: ExamWizard },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path.startsWith('/teacher') && auth.user?.role && !['TEACHER', 'ADMIN'].includes(auth.user.role)) {
    return '/student/dashboard'
  }
  return true
})

export default router
