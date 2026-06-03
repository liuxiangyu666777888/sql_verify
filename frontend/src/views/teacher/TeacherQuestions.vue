<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header>
        <div class="label">Question Management</div>
        <h1 class="headline-lg">题库管理</h1>
      </header>
      <section class="panel overflow-hidden">
        <table class="table w-full">
          <thead><tr><th>ID</th><th>标题</th><th>难度</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="item in list" :key="item.questionId">
              <td>{{ item.questionId }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.difficulty }}</td>
              <td><button class="btn-secondary" @click="go(item.questionId)">查看</button></td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const list = ref<any[]>([])
const router = useRouter()
onMounted(async () => {
  const { data } = await http.get('/questions')
  list.value = data.data
})

function go(id: number) {
  router.push(`/problems/${id}`)
}
</script>
