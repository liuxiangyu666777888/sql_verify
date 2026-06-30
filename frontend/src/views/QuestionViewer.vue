<template>
  <AppLayout>
    <div class="page-inner">
      <div v-if="loading" class="panel p-6">
        <div class="muted">正在加载题目...</div>
      </div>
      <div v-else-if="loadError" class="panel p-6">
        <div class="text-sm font-semibold text-red-700">{{ loadError }}</div>
        <button class="btn-secondary mt-4" @click="load">
          <span class="material-symbols-outlined">refresh</span>
          重新加载
        </button>
      </div>
      <div v-else class="split-editor">
        <section class="panel overflow-hidden">
          <div class="panel-header">
            <div class="flex items-center gap-3">
              <h1 class="headline-md">{{ question.title || '题目详情' }}</h1>
              <span class="rounded border border-outline-variant bg-surface-low px-2 py-1 text-xs font-bold">
                {{ question.difficulty || 'MEDIUM' }}
              </span>
            </div>
          </div>
          <div class="p-6 space-y-5">
            <p class="leading-7 text-[15px] text-on-surface-variant">{{ question.description }}</p>
            <div class="code-box">{{ question.sourceSchemaSql }}</div>
            <div class="rounded-lg border border-dashed border-outline-variant bg-surface-lowest p-4">
              <div class="label mb-2">示例输出</div>
              <div class="text-sm text-on-surface-variant">以标准 MySQL 查询结果为准，提交后按测试用例自动判分。</div>
            </div>
          </div>
        </section>

        <div class="split-divider">
          <div class="h-8 w-0.5 rounded-full bg-outline-variant"></div>
        </div>

        <section class="panel overflow-hidden">
          <div class="editor-shell">
            <div class="editor-toolbar">
              <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-on-surface-variant">code_blocks</span>
                <span class="label normal-case tracking-normal">MySQL</span>
              </div>
              <div class="flex gap-2">
                <button class="icon-btn"><span class="material-symbols-outlined">format_align_left</span></button>
                <button class="icon-btn"><span class="material-symbols-outlined">settings</span></button>
                <button class="icon-btn"><span class="material-symbols-outlined">fullscreen</span></button>
              </div>
            </div>

            <div class="editor-body">
              <div class="line-gutter">
                <div v-for="line in 18" :key="line">{{ line }}</div>
              </div>
              <textarea v-model="sql" class="editor-area"></textarea>
            </div>

            <div class="result-strip">
              <div class="flex gap-6">
                <button class="h-11 border-b-2 border-transparent text-sm text-on-surface-variant">测试用例</button>
                <button class="h-11 border-b-2 border-primary text-sm font-bold text-primary">执行结果</button>
              </div>
              <div class="flex gap-2">
                <button class="btn-secondary" @click="run">
                  <span class="material-symbols-outlined">play_arrow</span>
                  运行自测
                </button>
                <button class="btn-primary" @click="submit">
                  <span class="material-symbols-outlined">cloud_upload</span>
                  提交代码
                </button>
              </div>
            </div>

            <div class="space-y-4 bg-surface-lowest p-4">
              <div class="status-pill" :class="statusClass">
                <span class="material-symbols-outlined text-[16px]">check_circle</span>
                {{ statusText }}
              </div>
              <div class="result-meta">
                <span>执行用时: <strong>{{ result.runtimeMs || 0 }} ms</strong></span>
                <span>得分: <strong>{{ result.score }}</strong></span>
              </div>
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
              <div v-if="result.errorMessage" class="text-sm text-red-600">{{ result.errorMessage }}</div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../components/AppLayout.vue'
import http from '../api/http'

const route = useRoute()
const sql = ref('SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary FROM Employee e JOIN Department d ON e.departmentId = d.id WHERE (e.departmentId, e.salary) IN (SELECT departmentId, MAX(salary) FROM Employee GROUP BY departmentId);')
const result = ref<{ status: string; score: number; runtimeMs?: number; errorMessage?: string; resultPreview?: any }>({ status: '待执行', score: 0 })
const loading = ref(true)
const loadError = ref('')
const question = ref<{ title: string; description: string; difficulty?: string; sourceSchemaSql: string }>({
  title: '',
  description: '',
  difficulty: '',
  sourceSchemaSql: '',
})

const statusText = computed(() => {
  if (result.value.status === 'AC') return 'Accepted'
  if (result.value.status === 'WA') return 'Wrong Answer'
  if (result.value.status === 'FORBIDDEN') return 'Forbidden'
  if (result.value.status === 'TLE') return 'Time Limit Exceeded'
  return result.value.status || 'Pending'
})

const statusClass = computed(() => {
  if (result.value.status === 'AC') return 'bg-green-700'
  if (result.value.status === 'WA' || result.value.status === 'ERROR') return 'bg-red-600'
  return 'bg-slate-600'
})

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await http.get(`/questions/${route.params.id}`)
    question.value = data.data
  } catch (err: any) {
    loadError.value = err?.response?.data?.message || err?.message || '题目加载失败'
  } finally {
    loading.value = false
  }
}
load()

async function run() {
  try {
    const { data } = await http.post('/judge/run', { questionId: Number(route.params.id), sqlCode: sql.value })
    result.value = data.data
  } catch (err: any) {
    result.value = {
      status: err?.response?.status === 403 ? 'FORBIDDEN' : 'ERROR',
      score: 0,
      errorMessage: err?.response?.data?.message || err?.message || '请求失败',
    }
  }
}

async function submit() {
  try {
    const { data } = await http.post('/submissions', { questionId: Number(route.params.id), sqlCode: sql.value })
    result.value = data.data
  } catch (err: any) {
    result.value = {
      status: err?.response?.status === 403 ? 'FORBIDDEN' : 'ERROR',
      score: 0,
      errorMessage: err?.response?.data?.message || err?.message || '请求失败',
    }
  }
}
</script>
