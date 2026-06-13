<template>
  <div class="min-h-screen bg-background text-on-background">
    <header class="topbar">
      <button class="btn-secondary" @click="$router.push('/teacher/dashboard')">
        <span class="material-symbols-outlined">arrow_back</span>
        Exit Configuration
      </button>
      <div class="headline-md">New Assessment</div>
      <div class="label flex items-center gap-1">
        <span class="material-symbols-outlined text-[16px]">fact_check</span>
        {{ examId ? `Draft #${examId}` : 'Unsaved draft' }}
      </div>
    </header>

    <main class="grid min-h-[calc(100vh-64px)] grid-cols-[minmax(0,1fr)_360px] overflow-hidden">
      <section class="overflow-y-auto p-gutter">
        <div class="mx-auto max-w-3xl pb-28">
          <div class="mb-10 flex items-center justify-between px-4">
            <button v-for="(step, index) in steps" :key="step" class="grid justify-items-center gap-2 border-0 bg-transparent" @click="goStep(index)">
              <span class="grid h-8 w-8 place-items-center rounded-full border-4 border-background" :class="index === activeStep ? 'bg-primary text-on-primary ring-2 ring-primary' : index < activeStep ? 'bg-green-700 text-white' : 'bg-surface-container-highest text-on-surface-variant'">
                {{ index + 1 }}
              </span>
              <span class="label" :class="index === activeStep ? 'text-primary' : 'text-on-surface-variant'">{{ step }}</span>
            </button>
          </div>

          <section v-if="activeStep === 0" class="panel overflow-hidden">
            <div class="panel-header">
              <h1 class="headline-md flex items-center gap-2">
                <span class="material-symbols-outlined text-primary">settings</span>
                Exam Parameters
              </h1>
              <p class="muted mt-1">Define the fundamental settings and timing for this assessment.</p>
            </div>
            <div class="grid gap-8 p-6">
              <label class="grid gap-2">
                <span class="label text-on-surface">Exam Title</span>
                <input v-model="form.examName" class="form-input" />
              </label>
              <div class="grid gap-6 md:grid-cols-2">
                <label class="grid gap-2">
                  <span class="label text-on-surface">Start Window</span>
                  <input v-model="form.startTime" class="form-input" type="datetime-local" />
                </label>
                <label class="grid gap-2">
                  <span class="label text-on-surface">End Window</span>
                  <input v-model="form.endTime" class="form-input" type="datetime-local" />
                </label>
              </div>
              <div class="grid gap-6 md:grid-cols-2">
                <label class="grid gap-2">
                  <span class="label text-on-surface">Duration Limit (Minutes)</span>
                  <input v-model.number="form.durationMinutes" class="form-input" type="number" />
                </label>
                <label class="grid gap-2">
                  <span class="label text-on-surface">Lockdown Browser</span>
                  <div class="flex items-center justify-between rounded-lg border border-outline-variant bg-surface-bright px-4 py-3">
                    <span class="flex items-center gap-2">
                      <span class="material-symbols-outlined">lock</span>
                      Enabled
                    </span>
                    <input v-model="form.lockdownEnabled" type="checkbox" />
                  </div>
                </label>
              </div>
              <label class="grid gap-2">
                <span class="label text-on-surface">Instructions</span>
                <textarea v-model="form.instructions" class="form-input min-h-28 resize-y"></textarea>
              </label>
            </div>
          </section>

          <section v-else-if="activeStep === 1" class="panel overflow-hidden">
            <div class="panel-header">
              <h1 class="headline-md flex items-center gap-2">
                <span class="material-symbols-outlined text-primary">library_books</span>
                Select Problems
              </h1>
              <p class="muted mt-1">Search the problem bank and choose questions for this exam.</p>
            </div>
            <div class="grid gap-4 p-6">
              <div class="grid gap-3 md:grid-cols-[minmax(0,1fr)_120px]">
                <input v-model="questionKeyword" class="form-input" placeholder="Search by title or description" @keyup.enter="loadQuestions(1)" />
                <button class="btn-secondary" @click="loadQuestions(1)">
                  <span class="material-symbols-outlined">search</span>
                  Search
                </button>
              </div>
              <div class="grid gap-3">
                <article v-for="question in questions" :key="question.questionId" class="card flex items-center justify-between gap-4">
                  <div>
                    <div class="label">#{{ question.questionId }} · {{ question.difficulty }}</div>
                    <h2 class="mt-1 font-bold">{{ question.title }}</h2>
                  </div>
                  <button class="btn-secondary" @click="toggleQuestion(question)">
                    <span class="material-symbols-outlined">{{ selectedMap[question.questionId] ? 'remove' : 'add' }}</span>
                    {{ selectedMap[question.questionId] ? 'Remove' : 'Add' }}
                  </button>
                </article>
              </div>
              <div class="flex items-center justify-between gap-4">
                <button class="btn-secondary" :disabled="questionPage <= 1" @click="loadQuestions(questionPage - 1)">
                  <span class="material-symbols-outlined">chevron_left</span>
                  Prev
                </button>
                <div class="label">Page {{ questionPage }} · {{ questionTotal }} questions</div>
                <button class="btn-secondary" :disabled="questionPage * questionSize >= questionTotal" @click="loadQuestions(questionPage + 1)">
                  Next
                  <span class="material-symbols-outlined">chevron_right</span>
                </button>
              </div>
            </div>
          </section>

          <section v-else class="panel overflow-hidden">
            <div class="panel-header">
              <h1 class="headline-md flex items-center gap-2">
                <span class="material-symbols-outlined text-primary">scoreboard</span>
                Assign Points
              </h1>
              <p class="muted mt-1">Set question order and score. Saving will attach these questions to the draft exam.</p>
            </div>
            <div class="grid gap-4 p-6">
              <article v-for="(question, index) in selectedQuestions" :key="question.questionId" class="card grid gap-3 md:grid-cols-[minmax(0,1fr)_120px_120px] md:items-center">
                <div>
                  <div class="label">Question {{ index + 1 }} · #{{ question.questionId }}</div>
                  <h2 class="mt-1 font-bold">{{ question.title }}</h2>
                </div>
                <label class="grid gap-1">
                  <span class="label">Order</span>
                  <input v-model.number="question.questionOrder" class="form-input" type="number" min="1" />
                </label>
                <label class="grid gap-1">
                  <span class="label">Score</span>
                  <input v-model.number="question.score" class="form-input" type="number" min="0" max="100" />
                </label>
              </article>
              <p v-if="!selectedQuestions.length" class="muted">No questions selected yet.</p>
            </div>
          </section>
        </div>
      </section>

      <aside class="border-l border-outline-variant bg-surface-container-lowest p-6">
        <div class="label mb-4">Live Preview</div>
        <section class="panel p-5">
          <div class="difficulty">DRAFT</div>
          <h2 class="headline-md mt-4">{{ form.examName }}</h2>
          <p class="muted mt-2">{{ form.instructions }}</p>
          <div class="mt-6 grid gap-3 text-sm">
            <div class="metric"><span class="material-symbols-outlined">calendar_today</span><span>{{ form.startTime }}</span></div>
            <div class="metric"><span class="material-symbols-outlined">event_busy</span><span>{{ form.endTime }}</span></div>
            <div class="metric"><span class="material-symbols-outlined">timer</span><span>{{ form.durationMinutes }} minutes</span></div>
            <div class="metric"><span class="material-symbols-outlined">quiz</span><span>{{ selectedQuestions.length }} questions · {{ totalScore }} pts</span></div>
          </div>
          <div v-if="message" class="mt-5 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-800">
            {{ message }}
          </div>
          <div v-if="error" class="mt-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
            {{ error }}
          </div>
          <button class="btn-primary mt-6 w-full" :disabled="saving" @click="primaryAction">
            <span class="material-symbols-outlined">{{ saving ? 'progress_activity' : activeStep === 2 ? 'playlist_add_check' : 'arrow_forward' }}</span>
            {{ actionLabel }}
          </button>
          <button v-if="activeStep > 0" class="btn-secondary mt-3 w-full" @click="activeStep -= 1">
            <span class="material-symbols-outlined">arrow_back</span>
            Back
          </button>
        </section>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import http from '../../api/http'

