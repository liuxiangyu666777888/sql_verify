<template>
  <div class="app-shell min-h-screen p-6">
    <div class="max-w-7xl mx-auto space-y-6">
      <header class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500">Student Dashboard</p>
          <h1 class="text-3xl font-bold">学习工作台</h1>
        </div>
        <button class="btn-secondary rounded-lg px-4 py-2" @click="$router.push('/problems/1')">进入示例题</button>
      </header>

      <section class="grid md:grid-cols-3 gap-4">
        <div class="panel p-5">
          <p class="text-sm text-slate-500">已解决题数</p>
          <div class="text-3xl font-bold mt-2">{{ dashboard.solvedCount }}</div>
        </div>
        <div class="panel p-5">
          <p class="text-sm text-slate-500">正确率</p>
          <div class="text-3xl font-bold mt-2">{{ dashboard.accuracyRate }}%</div>
        </div>
        <div class="panel p-5">
          <p class="text-sm text-slate-500">连续练习</p>
          <div class="text-3xl font-bold mt-2">{{ dashboard.streakDays }} 天</div>
        </div>
      </section>

      <section class="panel p-6">
        <h2 class="text-xl font-semibold mb-4">即将考试</h2>
        <div class="border rounded-xl p-4 flex items-center justify-between">
          <div>
            <p class="font-medium">{{ dashboard.upcomingExams[0]?.examName || '暂无考试' }}</p>
            <p class="text-sm text-slate-500">{{ dashboard.upcomingExams[0]?.startTime || '' }} - {{ dashboard.upcomingExams[0]?.endTime || '' }}</p>
          </div>
          <button class="btn-primary rounded-lg px-4 py-2">进入考试</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import http from '../../api/http'

const dashboard = reactive({
  solvedCount: 0,
  accuracyRate: 0,
  streakDays: 0,
  upcomingExams: [] as Array<{ examName: string; startTime: string; endTime: string }>,
})

onMounted(async () => {
  const { data } = await http.get('/student/dashboard')
  Object.assign(dashboard, data.data)
})
</script>
