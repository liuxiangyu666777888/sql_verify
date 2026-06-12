<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <section class="welcome-card">
        <div class="welcome-content">
          <div>
            <h1 class="headline-lg">Problem Bank</h1>
            <p class="muted mt-2">Practice SQL questions seeded from the database.</p>
          </div>
          <div class="search-box">
            <span class="material-symbols-outlined">search</span>
            <input v-model="keyword" placeholder="Search problems..." />
          </div>
        </div>
      </section>

      <section class="problem-grid">
        <article v-for="q in filteredQuestions" :key="q.questionId" class="problem-card">
          <div class="mb-3 flex items-center justify-between gap-3">
            <span class="difficulty">{{ q.difficulty }}</span>
            <span class="label">#{{ q.questionId }}</span>
          </div>
          <h2 class="font-bold">{{ q.title }}</h2>
          <p class="muted mt-2 line-clamp-3 text-sm">{{ q.description }}</p>
          <button class="btn-primary mt-5" @click="$router.push(`/problems/${q.questionId}`)">
            Solve Challenge
            <span class="material-symbols-outlined">arrow_forward</span>
          </button>
        </article>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import http from '../api/http'

const keyword = ref('')
const questions = ref<Array<{ questionId: number; title: string; description: string; difficulty: string }>>([])

const filteredQuestions = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return questions.value
  return questions.value.filter((q) => `${q.questionId} ${q.title} ${q.description}`.toLowerCase().includes(text))
})

onMounted(async () => {
  const { data } = await http.get('/questions')
  questions.value = data.data
})
</script>
