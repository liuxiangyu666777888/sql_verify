<template>
  <AppLayout>
    <div class="mx-auto max-w-[1280px] space-y-6">
      <WelcomeBanner
        name="Alex Chen"
        quote="Data is the new oil, but SQL is the refinery. Keep honing your logic today."
        action-label="进入示例题"
        @action="$router.push('/problems/1')"
      />

      <!-- Progress Overview -->
      <div class="grid gap-6 lg:grid-cols-3">
        <section class="lg:col-span-2 rounded-xl border border-outline-variant bg-surface-lowest p-6 shadow-sm">
          <div class="mb-6 flex items-center justify-between">
            <h3 class="flex items-center gap-2 text-[24px] font-semibold">
              <span class="material-symbols-outlined text-primary">donut_large</span>
              Progress Overview
            </h3>
            <a href="#" class="text-xs font-extrabold text-primary hover:underline">View Detailed Stats</a>
          </div>
          <div class="flex flex-col items-center justify-around gap-6 sm:flex-row">
            <ProgressRing :value="dashboard.solvedCount" :total="200" label="Solved" />
            <div class="grid w-full gap-4 sm:w-2/3">
              <div class="flex items-center gap-4 rounded-lg border border-outline-variant bg-surface-container-low p-4">
                <div class="flex h-12 w-12 items-center justify-center rounded-full bg-secondary-container text-on-secondary-container">
                  <span class="material-symbols-outlined fill-icon">target</span>
                </div>
                <div>
                  <div class="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Accuracy Rate</div>
                  <div class="text-[24px] font-semibold text-on-surface">{{ dashboard.accuracyRate }}%</div>
                </div>
              </div>
              <div class="flex items-center gap-4 rounded-lg border border-outline-variant bg-surface-container-low p-4">
                <div class="flex h-12 w-12 items-center justify-center rounded-full bg-tertiary-container text-on-tertiary-container">
                  <span class="material-symbols-outlined fill-icon">local_fire_department</span>
                </div>
                <div>
                  <div class="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Current Streak</div>
                  <div class="text-[24px] font-semibold text-on-surface">{{ dashboard.streakDays }} Days</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Upcoming Exams -->
        <section class="flex flex-col rounded-xl border border-outline-variant bg-surface-lowest p-6 shadow-sm">
          <div class="mb-6 flex items-center gap-2 text-[24px] font-semibold">
            <span class="material-symbols-outlined text-secondary">calendar_month</span>
            Upcoming Exams
          </div>
          <div class="space-y-4">
            <ExamCard
              v-for="exam in dashboard.upcomingExams"
              :key="exam.examId"
              :title="exam.examName"
              :badge="'Today'"
              :badge-urgent="true"
              :time-range="`${exam.startTime} - ${exam.endTime}`"
              show-enter
              @enter="$router.push(`/student/exams/${exam.examId}/take`)"
            />
          </div>
          <div v-if="!dashboard.upcomingExams.length" class="py-8 text-center text-sm text-on-surface-variant">
            No upcoming exams
          </div>
        </section>
      </div>

      <!-- Recommended Problems -->
      <section>
        <div class="mb-4 flex items-center justify-between">
          <h3 class="flex items-center gap-2 text-[24px] font-semibold">
            <span class="material-symbols-outlined text-tertiary">psychology</span>
            Recommended for You
          </h3>
          <a href="#" class="text-xs font-extrabold text-primary hover:underline">View All</a>
        </div>
        <div class="grid gap-4 md:grid-cols-3">
          <ProblemCard
            v-for="p in recommendedProblems"
            :key="p.id"
            :id="p.id"
            :title="p.title"
            :description="p.description"
            :difficulty="p.difficulty"
            :tags="p.tags"
            @solve="$router.push(`/problems/${p.id}`)"
          />
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import WelcomeBanner from '../../components/WelcomeBanner.vue'
import ProgressRing from '../../components/ProgressRing.vue'
import ExamCard from '../../components/ExamCard.vue'
import ProblemCard from '../../components/ProblemCard.vue'
import http from '../../api/http'

const dashboard = reactive({
  solvedCount: 0,
  accuracyRate: 0,
  streakDays: 0,
  upcomingExams: [] as Array<{ examId: number; examName: string; startTime: string; endTime: string }>,
})

const recommendedProblems = [
  { id: 'SQL-105', title: 'Department Top Three Salaries', description: 'Find who earns the most in each department using window functions.', difficulty: 'Medium', tags: ['JOIN', 'DENSE_RANK'] },
  { id: 'SQL-185', title: 'Trips and Users', description: 'Calculate cancellation rate with unbanned users for each day.', difficulty: 'Hard', tags: ['GROUP BY', 'CASE'] },
  { id: 'SQL-197', title: 'Rising Temperature', description: 'Find dates with higher temperatures compared to previous day.', difficulty: 'Medium', tags: ['DATEDIFF'] },
]

onMounted(async () => {
  try {
    const { data } = await http.get('/student/dashboard')
    Object.assign(dashboard, data.data)
  } catch (_) { /* use defaults */ }
})
</script>
