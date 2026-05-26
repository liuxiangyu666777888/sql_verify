<template>
  <div v-loading="loading">
    <h2>教师统计面板</h2>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" style="text-align: center">
          <h1 :style="{ color: stat.color }">{{ stat.value }}</h1>
          <p style="color: #999">{{ stat.label }}</p>
        </el-card>
      </el-col>
    </el-row>

    <h3 style="margin-top: 32px">班级统计</h3>
    <el-table :data="data.class_stats || []">
      <el-table-column prop="name" label="班级" />
      <el-table-column prop="student_count" label="学生数" />
    </el-table>

    <h3 style="margin-top: 24px">考试统计</h3>
    <el-table :data="data.exam_stats || []">
      <el-table-column prop="name" label="考试" />
      <el-table-column prop="question_count" label="题目数" />
      <el-table-column prop="student_count" label="参与学生" />
    </el-table>

    <h3 style="margin-top: 24px">最近提交</h3>
    <el-table :data="data.recent_submissions || []">
      <el-table-column prop="student_name" label="学生" width="120" />
      <el-table-column prop="question_title" label="题目" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submit_time" label="时间" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getDashboard } from '../../api/users'

const store = useUserStore()
const loading = ref(false)
const data = ref({})
const statusMap = { '-1': '等待中', 0: 'AC', 1: 'RE', 2: 'WA', 3: 'TLE', 4: 'MLE' }

const stats = computed(() => [
  { label: '班级数', value: data.value.class_count || 0, color: '#409EFF' },
  { label: '学生数', value: data.value.total_students || 0, color: '#67C23A' },
  { label: '题目数', value: data.value.question_count || 0, color: '#E6A23C' },
  { label: '总通过率', value: (data.value.overall_pass_rate || 0) + '%', color: '#F56C6C' }
])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDashboard({ teacher_id: store.user.id })
    data.value = res.data
  } catch {} finally { loading.value = false }
})
</script>
