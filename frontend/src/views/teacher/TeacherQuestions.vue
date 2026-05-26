<template>
  <AppLayout>
    <div class="space-y-6">
      <header class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500">Question Management</p>
          <h1 class="text-3xl font-bold">题库管理</h1>
        </div>
      </header>
      <section class="panel overflow-hidden">
        <table class="table w-full">
          <thead><tr><th>ID</th><th>标题</th><th>难度</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="item in list" :key="item.questionId">
              <td>{{ item.questionId }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.difficulty }}</td>
              <td><button class="btn-secondary rounded-lg px-3 py-1.5" @click="go(item.questionId)">查看</button></td>
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
