<template>
  <div class="app-shell min-h-screen p-6">
    <div class="max-w-7xl mx-auto space-y-6">
      <header class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500">Teacher Dashboard</p>
          <h1 class="text-3xl font-bold">课堂总览</h1>
        </div>
        <button class="btn-primary rounded-lg px-4 py-2" @click="$router.push('/teacher/exams/new')">新建考试</button>
      </header>
      <section class="grid md:grid-cols-3 gap-4">
        <div class="panel p-5"><p class="text-sm text-slate-500">班级数</p><div class="text-3xl font-bold mt-2">{{ dashboard.activeClasses }}</div></div>
        <div class="panel p-5"><p class="text-sm text-slate-500">题库数</p><div class="text-3xl font-bold mt-2">{{ dashboard.totalProblems }}</div></div>
        <div class="panel p-5"><p class="text-sm text-slate-500">待处理提交</p><div class="text-3xl font-bold mt-2 text-red-600">{{ dashboard.pendingReviews }}</div></div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
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
