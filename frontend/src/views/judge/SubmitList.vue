<template>
  <div>
    <h2>提交记录</h2>
    <el-form inline style="margin-bottom: 16px">
      <el-form-item label="筛选">
        <el-select v-model="filter" @change="fetchList" placeholder="全部提交" clearable>
          <el-option label="我的提交" value="mine" />
          <el-option v-if="role === 1" label="全部提交" value="all" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="submits" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="student_name" label="学生" width="120" />
      <el-table-column prop="question_title" label="题目" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="通过率" width="100">
        <template #default="{ row }">{{ (row.pass_rate * 100).toFixed(0) }}%</template>
      </el-table-column>
      <el-table-column prop="submit_time" label="时间" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getSubmitList } from '../../api/judge'

const store = useUserStore()
const role = computed(() => store.user?.role)
const submits = ref([])
const loading = ref(false)
const filter = ref('mine')

const statusMap = { '-1': '等待中', 0: 'Accepted', 1: '运行错误', 2: '答案错误', 3: '超时', 4: '内存超限' }
function statusLabel(s) { return statusMap[s] || '未知' }
function statusTag(s) { return s === 0 ? 'success' : s === -1 ? 'info' : 'danger' }

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const params = filter.value === 'all' ? { fetchall: true } : { user_id: store.user.id }
    const res = await getSubmitList(params)
    submits.value = res.data
  } catch {} finally { loading.value = false }
}
</script>
