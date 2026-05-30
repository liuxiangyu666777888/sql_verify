<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header>
        <div class="label">My Classes</div>
        <h1 class="headline-lg">我的班级</h1>
      </header>
      <section class="grid gap-4 md:grid-cols-2">
        <div v-for="item in list" :key="item.classId" class="panel p-5">
          <div class="font-semibold">{{ item.className }}</div>
          <div class="muted mt-1 text-sm">{{ item.semester }}</div>
          <div class="muted mt-1 text-sm">邀请码：{{ item.inviteCode }}</div>
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
