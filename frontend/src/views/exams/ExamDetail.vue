<template>
  <div v-loading="loading">
    <el-button @click="$router.back()" style="margin-bottom: 16px">← 返回</el-button>
    <h2>{{ exam.name }}</h2>
    <p>时间：{{ exam.start_time }} ~ {{ exam.end_time }}</p>

    <h3 style="margin-top: 24px">题目列表</h3>
    <el-table :data="questions" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="题目" />
      <el-table-column prop="difficulty" label="难度" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="$router.push(`/question/${row.id}`)">作答</el-button>
        </template>
      </el-table-column>
    </el-table>

    <h3 v-if="role === 1" style="margin-top: 24px">成绩排名</h3>
    <el-table v-if="role === 1" :data="scores" style="width: 100%">
      <el-table-column prop="rank" label="排名" width="80" />
      <el-table-column prop="id" label="学生ID" />
      <el-table-column prop="score" label="分数" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getContest, getContestQuestions, getContestScores } from '../../api/exams'
import { getQuestion } from '../../api/questions'

const route = useRoute()
const store = useUserStore()
const role = computed(() => store.user?.role)
const exam = ref({})
const questions = ref([])
const scores = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const examId = route.params.id
    const eRes = await getContest({ contest_id: examId })
    exam.value = eRes.data

    const qRes = await getContestQuestions({ contest_id: examId })
    if (qRes.data.questionIds) {
      const qs = []
      for (const qid of qRes.data.questionIds) {
        try {
          const r = await getQuestion({ question_id: qid, student_id: store.user.id })
          qs.push(r.data)
        } catch {}
      }
      questions.value = qs
    }

    if (role.value === 1) {
      const sRes = await getContestScores({ contest_id: examId, user_id: store.user.id, user_role: store.user.role })
      scores.value = sRes.data
    }
  } catch {} finally { loading.value = false }
})
</script>
