<template>
  <div class="wizard-shell">
    <header class="topbar wizard-header">
      <div class="topbar-left">
        <button class="wizard-back" @click="$router.push('/teacher/dashboard')">
          <span class="material-symbols-outlined">arrow_back</span>
          Exit Configuration
        </button>
        <div class="wizard-title">New Midterm Assessment</div>
      </div>
      <div class="wizard-save">
        <span class="material-symbols-outlined">cloud_done</span>
        Draft auto-saved
      </div>
    </header>

    <main class="wizard-layout">
      <section>
        <div class="wizard-stepper">
          <div class="wizard-step is-active"><span>1</span><b>Configuration</b></div>
          <div class="wizard-step"><span>2</span><b>Select Problems</b></div>
          <div class="wizard-step"><span>3</span><b>Assign Points</b></div>
        </div>

        <section class="wizard-panel overflow-hidden">
          <div class="panel-header">
            <h1 class="headline-md flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">settings</span>
              Exam Parameters
            </h1>
            <p class="muted mt-1">Define the fundamental settings and timing for this assessment.</p>
          </div>
          <div class="space-y-8 p-6">
            <div>
              <label class="mb-2 block text-sm font-bold">Exam Title</label>
              <input v-model="form.examName" class="input" placeholder="Enter an official title..." />
            </div>
            <div class="grid gap-6 md:grid-cols-2">
              <div>
                <label class="mb-2 block text-sm font-bold">Start Window</label>
                <input v-model="form.startTime" type="datetime-local" class="input" />
              </div>
              <div>
                <label class="mb-2 block text-sm font-bold">End Window</label>
                <input v-model="form.endTime" type="datetime-local" class="input" />
              </div>
            </div>
            <div class="grid gap-6 md:grid-cols-2">
              <div>
                <label class="mb-2 block text-sm font-bold">Duration Limit</label>
                <input v-model.number="durationMinutes" type="number" class="input" />
              </div>
              <div>
                <label class="mb-2 block text-sm font-bold">Environment Strictness</label>
                <div class="flex items-center justify-between rounded-lg border border-outline-variant bg-surface p-3">
                  <span class="flex items-center gap-2"><span class="material-symbols-outlined">lock</span>Lockdown Browser</span>
                  <input v-model="lockdownEnabled" type="checkbox" />
                </div>
              </div>
            </div>
            <textarea v-model="form.instructions" class="input min-h-[120px]" placeholder="考试说明"></textarea>
          </div>
        </section>

        <section class="wizard-panel overflow-hidden mt-6">
          <div class="panel-header">
            <h2 class="headline-md">Select Problems</h2>
          </div>
          <table class="table">
            <thead>
              <tr><th>选择</th><th>题目</th><th>难度</th><th>分值</th></tr>
            </thead>
            <tbody>
              <tr v-for="question in questions" :key="question.questionId">
                <td><input v-model="selectedQuestionIds" type="checkbox" :value="question.questionId" /></td>
                <td>{{ question.title }}</td>
                <td>{{ question.difficulty }}</td>
                <td><input v-model.number="scores[question.questionId]" class="input max-w-24" type="number" min="0" max="100" /></td>
              </tr>
            </tbody>
          </table>
        </section>

        <div class="flex flex-wrap gap-3 mt-6">
          <button class="btn-secondary" @click="saveDraft">保存草稿</button>
          <button class="btn-primary" @click="publish">发布考试</button>
        </div>
        <div v-if="message" class="wizard-panel p-4 text-sm mt-4">{{ message }}</div>
      </section>

      <aside class="wizard-sidebar p-5">
        <div class="label">Live Preview</div>
        <h2 class="headline-md mt-2">{{ form.examName }}</h2>
        <p class="muted mt-3">{{ form.instructions }}</p>
        <div class="mt-6 space-y-3 text-sm">
          <div class="flex justify-between"><span>Start</span><b>{{ form.startTime }}</b></div>
          <div class="flex justify-between"><span>End</span><b>{{ form.endTime }}</b></div>
          <div class="flex justify-between"><span>Problems</span><b>{{ selectedQuestionIds.length }}</b></div>
          <div class="flex justify-between"><span>Total Points</span><b>{{ totalPoints }}</b></div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import http from '../../api/http'

const form = reactive({
  examName: 'CS304 Database Systems - Midterm',
  startTime: '2026-05-26T09:00',
  endTime: '2026-05-26T11:00',
  instructions: '请使用标准 MySQL 语法完成题目。',
})
const durationMinutes = ref(120)
const lockdownEnabled = ref(false)
const questions = ref<any[]>([])
const selectedQuestionIds = ref<number[]>([1])
const scores = reactive<Record<number, number>>({ 1: 100 })
const message = ref('')
const totalPoints = computed(() => selectedQuestionIds.value.reduce((sum, id) => sum + (scores[id] || 0), 0))

function payload() {
  return {
    examName: form.examName,
    startTime: form.startTime + ':00',
    endTime: form.endTime + ':00',
    instructions: form.instructions,
    durationMinutes: durationMinutes.value,
    lockdownEnabled: lockdownEnabled.value,
  }
}

async function saveDraft() {
  const { data } = await http.post('/exams', payload())
  message.value = `草稿已保存：#${data.data.examId}`
}

async function publish() {
  const { data } = await http.post('/exams', payload())
  const examId = data.data.examId
  if (examId) {
    const items = selectedQuestionIds.value.map((questionId, index) => ({
      questionId,
      score: scores[questionId] || 100,
      questionOrder: index + 1,
    }))
    if (items.length) {
      await http.post(`/exams/${examId}/questions`, items)
    }
    await http.post(`/exams/${examId}/students`, [3])
    await http.post(`/exams/${examId}/publish`)
    message.value = `考试已发布：#${examId}`
  }
}

onMounted(async () => {
  const { data } = await http.get('/questions')
  questions.value = data.data
  for (const question of questions.value) {
    if (!scores[question.questionId]) {
      scores[question.questionId] = 100
    }
  }
})
</script>
