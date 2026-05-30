<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header>
        <div class="label">My Exams</div>
        <h1 class="headline-lg">我的考试</h1>
      </header>
      <section class="panel overflow-hidden">
        <table class="table w-full">
          <thead><tr><th>考试</th><th>开始</th><th>结束</th><th></th></tr></thead>
          <tbody>
            <tr v-for="item in list" :key="item.examId">
              <td>{{ item.examName }}</td>
              <td>{{ item.startTime }}</td>
              <td>{{ item.endTime }}</td>
              <td class="text-right">
                <button class="btn-primary" @click="take(item.examId)">进入</button>
              </td>
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

const router = useRouter()
const list = ref<any[]>([])
onMounted(async () => {
  const { data } = await http.get('/exams')
  list.value = data.data
})

function take(id: number) {
  router.push(`/student/exams/${id}/take`)
}
</script>
