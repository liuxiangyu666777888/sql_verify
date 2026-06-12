<template>
  <AppLayout>
    <div class="page-inner grid-cards">
      <section class="card">
        <div class="label">Questions</div>
        <h2>题目管理</h2>
        <p class="muted">查看练习题库和考试题目。</p>
        <button class="btn-primary" @click="$router.push('/teacher/questions')">进入</button>
      </section>
      <section class="card">
        <div class="label">Exams</div>
        <h2>创建考试</h2>
        <p class="muted">配置考试时间、题目和分值。</p>
        <button class="btn-primary" @click="$router.push('/teacher/exams/new')">创建</button>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../../components/AppLayout.vue'
</script>
