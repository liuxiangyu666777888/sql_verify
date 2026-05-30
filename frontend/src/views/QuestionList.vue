<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <div class="label">Problem Bank</div>
          <h1 class="headline-lg">题库练习</h1>
        </div>
        <input v-model="keyword" class="input md:max-w-sm" placeholder="搜索题目" @keyup.enter="load" />
      </header>

      <section class="panel overflow-hidden">
        <table class="table w-full">
          <thead><tr><th>题号</th><th>标题</th><th>难度</th><th>标签</th><th></th></tr></thead>
          <tbody>
            <tr v-for="item in list" :key="item.questionId">
              <td>{{ item.questionId }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.difficulty }}</td>
              <td>{{ item.tags }}</td>
              <td class="text-right">
                <button class="btn-secondary" @click="go(item.questionId)">开始练习</button>
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
import AppLayout from '../components/AppLayout.vue'
import http from '../api/http'

const router = useRouter()
const keyword = ref('')
const list = ref<Array<{ questionId: number; title: string; difficulty: string; tags?: string }>>([])

async function load() {
  const { data } = await http.get('/questions', { params: { keyword: keyword.value } })
  list.value = data.data
}

function go(id: number) {
  router.push(`/problems/${id}`)
}

onMounted(load)
</script>
