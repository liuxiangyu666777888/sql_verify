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
            <div class="rounded-lg border border-outline-variant bg-surface-low p-4">
              <div class="label mb-2">当前题目</div>
              <div class="font-semibold">{{ currentQuestion?.title || '加载中' }}</div>
              <p class="muted mt-2 text-sm">{{ currentQuestion?.description || '请等待题目加载完成。' }}</p>
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
const currentQuestion = ref<any>(null)
const sql = ref('SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary FROM Employee e JOIN Department d ON e.departmentId = d.id WHERE (e.departmentId, e.salary) IN (SELECT departmentId, MAX(salary) FROM Employee GROUP BY departmentId);')
const result = ref<{ status: string; score: number; runtimeMs?: number; errorMessage?: string; resultPreview?: any }>({
  status: '待提交',
  score: 0,
})

const statusClass = computed(() => {
  if (result.value.status === 'AC') return 'bg-green-700'
  if (['WA', 'ERROR', 'FORBIDDEN', 'TLE'].includes(result.value.status)) return 'bg-red-600'
  return 'bg-slate-600'
})

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
