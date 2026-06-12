<template>
  <AppLayout>
    <div class="page-inner">
      <section class="panel">
        <div class="panel-header">
          <h1 class="headline-md">题库练习</h1>
          <p class="muted">从数据库题库读取，提交后由后端判题。</p>
        </div>
        <div class="grid-cards" style="padding: 18px;">
          <article v-for="q in questions" :key="q.questionId" class="card">
            <div class="label">{{ q.difficulty }}</div>
            <h2 style="font-size:18px;margin:8px 0;">{{ q.title }}</h2>
            <p class="muted">{{ q.description }}</p>
            <button class="btn-primary" @click="$router.push(`/problems/${q.questionId}`)">开始练习</button>
          </article>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import http from '../api/http'

const questions = ref<Array<{ questionId: number; title: string; description: string; difficulty: string }>>([])

onMounted(async () => {
  const { data } = await http.get('/questions')
  questions.value = data.data
})
</script>
