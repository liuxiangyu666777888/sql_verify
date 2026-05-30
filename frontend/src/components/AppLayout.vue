<template>
  <div class="shell min-h-screen">
    <aside class="sidebar hidden md:flex">
      <div class="sidebar-brand">
        <div class="brand-mark">
          <span class="material-symbols-outlined fill-icon">database</span>
        </div>
        <div>
          <div class="brand-title">Academic SQL</div>
          <div class="brand-sub">Standard Edition</div>
        </div>
      </div>
      <nav class="sidebar-nav">
        <router-link
          v-for="item in nav"
          :key="item.to"
          :to="item.to"
          class="side-link"
          active-class="side-link-active"
        >
          <span class="material-symbols-outlined">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="teacher-switch" @click="toggleMode">
          <span class="material-symbols-outlined">swap_horiz</span>
          <span>{{ roleLabel === '教师端' ? '切换到学生端' : '切换到教师端' }}</span>
        </button>
        <div class="sidebar-tools">
          <a href="#" class="tool-link">
            <span class="material-symbols-outlined">menu_book</span>
            <span>Documentation</span>
          </a>
          <a href="#" class="tool-link">
            <span class="material-symbols-outlined">help_outline</span>
            <span>Help Center</span>
          </a>
        </div>
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </aside>

    <div class="content-shell">
      <header class="topbar">
        <div class="topbar-left">
          <button class="icon-btn md:hidden">
            <span class="material-symbols-outlined">menu</span>
          </button>
          <div class="topbar-title">{{ title }}</div>
        </div>
        <div class="topbar-right">
          <div class="topbar-search">
            <span class="material-symbols-outlined">search</span>
            <input :placeholder="searchPlaceholder" type="text" />
          </div>
          <button class="icon-btn">
            <span class="material-symbols-outlined">notifications</span>
            <span class="dot"></span>
          </button>
          <button class="icon-btn hidden sm:inline-flex">
            <span class="material-symbols-outlined">settings</span>
          </button>
          <div class="avatar">{{ avatarLetter }}</div>
        </div>
      </header>

      <main class="page">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const role = computed(() => auth.user?.role || localStorage.getItem('role') || 'STUDENT')
const roleLabel = computed(() => (role.value === 'TEACHER' || role.value === 'ADMIN' ? '教师端' : '学生端'))
const title = computed(() => (roleLabel.value === '教师端' ? 'Academic SQL' : 'SQL Master'))
const searchPlaceholder = computed(() =>
  roleLabel.value === '教师端' ? '搜索题目、班级、考试...' : '搜索题目、文章、考试...',
)
const avatarLetter = computed(() => (auth.user?.username || 'U').slice(0, 1).toUpperCase())
const nav = computed(() => {
  if (role.value === 'TEACHER' || role.value === 'ADMIN') {
    return [
      { to: '/teacher/dashboard', label: 'Dashboard', icon: 'dashboard' },
      { to: '/teacher/questions', label: '题库管理', icon: 'library_books' },
      { to: '/teacher/exams/new', label: '考试配置', icon: 'event_note' },
      { to: '/teacher/classes', label: '班级管理', icon: 'groups' },
      { to: '/teacher/scores', label: '成绩统计', icon: 'pending_actions' },
    ]
  }
  return [
    { to: '/student/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { to: '/problems', label: '题库练习', icon: 'task_alt' },
    { to: '/student/exams', label: '我的考试', icon: 'school' },
    { to: '/student/submissions', label: '提交记录', icon: 'history' },
    { to: '/student/classes', label: '我的班级', icon: 'groups' },
  ]
})

function logout() {
  auth.logout()
  router.push('/login')
}

function toggleMode() {
  router.push(role.value === 'TEACHER' || role.value === 'ADMIN' ? '/student/dashboard' : '/teacher/dashboard')
}
</script>
