<template>
  <div v-loading="loading">
    <el-button @click="$router.back()" style="margin-bottom: 16px">← 返回</el-button>
    <h2>{{ question.title }}</h2>
    <el-card style="margin-top: 12px">
      <p><strong>描述：</strong>{{ question.description }}</p>
      <p><strong>难度：</strong>{{ question.difficulty }}</p>
    </el-card>

    <h3 style="margin-top: 24px">提交 SQL</h3>
    <el-input v-model="sql" type="textarea" rows="6" placeholder="请输入你的 SQL 语句..." />
    <el-button type="primary" style="margin-top: 12px" @click="handleSubmit" :loading="submitting">提交并判题</el-button>

    <el-card v-if="result" style="margin-top: 24px" :style="{ borderColor: resultColor }">
      <h3>判题结果：<span :style="{ color: resultColor }">{{ resultLabel }}</span></h3>
      <p>通过率：{{ (passRate * 100).toFixed(0) }}%</p>
    </el-card>

    <h3 style="margin-top: 24px">提交记录</h3>
    <el-table :data="submits" style="width: 100%">
      <el-table-column prop="submit_time" label="时间" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="pass_rate" label="通过率" width="100">
        <template #default="{ row }">{{ (row.pass_rate * 100).toFixed(0) }}%</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getQuestion } from '../../api/questions'
import { submit, getSubmitList, judge } from '../../api/judge'

const route = useRoute()
const store = useUserStore()
const question = ref({})
const sql = ref('')
const loading = ref(false)
const submitting = ref(false)
const submites = ref([])
const result = ref(null)
const passRate = ref(0)

const statusMap = { '-1': '等待中', 0: 'Accepted', 1: '运行错误', 2: '答案错误', 3: '超时', 4: '内存超限' }
const resultLabels = { 0: 'Accepted', 1: '运行错误', 2: '答案错误', 3: '超时', 4: '内存超限' }

const resultLabel = computed(() => resultLabels[result.value?.status] || '')
const resultColor = computed(() => result.value?.status === 0 ? '#67C23A' : '#F56C6C')

function statusLabel(s) { return statusMap[s] || '未知' }
function statusTag(s) { return s === 0 ? 'success' : s === -1 ? 'info' : 'danger' }

onMounted(async () => {
  loading.value = true
  try {
    const res = await getQuestion({ question_id: route.params.id, student_id: store.user.id })
    question.value = res.data
  } catch {} finally { loading.value = false }
})

async function handleSubmit() {
  if (!sql.value.trim()) { ElMessage.warning('请输入 SQL'); return }
  submitting.value = true
  try {
    const sRes = await submit({
      student_id: store.user.id,
      question_id: question.value.id,
      exam_id: null,
      submit_sql: sql.value,
      submit_time: new Date().toISOString()
    })
    const submitId = sRes.data.submit_id

    const jRes = await judge({
      submit_id: submitId,
      submit_sql: sql.value,
      question_id: question.value.id,
      create_code: question.value.create_code
    })
    result.value = { status: jRes.data.result[1] === 'Accepted' ? 0 : 1 }
    passRate.value = jRes.data.pass_rate
  } catch {} finally { submitting.value = false }
}
</script>
