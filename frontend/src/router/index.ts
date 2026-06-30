import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import StudentDashboard from '../views/student/StudentDashboard.vue'
import QuestionViewer from '../views/QuestionViewer.vue'
import ExamWizard from '../views/teacher/ExamWizard.vue'
import TeacherDashboard from '../views/teacher/TeacherDashboard.vue'
import QuestionList from '../views/QuestionList.vue'
import StudentSubmissions from '../views/student/StudentSubmissions.vue'
import StudentClasses from '../views/student/StudentClasses.vue'
import StudentExams from '../views/student/StudentExams.vue'
import ExamTake from '../views/student/ExamTake.vue'
import TeacherQuestions from '../views/teacher/TeacherQuestions.vue'
import TeacherClasses from '../views/teacher/TeacherClasses.vue'
import TeacherScores from '../views/teacher/TeacherScores.vue'
import AdminUsers from '../views/admin/AdminUsers.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView },
  { path: '/student/dashboard', component: StudentDashboard },
  { path: '/student/submissions', component: StudentSubmissions },
  { path: '/student/classes', component: StudentClasses },
  { path: '/student/exams', component: StudentExams },
  { path: '/student/exams/:id/take', component: ExamTake },
  { path: '/problems', component: QuestionList },
  { path: '/problems/:id', component: QuestionViewer },
  { path: '/teacher/dashboard', component: TeacherDashboard },
  { path: '/teacher/questions', component: TeacherQuestions },
  { path: '/teacher/classes', component: TeacherClasses },
  { path: '/teacher/scores', component: TeacherScores },
  { path: '/teacher/exams/new', component: ExamWizard },
  { path: '/admin/users', component: AdminUsers },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  auth.restore()
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path !== '/login' && token && (!auth.user || auth.user.userId === 0)) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      return '/login'
    }
  }
  if (to.path.startsWith('/teacher') && auth.user?.role && !['TEACHER', 'ASSISTANT', 'ADMIN'].includes(auth.user.role)) {
    return '/student/dashboard'
  }
  if (to.path.startsWith('/admin') && auth.user?.role && auth.user.role !== 'ADMIN') {
    return ['TEACHER', 'ASSISTANT'].includes(auth.user.role) ? '/teacher/dashboard' : '/student/dashboard'
  }
  return true
})

export default router
