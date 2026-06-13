<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <section class="welcome-card">
        <div class="welcome-content">
          <div>
            <h1 class="headline-lg">成绩统计</h1>
            <p class="muted mt-2">按考试查看学生成绩、提交状态和班级表现。</p>
          </div>
          <button class="btn-secondary" @click="loadScores" :disabled="loading || !selectedExamId">
            <span class="material-symbols-outlined">refresh</span>
            刷新
          </button>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div class="grid gap-4 md:grid-cols-[minmax(0,1fr)_220px] md:items-end">
            <label class="grid gap-2">
              <span class="label text-on-surface">选择考试</span>
              <select v-model.number="selectedExamId" class="form-input" @change="loadScores">
                <option :value="0" disabled>请选择考试</option>
                <option v-for="exam in exams" :key="exam.examId" :value="exam.examId">
                  {{ exam.examName }}
                </option>
              </select>
            </label>
            <div class="rounded-lg border border-outline-variant bg-white px-4 py-3">
              <div class="label">考试状态</div>
              <div class="mt-1 font-bold">{{ selectedExam?.status || '-' }}</div>
            </div>
          </div>
        </div>

        <div class="p-6">
          <div v-if="error" class="mb-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
            {{ error }}
          </div>

          <div class="metric-grid mb-6">
            <article class="metric">
              <div class="metric-icon"><span class="material-symbols-outlined">groups</span></div>
              <div>
                <div class="label">学生数</div>
                <div class="headline-md">{{ scores.length }}</div>
              </div>
            </article>
            <article class="metric">
              <div class="metric-icon"><span class="material-symbols-outlined">trending_up</span></div>
              <div>
                <div class="label">平均分</div>
                <div class="headline-md">{{ averageScore }}</div>
              </div>
            </article>
            <article class="metric">
              <div class="metric-icon"><span class="material-symbols-outlined">workspace_premium</span></div>
              <div>
                <div class="label">最高分</div>
                <div class="headline-md">{{ highestScore }}</div>
              </div>
            </article>
          </div>

          <div v-if="loading" class="muted">正在加载成绩...</div>
          <div v-else-if="!exams.length" class="muted">暂无考试，请先创建考试。</div>
          <div v-else-if="!scores.length" class="muted">该考试还没有分配学生或暂无成绩。</div>
          <div v-else class="panel overflow-hidden">
            <table class="table">
              <thead>
                <tr>
                  <th>排名</th>
                  <th>学生</th>
                  <th>账号</th>
                  <th>最终分</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in scores" :key="row.studentId">
                  <td>{{ index + 1 }}</td>
                  <td class="font-semibold">{{ row.realName || row.username }}</td>
                  <td>{{ row.username }}</td>
                  <td>
                    <span class="font-bold">{{ formatScore(row.finalScore) }}</span>
                  </td>
                  <td>
                    <span class="status-pill" :class="row.status === 'SUBMITTED' ? 'bg-green-700' : 'bg-slate-600'">
                      {{ statusText(row.status) }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

type Exam = {
  examId: number
  examName: string
  status: string
}

type ScoreRow = {
  studentId: number
  username: string
  realName?: string
  finalScore: number
  status: string
}

const exams = ref<Exam[]>([])
const scores = ref<ScoreRow[]>([])
const selectedExamId = ref(0)
const loading = ref(false)
const error = ref('')

const selectedExam = computed(() => exams.value.find((exam) => exam.examId === selectedExamId.value))
const averageScore = computed(() => {
  if (!scores.value.length) return '-'
  const total = scores.value.reduce((sum, row) => sum + Number(row.finalScore || 0), 0)
  return (total / scores.value.length).toFixed(1)
})
const highestScore = computed(() => {
  if (!scores.value.length) return '-'
  return Math.max(...scores.value.map((row) => Number(row.finalScore || 0))).toFixed(1)
})

onMounted(async () => {
  await loadExams()
})

async function loadExams() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await http.get('/exams')
    if (data.code !== 0) {
      throw new Error(data.message || '加载考试失败')
    }
    exams.value = data.data || []
    selectedExamId.value = exams.value[0]?.examId || 0
    if (selectedExamId.value) {
      await loadScores()
    }
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '加载考试失败'
  } finally {
    loading.value = false
  }
}

async function loadScores() {
  if (!selectedExamId.value) return
  error.value = ''
  loading.value = true
  try {
    const { data } = await http.get(`/exams/${selectedExamId.value}/scores`)
    if (data.code !== 0) {
      throw new Error(data.message || '加载成绩失败')
    }
    scores.value = data.data || []
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '加载成绩失败'
  } finally {
    loading.value = false
  }
}

function formatScore(value: number) {
  return Number(value || 0).toFixed(1)
}

function statusText(status: string) {
  const names: Record<string, string> = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    SUBMITTED: '已提交',
  }
  return names[status] || status || '-'
}
</script>