type QuestionSummary = {
  questionId: number
  title: string
  difficulty: string
}

type SelectedQuestion = QuestionSummary & {
  score: number
  questionOrder: number
}

const steps = ['Configuration', 'Select Problems', 'Assign Points']
const activeStep = ref(0)
const examId = ref<number | null>(null)
const form = reactive({
  examName: 'CS304 Database Systems - Midterm',
  startTime: '2026-06-15T09:00',
  endTime: '2026-06-15T11:00',
  durationMinutes: 120,
  lockdownEnabled: true,
  instructions: '请使用标准 MySQL 语法完成题目。',
})

const saving = ref(false)
const message = ref('')
const error = ref('')
const questionKeyword = ref('')
const questions = ref<QuestionSummary[]>([])
const questionPage = ref(1)
const questionSize = ref(8)
const questionTotal = ref(0)
const selectedQuestions = ref<SelectedQuestion[]>([])

const selectedMap = computed(() => Object.fromEntries(selectedQuestions.value.map((question) => [question.questionId, true])))
const totalScore = computed(() => selectedQuestions.value.reduce((sum, question) => sum + Number(question.score || 0), 0))
const actionLabel = computed(() => {
  if (saving.value) return 'Saving...'
  if (activeStep.value === 0) return examId.value ? 'Save And Continue' : 'Create Draft'
  if (activeStep.value === 1) return 'Continue To Points'
  return 'Save Questions'
})

