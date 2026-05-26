<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>考试列表</h2>
      <el-button v-if="role === 1" type="primary" @click="dialogVisible = true">创建考试</el-button>
    </div>

    <el-table :data="exams" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="考试名称" />
      <el-table-column prop="start_time" label="开始时间" width="180" />
      <el-table-column prop="end_time" label="结束时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="$router.push(`/exam/${row.id}`)">查看</el-button>
          <el-button v-if="role === 1" type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="创建考试" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.start_time" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.end_time" type="datetime" style="width:100%" /></el-form-item>
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
import { getContestList, createContest, deleteContest } from '../../api/exams'

const store = useUserStore()
const role = computed(() => store.user?.role)
const exams = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const creating = ref(false)

const form = ref({ name: '', start_time: '', end_time: '' })

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getContestList({ user_id: store.user.id, user_role: store.user.role })
    exams.value = res.data
  } catch {} finally { loading.value = false }
}

async function handleCreate() {
  if (!form.value.name || !form.value.start_time || !form.value.end_time) {
    ElMessage.warning('请填写完整信息'); return
  }
  creating.value = true
  try {
    await createContest({ ...form.value, teacher_id: store.user.id })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchList()
  } catch {} finally { creating.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await deleteContest(id)
    ElMessage.success('已删除')
    fetchList()
  } catch {}
}
</script>
