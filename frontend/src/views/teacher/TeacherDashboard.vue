<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <div class="label">Dashboard</div>
          <h1 class="headline-lg">课堂总览</h1>
        </div>
        <button class="btn-primary" @click="$router.push('/teacher/exams/new')">
          <span class="material-symbols-outlined">add</span>
          新建考试
        </button>
      </header>
      <section class="grid gap-6 md:grid-cols-3">
        <div class="stat-card"><span class="material-symbols-outlined stat-icon">groups</span><div class="label">班级数</div><div class="headline-lg mt-2">{{ dashboard.activeClasses }}</div></div>
        <div class="stat-card"><span class="material-symbols-outlined stat-icon">library_books</span><div class="label">题库数</div><div class="headline-lg mt-2">{{ dashboard.totalProblems }}</div></div>
        <div class="stat-card"><span class="material-symbols-outlined stat-icon">pending_actions</span><div class="label">待处理提交</div><div class="headline-lg mt-2 text-red-600">{{ dashboard.pendingReviews }}</div></div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const dashboard = reactive({
  activeClasses: 0,
  totalProblems: 0,
  pendingReviews: 0,
})

onMounted(async () => {
  const { data } = await http.get('/teacher/dashboard')
  Object.assign(dashboard, data.data)
})
</script>
