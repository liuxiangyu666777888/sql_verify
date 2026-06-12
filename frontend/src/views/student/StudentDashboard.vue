<template>
  <AppLayout>
    <div class="page-inner">
      <section class="panel">
        <div class="panel-header">
          <h1 class="headline-md">学生首页</h1>
          <p class="muted">继续完成 SQL 题库练习。</p>
        </div>
        <div class="grid-cards" style="padding:18px;">
          <article class="card">
            <div class="label">Solved</div>
            <h2>{{ dashboard.solvedCount }}</h2>
            <p class="muted">已通过题目</p>
          </article>
          <article class="card">
            <div class="label">Accuracy</div>
            <h2>{{ dashboard.accuracyRate }}%</h2>
            <p class="muted">提交正确率</p>
          </article>
          <article class="card">
            <div class="label">Practice</div>
            <h2>题库练习</h2>
            <button class="btn-primary" @click="$router.push('/problems')">进入题库</button>
          </article>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

const dashboard = reactive({ solvedCount: 0, accuracyRate: 0 })

onMounted(async () => {
  try {
    const { data } = await http.get('/student/dashboard')
    Object.assign(dashboard, data.data)
  } catch (_) {
    // keep defaults
  }
})
</script>
