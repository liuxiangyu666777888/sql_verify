<template>
  <AppLayout>
    <div class="page-inner">
      <section class="panel">
        <div class="panel-header">
          <h1 class="headline-md">我的考试</h1>
        </div>
        <div class="grid-cards" style="padding:18px;">
          <article v-for="exam in exams" :key="exam.examId" class="card">
            <div class="label">{{ exam.status }}</div>
            <h2 style="font-size:18px;margin:8px 0;">{{ exam.examName }}</h2>
            <p class="muted">{{ exam.startTime }} - {{ exam.endTime }}</p>
            <button class="btn-primary" @click="$router.push(`/student/exams/${exam.examId}/take`)">进入考试</button>
          </article>
          <p v-if="!exams.length" class="muted">暂无考试</p>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const exams = ref<any[]>([])
onMounted(async () => {
  const { data } = await http.get('/exams')
  exams.value = data.data
})
</script>
