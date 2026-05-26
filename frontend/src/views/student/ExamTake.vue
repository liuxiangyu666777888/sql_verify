<template>
  <AppLayout>
    <div class="grid lg:grid-cols-[1fr_1fr] gap-6">
      <section class="panel p-6 space-y-4">
        <p class="text-sm text-slate-500">Exam #{{ $route.params.id }}</p>
        <h1 class="text-2xl font-bold">考试作答</h1>
        <div class="rounded-xl border border-slate-200 bg-slate-50 p-3 text-sm">
          当前题目：{{ currentQuestion?.title || '加载中' }}
        </div>
        <textarea v-model="sql" class="input min-h-[360px] font-mono" />
        <div class="flex gap-3">
          <button class="btn-secondary rounded-lg px-4 py-2" @click="run">运行</button>
          <button class="btn-primary rounded-lg px-4 py-2" @click="submit">提交</button>
        </div>
      </section>
      <section class="panel p-6 space-y-4">
        <h2 class="text-xl font-semibold">结果</h2>
        <div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
          <div class="font-semibold">{{ result.status }}</div>
          <div class="mt-2">得分：{{ result.score }}</div>
          <div class="mt-2 text-red-600" v-if="result.errorMessage">{{ result.errorMessage }}</div>
          <div v-if="result.resultPreview?.columns?.length" class="mt-4 overflow-auto">
            <table class="table w-full bg-white">
              <thead><tr><th v-for="col in result.resultPreview.columns" :key="col">{{ col }}</th></tr></thead>
              <tbody>
                <tr v-for="(row, idx) in result.resultPreview.rows" :key="idx">
                  <td v-for="(cell, cidx) in row" :key="cidx">{{ cell }}</td>
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
import { ref } from 'vue'
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const sql = ref('SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary FROM Employee e JOIN Department d ON e.departmentId = d.id WHERE (e.departmentId, e.salary) IN (SELECT departmentId, MAX(salary) FROM Employee GROUP BY departmentId)')
const result = ref<{ status: string; score: number; errorMessage?: string; resultPreview?: any }>({ status: '待提交', score: 0 })
const route = useRoute()
const examId = Number(route.params.id)
const currentQuestion = ref<any>(null)

onMounted(async () => {
  const { data } = await http.get(`/exams/${examId}/questions`)
  currentQuestion.value = data.data[0]
})

async function run() {
  const questionId = currentQuestion.value?.questionId || 1
  const { data } = await http.post('/judge/run', { questionId, examId, sqlCode: sql.value })
  result.value = data.data
}

async function submit() {
  const questionId = currentQuestion.value?.questionId || 1
  const { data } = await http.post('/submissions', { questionId, examId, sqlCode: sql.value })
  result.value = data.data
}
</script>
