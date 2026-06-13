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
            <input v-model="keyword" placeholder="Search problems..." @keyup.enter="loadQuestions(1)" />
          </div>
        </div>
      </section>

      <section class="problem-grid">
        <article v-for="q in questions" :key="q.questionId" class="problem-card">
          <div class="mb-3 flex items-center justify-between gap-3">
            <span class="difficulty">{{ q.difficulty }}</span>
            <span class="label">#{{ q.questionId }}</span>
          </div>
          <h2 class="font-bold">{{ q.title }}</h2>
          <p class="muted mt-2 line-clamp-3 text-sm">{{ q.tags || 'SQL practice challenge' }}</p>
          <button class="btn-primary mt-5" @click="$router.push(`/problems/${q.questionId}`)">
            Solve Challenge
            <span class="material-symbols-outlined">arrow_forward</span>
          </button>
        </article>
      </section>

      <section class="flex items-center justify-between gap-4">
        <button class="btn-secondary" :disabled="page <= 1" @click="loadQuestions(page - 1)">
          <span class="material-symbols-outlined">chevron_left</span>
          上一页
        </button>
        <div class="label">第 {{ page }} 页 / 共 {{ total }} 题</div>
        <button class="btn-secondary" :disabled="page * size >= total" @click="loadQuestions(page + 1)">
          下一页
          <span class="material-symbols-outlined">chevron_right</span>
        </button>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../components/AppLayout.vue'
import http from '../api/http'

const route = useRoute()
const keyword = ref('')
const questions = ref<Array<{ questionId: number; title: string; difficulty: string; tags?: string }>>([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

onMounted(() => {
  keyword.value = String(route.query.keyword || '')
  loadQuestions(1)
})

watch(() => route.query.keyword, (value) => {
  keyword.value = String(value || '')
  loadQuestions(1)
})

async function loadQuestions(nextPage: number) {
  page.value = nextPage
  const { data } = await http.get('/questions', {
    params: { keyword: keyword.value, page: page.value, size: size.value },
  })
  questions.value = data.data.items
  total.value = data.data.total
}
</script>
