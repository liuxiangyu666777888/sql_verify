<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <section class="welcome-card">
        <div class="welcome-content">
          <div>
            <h2 class="headline-lg">
              Welcome back, <span class="text-primary">{{ displayName }}</span>
            </h2>
            <p class="muted mt-2 flex items-center gap-2 italic">
              <span class="material-symbols-outlined text-primary fill-icon">format_quote</span>
              "Data is the new oil, but SQL is the refinery." Keep honing your logic today.
            </p>
          </div>
          <button class="btn-primary" @click="$router.push('/problems')">
            Resume Learning
            <span class="material-symbols-outlined">arrow_forward</span>
          </button>
        </div>
      </section>

      <div class="stats-grid">
        <section class="panel p-6">
          <div class="mb-6 flex items-center justify-between">
            <h3 class="headline-md flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">donut_large</span>
              Progress Overview
            </h3>
            <RouterLink class="label text-primary" to="/student/submissions">View Detailed Stats</RouterLink>
          </div>

          <div class="circle-card">
            <div class="progress-ring">
              <svg viewBox="0 0 36 36">
                <path class="track" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                <path class="value" :stroke-dasharray="`${ringPercent}, 100`" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              </svg>
              <div class="center">
                <div>
                  <div class="headline-md">{{ dashboard.solvedCount }}</div>
                  <div class="label">Solved</div>
                </div>
              </div>
            </div>

            <div class="metric-grid">
              <div class="metric">
                <div class="metric-icon">
                  <span class="material-symbols-outlined fill-icon">target</span>
                </div>
                <div>
                  <div class="label">Accuracy Rate</div>
                  <div class="headline-md">{{ dashboard.accuracyRate }}%</div>
                </div>
              </div>
              <div class="metric">
                <div class="metric-icon">
                  <span class="material-symbols-outlined fill-icon">local_fire_department</span>
                </div>
                <div>
                  <div class="label">Current Streak</div>
                  <div class="headline-md">{{ dashboard.streakDays || 0 }} Days</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="panel p-6">
          <h3 class="headline-md mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-tertiary">calendar_month</span>
            Upcoming Exams
          </h3>
          <div class="grid gap-4">
            <article v-for="exam in dashboard.upcomingExams" :key="exam.examId" class="card">
              <div class="label">Today</div>
              <h4 class="mt-2 font-bold">{{ exam.examName }}</h4>
              <p class="muted mt-1 text-sm">{{ exam.startTime }} - {{ exam.endTime }}</p>
              <button class="btn-primary mt-4" @click="$router.push(`/student/exams/${exam.examId}/take`)">Enter Exam</button>
            </article>
            <p v-if="!dashboard.upcomingExams.length" class="muted">No upcoming exams</p>
          </div>
        </section>
      </div>

      <section>
        <div class="mb-4 flex items-center justify-between">
          <h3 class="headline-md flex items-center gap-2">
            <span class="material-symbols-outlined text-tertiary">psychology</span>
            Recommended for You
          </h3>
          <RouterLink class="label text-primary" to="/problems">View All</RouterLink>
        </div>
        <div class="problem-grid">
          <article v-for="q in recommended" :key="q.questionId" class="problem-card">
            <div class="mb-3 flex items-center justify-between gap-3">
              <span class="difficulty">{{ q.difficulty }}</span>
              <span class="material-symbols-outlined text-on-surface-variant">database</span>
            </div>
            <h4 class="font-bold">{{ q.title }}</h4>
            <p class="muted mt-2 line-clamp-3 text-sm">{{ q.description }}</p>
            <button class="btn-secondary mt-4" @click="$router.push(`/problems/${q.questionId}`)">
              Solve Challenge
            </button>
          </article>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import { useAuthStore } from '../../stores/auth'
import http from '../../api/http'

const auth = useAuthStore()
const displayName = computed(() => auth.user?.realName || auth.user?.username || 'Alex Chen')
const dashboard = reactive({
  solvedCount: 0,
  accuracyRate: 0,
  streakDays: 0,
  upcomingExams: [] as Array<{ examId: number; examName: string; startTime: string; endTime: string }>,
})
const recommended = ref<Array<{ questionId: number; title: string; description: string; difficulty: string }>>([])
const ringPercent = computed(() => Math.min(100, Math.max(0, Math.round((dashboard.solvedCount / 200) * 100))))

onMounted(async () => {
  try {
    const { data } = await http.get('/student/dashboard')
    Object.assign(dashboard, data.data)
    recommended.value = (data.data.recommendedQuestions || []).slice(0, 6)
  } catch (_) {
    // keep defaults when backend is unavailable during design preview
  }
  if (!recommended.value.length) {
    try {
      const { data } = await http.get('/questions')
      recommended.value = data.data.slice(0, 6)
    } catch (_) {
      recommended.value = []
    }
  }
})
</script>
