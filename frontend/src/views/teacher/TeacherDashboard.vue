<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <section class="welcome-card">
        <div class="welcome-content">
          <div>
            <h2 class="headline-lg">Instructor Dashboard</h2>
            <p class="muted mt-2">Monitor classes, curate SQL problems, and configure assessments.</p>
          </div>
          <button class="btn-primary" @click="$router.push('/teacher/exams/new')">
            New Assessment
            <span class="material-symbols-outlined">add</span>
          </button>
        </div>
      </section>

      <div class="metric-grid">
        <article class="metric">
          <div class="metric-icon"><span class="material-symbols-outlined">groups</span></div>
          <div>
            <div class="label">Active Classes</div>
            <div class="headline-md">{{ dashboard.activeClasses }}</div>
          </div>
        </article>
        <article class="metric">
          <div class="metric-icon"><span class="material-symbols-outlined">library_books</span></div>
          <div>
            <div class="label">Total Problems</div>
            <div class="headline-md">{{ dashboard.totalProblems }}</div>
          </div>
        </article>
        <article class="metric">
          <div class="metric-icon"><span class="material-symbols-outlined">rate_review</span></div>
          <div>
            <div class="label">Pending Reviews</div>
            <div class="headline-md">{{ dashboard.pendingReviews }}</div>
          </div>
        </article>
      </div>

      <div class="stats-grid">
        <section class="panel p-6">
          <h3 class="headline-md mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">assignment</span>
            Recent Exams
          </h3>
          <div class="grid gap-4">
            <article v-for="exam in dashboard.recentExams" :key="exam.examId" class="card">
              <div class="label">{{ exam.status }}</div>
              <h4 class="mt-2 font-bold">{{ exam.examName }}</h4>
              <p class="muted mt-1 text-sm">{{ exam.startTime }} - {{ exam.endTime }}</p>
            </article>
            <p v-if="!dashboard.recentExams.length" class="muted">No recent exams</p>
          </div>
        </section>

        <section class="panel p-6">
          <h3 class="headline-md mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-tertiary">tune</span>
            Quick Actions
          </h3>
          <div class="grid gap-4">
            <button class="btn-secondary justify-start" @click="$router.push('/teacher/questions')">
              <span class="material-symbols-outlined">edit_note</span>
              Manage Problem Bank
            </button>
            <button class="btn-secondary justify-start" @click="$router.push('/teacher/classes')">
              <span class="material-symbols-outlined">groups</span>
              Manage Classes
            </button>
            <button class="btn-secondary justify-start" @click="$router.push('/teacher/scores')">
              <span class="material-symbols-outlined">bar_chart</span>
              View Gradebook
            </button>
          </div>
        </section>
      </div>
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
  recentExams: [] as any[],
})

onMounted(async () => {
  try {
    const { data } = await http.get('/teacher/dashboard')
    Object.assign(dashboard, data.data)
  } catch (_) {
    // keep defaults
  }
})
</script>
