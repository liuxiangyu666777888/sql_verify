<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>班级管理</h2>
      <el-button type="primary" @click="showCreateDialog">创建班级</el-button>
    </div>

    <el-table :data="classes" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="班级名称" />
      <el-table-column prop="student_count" label="学生数" width="100" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="showStudents(row)">查看学生</el-button>
          <el-button type="success" size="small" @click="showAddStudents(row)">添加学生</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 学生列表弹窗 -->
    <el-dialog v-model="studentVisible" title="班级学生" width="500px">
      <el-table :data="students">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleRemoveStudent(row.id)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 创建 / 添加学生弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form v-if="dialogMode === 'create'" :model="createForm" label-width="80px">
        <el-form-item label="班级名称"><el-input v-model="createForm.name" /></el-form-item>
      </el-form>
      <div v-else>
        <el-input v-model="studentIds" type="textarea" rows="4" placeholder="输入学生ID，用逗号分隔，如：20001,20002,20003" />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDialogConfirm" :loading="dialogLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getClassList, createClass, deleteClass, getClassStudents, addClassStudents, removeClassStudent } from '../../api/classes'

const store = useUserStore()
const classes = ref([])
const students = ref([])
const loading = ref(false)
const studentVisible = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogMode = ref('create')
const dialogTitle = ref('')
const createForm = ref({ name: '' })
const studentIds = ref('')
const currentClass = ref(null)

onMounted(fetchClasses)

async function fetchClasses() {
  loading.value = true
  try {
    const res = await getClassList({ teacher_id: store.user.id })
    classes.value = res.data
  } catch {} finally { loading.value = false }
}

async function showStudents(row) {
  currentClass.value = row
  const res = await getClassStudents({ class_id: row.id })
  students.value = res.data
  studentVisible.value = true
}

function showCreateDialog() {
  dialogMode.value = 'create'
  dialogTitle.value = '创建班级'
  createForm.value = { name: '' }
  dialogVisible.value = true
}

function showAddStudents(row) {
  currentClass.value = row
  dialogMode.value = 'addStudents'
  dialogTitle.value = `添加学生到 ${row.name}`
  studentIds.value = ''
  dialogVisible.value = true
}

async function handleDialogConfirm() {
  dialogLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createClass({ name: createForm.value.name, teacher_id: store.user.id })
      ElMessage.success('班级创建成功')
      fetchClasses()
    } else {
      const ids = studentIds.value.split(',').map(s => parseInt(s.trim())).filter(Boolean)
      await addClassStudents({ class_id: currentClass.value.id, student_ids: ids })
      ElMessage.success('学生添加成功')
      showStudents(currentClass.value)
    }
    dialogVisible.value = false
  } catch {} finally { dialogLoading.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await deleteClass({ class_id: id })
    ElMessage.success('已删除')
    fetchClasses()
  } catch {}
}

async function handleRemoveStudent(sid) {
  await removeClassStudent({ class_id: currentClass.value.id, student_id: sid })
  ElMessage.success('已移除')
  showStudents(currentClass.value)
}
</script>
