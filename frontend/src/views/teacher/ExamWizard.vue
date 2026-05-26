<template>
  <AppLayout>
    <div class="max-w-6xl mx-auto space-y-6">
      <header>
        <p class="text-sm text-slate-500">Exam Wizard</p>
        <h1 class="text-3xl font-bold">新建考试</h1>
      </header>

      <section class="panel p-6 space-y-4">
        <h2 class="text-xl font-semibold">1. 基本参数</h2>
        <input v-model="form.examName" class="input" placeholder="考试标题" />
        <div class="grid md:grid-cols-2 gap-4">
          <input v-model="form.startTime" type="datetime-local" class="input" />
          <input v-model="form.endTime" type="datetime-local" class="input" />
        </div>
        <textarea v-model="form.instructions" class="input min-h-[140px]" placeholder="考试说明" />
      </section>

      <section class="panel p-6 space-y-4">
        <h2 class="text-xl font-semibold">2. 选择题目和分值</h2>
        <div class="overflow-hidden rounded-xl border border-slate-200">
          <table class="table w-full">
            <thead><tr><th>选择</th><th>题目</th><th>难度</th><th>分值</th></tr></thead>
            <tbody>
              <tr v-for="question in questions" :key="question.questionId">
                <td><input v-model="selectedQuestionIds" type="checkbox" :value="question.questionId" /></td>
                <td>{{ question.title }}</td>
                <td>{{ question.difficulty }}</td>
                <td><input v-model.number="scores[question.questionId]" class="input max-w-24" type="number" min="0" max="100" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel p-6 space-y-4">
        <h2 class="text-xl font-semibold">3. 发布</h2>
        <p class="text-sm text-slate-500">实验版默认分配给种子学生 student1，适合完整演示教师创建考试、学生作答、成绩回收流程。</p>
        <div class="flex flex-wrap gap-3">
          <button class="btn-secondary rounded-lg px-4 py-2" @click="saveDraft">保存草稿</button>
          <button class="btn-primary rounded-lg px-4 py-2" @click="publish">发布考试</button>
        </div>
        <div v-if="message" class="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm">{{ message }}</div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const form = reactive({
  examName: 'CS304 Database Systems - Midterm',
  startTime: '2026-05-26T09:00',
  endTime: '2026-05-26T11:00',
  instructions: '请使用标准 MySQL 语法完成题目。',
})
const questions = ref<any[]>([])
const selectedQuestionIds = ref<number[]>([1])
const scores = reactive<Record<number, number>>({ 1: 100 })
const message = ref('')

function payload() {
  return {
    examName: form.examName,
    startTime: form.startTime + ':00',
    endTime: form.endTime + ':00',
    instructions: form.instructions,
    durationMinutes: 120,
    lockdownEnabled: false,
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
  if (!selectedQuestionIds.value.length && questions.value[0]) {
    selectedQuestionIds.value = [questions.value[0].questionId]
  }
})
</script>
