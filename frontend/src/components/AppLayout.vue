<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">SQL</div>
        <div>
          <strong>SQL Judge</strong>
          <span>数据库练习平台</span>
        </div>
      </div>

      <nav class="nav">
        <RouterLink v-if="isTeacher" to="/teacher/dashboard">教师首页</RouterLink>
        <RouterLink v-if="isTeacher" to="/teacher/questions">题目管理</RouterLink>
        <RouterLink v-if="isTeacher" to="/teacher/exams/new">创建考试</RouterLink>
        <RouterLink v-if="isTeacher" to="/teacher/classes">班级管理</RouterLink>
        <RouterLink v-if="isTeacher" to="/teacher/scores">成绩统计</RouterLink>

        <RouterLink v-if="!isTeacher" to="/student/dashboard">学生首页</RouterLink>
        <RouterLink v-if="!isTeacher" to="/problems">题库练习</RouterLink>
        <RouterLink v-if="!isTeacher" to="/student/exams">我的考试</RouterLink>
        <RouterLink v-if="!isTeacher" to="/student/submissions">提交记录</RouterLink>
        <RouterLink v-if="!isTeacher" to="/student/classes">我的班级</RouterLink>
      </nav>
    </aside>

    <main class="main">
      <header class="topbar">
        <div>
          <div class="eyebrow">SQL Practice</div>
          <h1>{{ isTeacher ? '教师工作台' : '学生练习中心' }}</h1>
        </div>
        <div class="userbar">
          <span>{{ auth.user?.realName || auth.user?.username || '用户' }}</span>
          <button class="btn-secondary" @click="logout">退出</button>
        </div>
      </header>
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const isTeacher = computed(() => ['TEACHER', 'ADMIN'].includes(auth.user?.role || localStorage.getItem('role') || ''))

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
