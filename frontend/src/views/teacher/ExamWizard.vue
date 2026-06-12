<template>
  <AppLayout>
    <div class="page-inner">
      <section class="panel">
        <div class="panel-header">
          <h1 class="headline-md">创建考试</h1>
          <p class="muted">当前版本保留考试创建入口，详细配置可继续扩展。</p>
        </div>
        <div style="padding:18px;">
          <button class="btn-secondary" @click="$router.push('/teacher/dashboard')">返回教师首页</button>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../../components/AppLayout.vue'
</script>
