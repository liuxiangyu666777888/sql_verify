<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>社区</h2>
      <el-button type="primary" @click="showEditor()">发布文章</el-button>
    </div>

    <el-table :data="articles" v-loading="loading">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="showDetail(row)">查看</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" :title="currentArticle.title" width="600px">
      <div v-html="currentArticle.content" style="white-space: pre-wrap" />
    </el-dialog>

    <el-dialog v-model="editorVisible" :title="editingId ? '编辑文章' : '发布文章'" width="600px">
      <el-form :model="articleForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="articleForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="articleForm.content" type="textarea" rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getCommunityList, createArticle, deleteArticle } from '../../api/community'
const store = useUserStore()

const articles = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const editorVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const currentArticle = ref({})
const articleForm = ref({ title: '', content: '' })

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getCommunityList()
    articles.value = res.data
  } catch {} finally { loading.value = false }
}

function showDetail(row) { currentArticle.value = row; detailVisible.value = true }
function showEditor(row) {
  editingId.value = row?.id || null
  articleForm.value = row ? { title: row.title, content: row.content } : { title: '', content: '' }
  editorVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await createArticle({
      ...articleForm.value,
      user_id: store.user.id,
      publish_time: new Date().toISOString(),
      last_modify_time: new Date().toISOString()
    })
    ElMessage.success('发布成功')
    editorVisible.value = false
    fetchList()
  } catch {} finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await deleteArticle({ article_id: id })
    ElMessage.success('已删除')
    fetchList()
  } catch {}
}
</script>