onMounted(() => loadQuestions(1))

function toApiDateTime(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function validateExam() {
  if (!form.examName.trim()) return '考试标题不能为空'
  if (!form.startTime || !form.endTime) return '开始和结束时间不能为空'
  if (new Date(form.startTime).getTime() >= new Date(form.endTime).getTime()) return '结束时间必须晚于开始时间'
  if (!Number.isFinite(form.durationMinutes) || form.durationMinutes <= 0) return '考试时长必须大于 0 分钟'
  return ''
}

function validateQuestions() {
  if (!selectedQuestions.value.length) return '请至少选择一道题'
  for (const question of selectedQuestions.value) {
    if (!Number.isFinite(question.score) || question.score < 0 || question.score > 100) return '题目分值必须在 0 到 100 之间'
    if (!Number.isFinite(question.questionOrder) || question.questionOrder < 1) return '题目顺序必须大于 0'
  }
  return ''
}

async function primaryAction() {
  if (activeStep.value === 0) {
    await saveExam()
    if (!error.value) activeStep.value = 1
  } else if (activeStep.value === 1) {
    error.value = validateQuestions()
    if (!error.value) activeStep.value = 2
  } else {
    await saveQuestions()
  }
}

function goStep(index: number) {
  if (index > 0 && !examId.value) {
    error.value = '请先保存考试配置'
    return
  }
  activeStep.value = index
}

async function saveExam() {
  message.value = ''
  error.value = validateExam()
  if (error.value) return

  saving.value = true
  try {
    const payload = {
      examName: form.examName.trim(),
      startTime: toApiDateTime(form.startTime),
      endTime: toApiDateTime(form.endTime),
      durationMinutes: form.durationMinutes,
      instructions: form.instructions.trim(),
      lockdownEnabled: form.lockdownEnabled,
    }
    const request = examId.value ? http.put(`/exams/${examId.value}`, payload) : http.post('/exams', payload)
    const { data } = await request
    if (data.code !== 0) throw new Error(data.message || '保存考试失败')
    examId.value = data.data.examId
    message.value = `考试配置已保存，ID: ${examId.value}`
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '保存考试失败'
  } finally {
    saving.value = false
  }
}

async function loadQuestions(nextPage: number) {
  questionPage.value = nextPage
  const { data } = await http.get('/questions', {
    params: { keyword: questionKeyword.value, page: questionPage.value, size: questionSize.value },
  })
  questions.value = data.data.items || []
  questionTotal.value = data.data.total || 0
}

function toggleQuestion(question: QuestionSummary) {
  const existingIndex = selectedQuestions.value.findIndex((item) => item.questionId === question.questionId)
  if (existingIndex >= 0) {
    selectedQuestions.value.splice(existingIndex, 1)
    return
  }
  selectedQuestions.value.push({
    ...question,
    score: 10,
    questionOrder: selectedQuestions.value.length + 1,
  })
}

async function saveQuestions() {
  message.value = ''
  error.value = validateQuestions()
  if (error.value) return
  if (!examId.value) {
    error.value = '请先保存考试配置'
    return
  }

  saving.value = true
  try {
    const payload = selectedQuestions.value.map((question, index) => ({
      questionId: question.questionId,
      score: question.score,
      questionOrder: question.questionOrder || index + 1,
    }))
    const { data } = await http.post(`/exams/${examId.value}/questions`, payload)
    if (data.code !== 0) throw new Error(data.message || '保存题目失败')
    message.value = '考试题目和分值已保存'
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '保存题目失败'
  } finally {
    saving.value = false
  }
}
</script>
