<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <section class="welcome-card">
        <div class="welcome-content">
          <div>
            <div class="label">Question Management</div>
            <h1 class="headline-lg">题库管理</h1>
            <p class="muted mt-2">维护 SQL 题目、参考答案和教师可见的测试用例。</p>
          </div>
          <button class="btn-primary" @click="startCreate">
            <span class="material-symbols-outlined">add</span>
            新增题目
          </button>
        </div>
      </section>

      <div class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
        <section class="panel overflow-hidden">
          <div class="panel-header">
            <div class="grid gap-3 md:grid-cols-[minmax(0,1fr)_120px]">
              <input v-model="keyword" class="form-input" placeholder="搜索题目标题" @keyup.enter="loadQuestions" />
              <button class="btn-secondary" @click="loadQuestions">
                <span class="material-symbols-outlined">search</span>
                搜索
              </button>
            </div>
          </div>
          <table class="table w-full">
            <thead>
              <tr>
                <th>ID</th>
                <th>标题</th>
                <th>难度</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in list" :key="item.questionId">
                <td>{{ item.questionId }}</td>
                <td class="font-semibold">{{ item.title }}</td>
                <td>{{ item.difficulty }}</td>
                <td>
                  <div class="flex flex-wrap gap-2">
                    <button class="btn-secondary" @click="editQuestion(item.questionId)">编辑</button>
                    <button class="btn-secondary" @click="previewQuestion(item.questionId)">查看</button>
                    <button class="btn-secondary" @click="deleteQuestion(item.questionId)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="!list.length" class="p-6 muted">暂无题目</div>
          <div class="flex items-center justify-between gap-4 border-t border-outline-variant p-4">
            <button class="btn-secondary" :disabled="page <= 1" @click="loadQuestions(page - 1)">
              <span class="material-symbols-outlined">chevron_left</span>
              上一页
            </button>
            <div class="label">第 {{ page }} 页 / 共 {{ total }} 题</div>
            <button class="btn-secondary" :disabled="page * size >= total" @click="loadQuestions(page + 1)">
              下一页
              <span class="material-symbols-outlined">chevron_right</span>
            </button>
          </div>
        </section>

        <aside class="panel overflow-hidden">
          <div class="panel-header">
            <h2 class="headline-md">{{ editingId ? '编辑题目' : '新增题目' }}</h2>
            <p class="muted mt-1">保存后学生端仍不会看到参考答案。</p>
          </div>
          <div class="grid gap-4 p-6">
            <div v-if="message" class="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-800">
              {{ message }}
            </div>
            <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
              {{ error }}
            </div>

            <label class="grid gap-2">
              <span class="label text-on-surface">标题</span>
              <input v-model="form.title" class="form-input" />
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">难度</span>
              <select v-model="form.difficulty" class="form-input">
                <option value="EASY">EASY</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HARD">HARD</option>
              </select>
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">描述</span>
              <textarea v-model="form.description" class="form-input min-h-28 resize-y"></textarea>
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">建表 SQL</span>
              <textarea v-model="form.sourceSchemaSql" class="form-input min-h-24 resize-y font-mono text-sm"></textarea>
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">参考答案</span>
              <textarea v-model="form.answerSql" class="form-input min-h-24 resize-y font-mono text-sm"></textarea>
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">标签 JSON</span>
              <input v-model="form.tags" class="form-input font-mono text-sm" placeholder='["JOIN","GROUP BY"]' />
            </label>

            <div v-if="testCases.length" class="rounded-lg border border-outline-variant bg-surface-low p-4">
              <div class="label mb-3">测试用例</div>
              <div class="grid gap-3">
                <details v-for="testCase in testCases" :key="testCase.caseId" class="rounded border border-outline-variant bg-white p-3">
                  <summary class="cursor-pointer font-semibold">Case #{{ testCase.caseOrder }} {{ testCase.isHidden ? '(Hidden)' : '' }}</summary>
                  <pre class="code-box mt-3">{{ testCase.inputSql }}</pre>
                </details>
              </div>
            </div>

            <div class="flex flex-wrap gap-3">
              <button class="btn-primary" :disabled="saving" @click="saveQuestion">
                <span class="material-symbols-outlined">save</span>
                {{ saving ? '保存中...' : '保存题目' }}
              </button>
              <button class="btn-secondary" @click="resetForm">重置</button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const route = useRoute()
