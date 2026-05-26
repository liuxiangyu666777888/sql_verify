<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>题库</h2>
      <el-button v-if="role === 1 || role === 2" type="primary" @click="dialogVisible = true">新增题目</el-button>
    </div>

    <el-table :data="questions" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="题目" />
      <el-table-column prop="difficulty" label="难度" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="$router.push(`/question/${row.id}`)">作答</el-button>
          <el-button v-if="role === 1 || role === 2" type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增题目" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="建表语句"><el-input v-model="form.create_code" type="textarea" rows="4" /></el-form-item>
        <el-form-item label="难度"><el-input-number v-model="form.difficulty" :min="1" :max="5" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getQuestionList, createQuestion, deleteQuestion } from '../../api/questions'

const store = useUserStore()
const role = computed(() => store.user?.role)
const questions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const creating = ref(false)

const form = ref({ title: '', description: '', create_code: '', difficulty: 1 })

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const params = role.value === 1 || role.value === 2
      ? { teacher_id: store.user.id }
      : { student_id: store.user.id }
    const res = await getQuestionList(params)
    questions.value = res.data
  } catch {} finally { loading.value = false }
}

async function handleCreate() {
  creating.value = true
  try {
    await createQuestion({ ...form.value, teacher_id: store.user.id })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchList()
  } catch {} finally { creating.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await deleteQuestion(id, { user_id: store.user.id })
    ElMessage.success('已删除')
    fetchList()
  } catch {}
}
</script>
