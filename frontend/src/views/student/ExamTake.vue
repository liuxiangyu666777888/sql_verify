<template>
  <AppLayout>
    <div class="page-inner">
      <div class="split-editor">
        <section class="panel overflow-hidden">
          <div class="panel-header">
            <div class="label">Exam #{{ examId }}</div>
            <h1 class="headline-md mt-1">考试作答</h1>
          </div>
          <div class="p-6 space-y-5">
            <div v-if="questions.length" class="grid gap-2">
              <div class="label">题目导航</div>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="(question, index) in questions"
                  :key="question.questionId"
                  class="icon-btn"
                  :class="index === activeIndex ? 'bg-primary text-on-primary' : ''"
                  :title="question.title"
                  @click="selectQuestion(index)"
                >
                  {{ index + 1 }}
                </button>
              </div>
            </div>
            <div class="rounded-lg border border-outline-variant bg-surface-low p-4">
              <div class="label mb-2">当前题目</div>
              <div class="font-semibold">{{ currentQuestion?.title || '加载中' }}</div>
              <p class="muted mt-2 text-sm">{{ currentQuestion?.description || '请等待题目加载完成。' }}</p>
              <div v-if="currentQuestion" class="mt-3 flex flex-wrap gap-2 text-sm">
                <span class="difficulty">第 {{ activeIndex + 1 }} 题</span>
                <span class="difficulty">{{ currentQuestion.score || 0 }} 分</span>
              </div>
            </div>
            <div v-if="currentQuestion?.sourceSchemaSql" class="code-box">{{ currentQuestion.sourceSchemaSql }}</div>
          </div>
        </section>

        <div class="split-divider">
          <div class="h-8 w-0.5 rounded-full bg-outline-variant"></div>
        </div>

        <section class="panel overflow-hidden">
          <div class="editor-shell">
            <div class="editor-toolbar">
              <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-on-surface-variant">school</span>
                <span class="label normal-case tracking-normal">MySQL</span>
              </div>
              <div class="flex gap-2">
                <button class="btn-secondary" @click="run">
                  <span class="material-symbols-outlined">play_arrow</span>
                  运行
                </button>
                <button class="btn-primary" @click="submit">
                  <span class="material-symbols-outlined">cloud_upload</span>
                  提交
                </button>
              </div>
            </div>

            <div class="editor-body">
              <div class="line-gutter">
                <div v-for="line in 18" :key="line">{{ line }}</div>
              </div>
              <textarea v-model="sql" class="editor-area"></textarea>
            </div>

            <div class="space-y-4 bg-surface-lowest p-4">
              <div v-if="questions.length" class="flex items-center justify-between gap-3">
                <button class="btn-secondary" :disabled="activeIndex === 0" @click="selectQuestion(activeIndex - 1)">
                  <span class="material-symbols-outlined">chevron_left</span>
                  上一题
                </button>
                <div class="label">Question {{ activeIndex + 1 }} / {{ questions.length }}</div>
                <button class="btn-secondary" :disabled="activeIndex >= questions.length - 1" @click="selectQuestion(activeIndex + 1)">
                  下一题
                  <span class="material-symbols-outlined">chevron_right</span>
                </button>
              </div>
              <div class="status-pill" :class="statusClass">
                <span class="material-symbols-outlined text-[16px]">check_circle</span>
                {{ result.status }}
              </div>
              <div class="result-meta">
                <span>得分: <strong>{{ result.score }}</strong></span>
                <span v-if="result.runtimeMs">执行用时: <strong>{{ result.runtimeMs }} ms</strong></span>
              </div>
              <div v-if="result.errorMessage" class="text-sm text-red-600">{{ result.errorMessage }}</div>
              <div v-if="result.resultPreview?.columns?.length" class="panel overflow-hidden">
                <table class="table">
                  <thead>
                    <tr>
                      <th v-for="col in result.resultPreview.columns" :key="col">{{ col }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, idx) in result.resultPreview.rows" :key="idx">
                      <td v-for="(cell, cidx) in row" :key="cidx">{{ cell }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const route = useRoute()
const examId = Number(route.params.id)
const questions = ref<any[]>([])
const activeIndex = ref(0)
const sqlDrafts = ref<Record<number, string>>({})
const results = ref<Record<number, ResultState>>({})

type ResultState = {
  status: string
  score: number
  runtimeMs?: number
  errorMessage?: string
  resultPreview?: any
}

const defaultSql = 'SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary FROM Employee e JOIN Department d ON e.departmentId = d.id WHERE (e.departmentId, e.salary) IN (SELECT departmentId, MAX(salary) FROM Employee GROUP BY departmentId);'
const defaultResult: ResultState = {
  status: '待提交',
  score: 0,
}

const currentQuestion = computed(() => questions.value[activeIndex.value])
const currentQuestionId = computed(() => currentQuestion.value?.questionId || 0)
const sql = computed({
  get() {
    return sqlDrafts.value[currentQuestionId.value] ?? defaultSql
  },
  set(value: string) {
    if (currentQuestionId.value) {
      sqlDrafts.value[currentQuestionId.value] = value
    }
  },
})
const result = computed(() => {
  if (!currentQuestionId.value) return defaultResult
  return results.value[currentQuestionId.value] || defaultResult
})

const statusClass = computed(() => {
  if (result.value.status === 'AC') return 'bg-green-700'
  if (['WA', 'ERROR', 'FORBIDDEN', 'TLE'].includes(result.value.status)) return 'bg-red-600'
  return 'bg-slate-600'
})

onMounted(async () => {
  const { data } = await http.get(`/exams/${examId}/questions`)
  questions.value = data.data || []
  for (const question of questions.value) {
    sqlDrafts.value[question.questionId] = defaultSql
    results.value[question.questionId] = { ...defaultResult }
  }
})

function selectQuestion(index: number) {
  if (index < 0 || index >= questions.value.length) return
  activeIndex.value = index
}

async function run() {
  const questionId = currentQuestionId.value
  if (!questionId) return
  try {
    const { data } = await http.post('/judge/run', { questionId, examId, sqlCode: sql.value })
    results.value[questionId] = data.data
  } catch (err: any) {
    results.value[questionId] = {
      status: err?.response?.status === 403 ? 'FORBIDDEN' : 'ERROR',
      score: 0,
      errorMessage: err?.response?.data?.message || err?.message || '请求失败',
    }
  }
}

async function submit() {
  const questionId = currentQuestionId.value
  if (!questionId) return
  try {
    const { data } = await http.post('/submissions', { questionId, examId, sqlCode: sql.value })
    results.value[questionId] = data.data
  } catch (err: any) {
    results.value[questionId] = {
      status: err?.response?.status === 403 ? 'FORBIDDEN' : 'ERROR',
      score: 0,
      errorMessage: err?.response?.data?.message || err?.message || '请求失败',
    }
  }
}
</script>
