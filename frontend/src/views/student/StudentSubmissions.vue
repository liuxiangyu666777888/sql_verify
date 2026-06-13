<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header>
        <div class="label">My Submissions</div>
        <h1 class="headline-lg">提交记录</h1>
      </header>
      <section class="panel overflow-hidden">
        <table class="table w-full">
          <thead><tr><th>题目</th><th>状态</th><th>得分</th><th>时间</th></tr></thead>
          <tbody>
            <tr v-for="item in list" :key="item.submissionId">
              <td>{{ item.title }}</td>
              <td>{{ item.status }}</td>
              <td>{{ item.score }}</td>
              <td>{{ item.submitTime }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const list = ref<any[]>([])
onMounted(async () => {
  const { data } = await http.get('/submissions/mine')
  list.value = data.data
})
</script>
