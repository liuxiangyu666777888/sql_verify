<template>
  <AppLayout>
    <div class="space-y-6">
      <header class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm text-slate-500">Score Overview</p>
          <h1 class="text-3xl font-bold">成绩统计</h1>
        </div>
        <select v-model="selectedExamId" class="input md:max-w-sm" @change="loadScores">
          <option v-for="exam in exams" :key="exam.examId" :value="exam.examId">{{ exam.examName }}</option>
        </select>
      </header>
      <section class="panel overflow-hidden">
        <table class="table w-full">
          <thead><tr><th>考试</th><th>学生</th><th>状态</th><th>成绩</th></tr></thead>
          <tbody>
            <tr v-for="item in scores" :key="item.studentId">
              <td>{{ currentExamName }}</td>
              <td>{{ item.realName || item.username }}</td>
              <td>{{ item.status }}</td>
              <td>{{ item.finalScore }}</td>
            </tr>
            <tr v-if="!scores.length">
              <td colspan="4" class="text-slate-500">暂无成绩数据</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const exams = ref<any[]>([])
const scores = ref<any[]>([])
const selectedExamId = ref<number | ''>('')
const currentExamName = computed(() => exams.value.find((item) => item.examId === Number(selectedExamId.value))?.examName || '')

async function loadScores() {
  if (!selectedExamId.value) {
    scores.value = []
    return
  }
  const { data } = await http.get(`/exams/${selectedExamId.value}/scores`)
  scores.value = data.data
}

onMounted(async () => {
  const { data } = await http.get('/exams')
  exams.value = data.data
  selectedExamId.value = exams.value[0]?.examId || ''
  await loadScores()
})
</script>
