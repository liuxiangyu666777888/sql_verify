<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <section class="hero-band hero-band-strong">
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <div class="label">Dashboard</div>
            <h1 class="headline-lg">学习工作台</h1>
            <p class="muted mt-2">浏览题库、练习 SQL、查看即将考试。</p>
          </div>
          <button class="btn-primary" @click="$router.push('/problems/1')">
            <span class="material-symbols-outlined">arrow_forward</span>
            进入示例题
          </button>
        </div>
      </section>

      <section class="metric-row">
        <div class="stat-card">
          <span class="material-symbols-outlined stat-icon">task_alt</span>
          <div class="label">已解决题数</div>
          <div class="headline-lg mt-2">{{ dashboard.solvedCount }}</div>
        </div>
        <div class="stat-card">
          <span class="material-symbols-outlined stat-icon">target</span>
          <div class="label">正确率</div>
          <div class="headline-lg mt-2">{{ dashboard.accuracyRate }}%</div>
        </div>
        <div class="stat-card">
          <span class="material-symbols-outlined stat-icon">local_fire_department</span>
          <div class="label">连续练习</div>
          <div class="headline-lg mt-2">{{ dashboard.streakDays }} 天</div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2 class="headline-md">即将考试</h2>
        </div>
        <div class="p-5 space-y-4">
          <div v-if="dashboard.upcomingExams[0]" class="flex items-center justify-between gap-4">
            <div>
              <div class="font-semibold">{{ dashboard.upcomingExams[0].examName }}</div>
              <div class="muted text-sm mt-1">{{ dashboard.upcomingExams[0].startTime }} - {{ dashboard.upcomingExams[0].endTime }}</div>
            </div>
            <button class="btn-primary" @click="$router.push(`/student/exams/${dashboard.upcomingExams[0].examId}/take`)">
              <span class="material-symbols-outlined">school</span>
              进入考试
            </button>
          </div>
          <div v-else class="muted text-sm">暂无考试安排</div>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const dashboard = reactive({
  solvedCount: 0,
  accuracyRate: 0,
  streakDays: 0,
  upcomingExams: [] as Array<{ examId: number; examName: string; startTime: string; endTime: string }>,
})

onMounted(async () => {
  const { data } = await http.get('/student/dashboard')
  Object.assign(dashboard, data.data)
})
</script>
