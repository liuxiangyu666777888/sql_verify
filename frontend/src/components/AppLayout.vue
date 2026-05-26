<template>
  <div class="app-shell min-h-screen">
    <aside class="fixed inset-y-0 left-0 hidden w-64 border-r border-slate-200 bg-white/88 px-4 py-5 backdrop-blur md:block">
      <div class="mb-8">
        <div class="text-xs font-semibold uppercase tracking-[0.22em] text-slate-400">Academic SQL</div>
        <div class="mt-2 text-2xl font-black text-slate-950">Judge Lab</div>
      </div>
      <nav class="space-y-1">
        <router-link v-for="item in nav" :key="item.to" :to="item.to" class="nav-link" active-class="nav-active">
          <span>{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="absolute bottom-5 left-4 right-4 border-t border-slate-200 pt-4">
        <div class="text-sm font-semibold text-slate-800">{{ roleLabel }}</div>
        <button class="mt-3 w-full rounded-lg border border-slate-200 px-3 py-2 text-left text-sm text-slate-600 hover:bg-slate-50" @click="logout">退出登录</button>
      </div>
    </aside>
    <main class="min-h-screen p-4 md:ml-64 md:p-8">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const role = computed(() => auth.user?.role || localStorage.getItem('role') || 'STUDENT')
const roleLabel = computed(() => role.value === 'TEACHER' || role.value === 'ADMIN' ? '教师端' : '学生端')
const nav = computed(() => {
  if (role.value === 'TEACHER' || role.value === 'ADMIN') {
    return [
      { to: '/teacher/dashboard', label: '课堂总览', icon: '▦' },
      { to: '/teacher/questions', label: '题库管理', icon: '⌘' },
      { to: '/teacher/exams/new', label: '考试配置', icon: '◷' },
      { to: '/teacher/classes', label: '班级管理', icon: '◎' },
      { to: '/teacher/scores', label: '成绩统计', icon: '▣' },
    ]
  }
  return [
    { to: '/student/dashboard', label: '学习工作台', icon: '▦' },
    { to: '/problems', label: '题库练习', icon: '⌘' },
    { to: '/student/exams', label: '我的考试', icon: '◷' },
    { to: '/student/submissions', label: '提交记录', icon: '▣' },
    { to: '/student/classes', label: '我的班级', icon: '◎' },
  ]
})

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

