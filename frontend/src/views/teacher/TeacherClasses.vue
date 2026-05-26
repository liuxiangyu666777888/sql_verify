<template>
  <AppLayout>
    <div class="space-y-6">
      <header>
        <p class="text-sm text-slate-500">Class Management</p>
        <h1 class="text-3xl font-bold">班级管理</h1>
      </header>
      <section class="grid md:grid-cols-2 gap-4">
        <div v-for="item in list" :key="item.classId" class="panel p-5">
          <div class="font-semibold">{{ item.className }}</div>
          <div class="text-sm text-slate-500 mt-1">{{ item.semester }}</div>
          <div class="text-sm text-slate-500 mt-1">邀请码：{{ item.inviteCode }}</div>
        </div>
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
  const { data } = await http.get('/classes')
  list.value = data.data
})
</script>
