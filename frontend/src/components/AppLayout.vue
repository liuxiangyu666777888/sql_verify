<template>
  <div class="app-shell">
    <nav class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <span class="material-symbols-outlined fill-icon">database</span>
        </div>
        <div>
          <div class="brand-title">Academic SQL</div>
          <div class="brand-subtitle">Standard Edition</div>
        </div>
      </div>

      <div class="nav">
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/dashboard">
          <span class="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/questions">
          <span class="material-symbols-outlined">library_books</span>
          <span>Problem Bank</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/exams/new">
          <span class="material-symbols-outlined">assignment_add</span>
          <span>Configure Exam</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/classes">
          <span class="material-symbols-outlined">groups</span>
          <span>Classroom</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/scores">
          <span class="material-symbols-outlined">analytics</span>
          <span>Gradebook</span>
        </RouterLink>

        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/dashboard">
          <span class="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/problems">
          <span class="material-symbols-outlined">task_alt</span>
          <span>Solved Problems</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/submissions">
          <span class="material-symbols-outlined">history</span>
          <span>My Submissions</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/exams">
          <span class="material-symbols-outlined">school</span>
          <span>Learning Paths</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/classes">
          <span class="material-symbols-outlined">groups</span>
          <span>Classroom</span>
        </RouterLink>
      </div>

      <div class="sidebar-footer">
        <a href="#">
          <span class="material-symbols-outlined">menu_book</span>
          <span>Documentation</span>
        </a>
        <a href="#">
          <span class="material-symbols-outlined">help_outline</span>
          <span>Help Center</span>
        </a>
      </div>
    </nav>

    <div class="workspace">
      <header class="topbar">
        <div class="mobile-brand">
          <span class="material-symbols-outlined">database</span>
          <span>SQL Master</span>
        </div>
        <div class="search-box">
          <span class="material-symbols-outlined">search</span>
          <input placeholder="Search problems, articles..." />
        </div>
        <div class="top-actions">
          <button class="top-icon" aria-label="Notifications">
            <span class="material-symbols-outlined">notifications</span>
            <span class="notice-dot"></span>
          </button>
          <button class="top-icon" aria-label="Settings">
            <span class="material-symbols-outlined">settings</span>
          </button>
          <button class="btn-secondary" @click="logout">退出</button>
          <div class="avatar">{{ avatarText }}</div>
        </div>
      </header>

      <main class="content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const role = computed(() => auth.user?.role || localStorage.getItem('role') || '')
const isTeacher = computed(() => ['TEACHER', 'ADMIN'].includes(role.value))
const avatarText = computed(() => (auth.user?.realName || auth.user?.username || 'U').slice(0, 1).toUpperCase())

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