const list = ref<any[]>([])
const testCases = ref<any[]>([])
const keyword = ref('')
const editingId = ref<number | null>(null)
const saving = ref(false)
const message = ref('')
const error = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const form = reactive({
  title: '',
  description: '',
  difficulty: 'MEDIUM',
  answerSql: '',
  sourceSchemaSql: '',
  tags: '[]',
  visible: 1,
})

onMounted(() => {
  keyword.value = String(route.query.keyword || '')
  loadQuestions(1)
})

watch(() => route.query.keyword, (value) => {
  keyword.value = String(value || '')
  loadQuestions(1)
})

async function loadQuestions(nextPage = 1) {
  page.value = nextPage
  const { data } = await http.get('/questions', { params: { keyword: keyword.value, page: page.value, size: size.value } })
  list.value = data.data.items || []
  total.value = data.data.total || 0
}

function startCreate() {
  resetForm()
}

async function previewQuestion(id: number) {
  await editQuestion(id)
}

async function editQuestion(id: number) {
  message.value = ''
  error.value = ''
  const { data } = await http.get(`/questions/${id}`)
  const detail = data.data
  editingId.value = id
  form.title = detail.title || ''
  form.description = detail.description || ''
  form.difficulty = detail.difficulty || 'MEDIUM'
  form.answerSql = detail.answerSql || ''
  form.sourceSchemaSql = detail.sourceSchemaSql || ''
  form.tags = typeof detail.tags === 'string' ? detail.tags : JSON.stringify(detail.tags || [])
  form.visible = 1
  testCases.value = detail.testCases || []
}

async function saveQuestion() {
  message.value = ''
  error.value = validate()
  if (error.value) return

  saving.value = true
  try {
    const payload = {
      ...form,
      tags: normalizeTags(form.tags),
    }
    const request = editingId.value
      ? http.put(`/questions/${editingId.value}`, payload)
      : http.post('/questions', payload)
    const { data } = await request
    if (data.code !== 0) {
      throw new Error(data.message || '保存题目失败')
    }
    editingId.value = data.data.questionId
    message.value = '题目已保存'
    await loadQuestions(page.value)
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '保存题目失败'
  } finally {
    saving.value = false
  }
}

async function deleteQuestion(id: number) {
  if (!window.confirm('确认删除这道题？已关联考试或提交记录的历史数据会保留。')) return
  error.value = ''
  message.value = ''
  try {
    const { data } = await http.delete(`/questions/${id}`)
    if (data.code !== 0) {
      throw new Error(data.message || '删除题目失败')
    }
    if (editingId.value === id) resetForm()
    message.value = '题目已删除'
    await loadQuestions(page.value)
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '删除题目失败'
  }
}

function resetForm() {
  editingId.value = null
  testCases.value = []
  form.title = ''
  form.description = ''
  form.difficulty = 'MEDIUM'
  form.answerSql = ''
  form.sourceSchemaSql = ''
  form.tags = '[]'
  form.visible = 1
  message.value = ''
  error.value = ''
}

function validate() {
  if (!form.title.trim()) return '标题不能为空'
  if (!form.description.trim()) return '描述不能为空'
  if (!form.answerSql.trim()) return '参考答案不能为空'
  try {
    JSON.parse(normalizeTags(form.tags))
  } catch (_) {
    return '标签必须是合法 JSON'
  }
  return ''
}

function normalizeTags(value: string) {
  return value.trim() || '[]'
}
</script>
