<template>
  <div class="app-shell min-h-screen p-6">
    <div class="grid lg:grid-cols-[1.15fr_1fr] gap-6 max-w-7xl mx-auto">
      <section class="panel p-6 space-y-4">
        <div>
          <p class="text-sm text-slate-500">Problem #{{ $route.params.id }}</p>
          <h1 class="text-3xl font-bold mt-1">{{ question.title }}</h1>
        </div>
        <p class="leading-7 text-slate-700">{{ question.description }}</p>
        <div class="border rounded-xl p-4 bg-slate-50 text-sm font-mono overflow-auto">
          {{ question.sourceSchemaSql }}
        </div>
      </section>
      <section class="panel p-6 space-y-4">
        <div class="flex items-center justify-between">
          <h2 class="text-xl font-semibold">SQL 编辑器</h2>
          <span class="text-sm text-slate-500">MySQL</span>
        </div>
        <textarea v-model="sql" class="w-full min-h-[280px] border rounded-xl p-4 font-mono text-sm" />
        <div class="flex gap-3">
          <button class="btn-secondary rounded-lg px-4 py-2" @click="run">运行自测</button>
          <button class="btn-primary rounded-lg px-4 py-2" @click="submit">提交代码</button>
        </div>
        <div class="border rounded-xl p-4 bg-slate-50 text-sm">
          <div class="font-medium mb-2">{{ result.status }}</div>
          <div>Score: {{ result.score }}</div>
          <div v-if="result.errorMessage" class="text-red-600 mt-2">{{ result.errorMessage }}</div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import http from '../api/http'

const sql = ref('SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary FROM Employee e JOIN Department d ON e.departmentId = d.id WHERE (e.departmentId, e.salary) IN (SELECT departmentId, MAX(salary) FROM Employee GROUP BY departmentId);')
const result = ref<{ status: string; score: number; errorMessage?: string }>({ status: '待执行', score: 0 })
const question = ref<{ title: string; description: string; sourceSchemaSql: string }>({
  title: '',
  description: '',
  sourceSchemaSql: '',
})

async function load() {
  const { data } = await http.get(`/questions/${(window.location.pathname.split('/').pop())}`)
  question.value = data.data
}

load()

async function run() {
  const { data } = await http.post('/judge/run', { questionId: 1, sqlCode: sql.value })
  result.value = data.data
}

async function submit() {
  const { data } = await http.post('/judge/run', { questionId: 1, sqlCode: sql.value })
  result.value = data.data
}
</script>
