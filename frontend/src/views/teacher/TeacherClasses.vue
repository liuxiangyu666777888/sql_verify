<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header>
        <div class="label">Class Management</div>
        <h1 class="headline-lg">班级管理</h1>
      </header>

      <section class="panel overflow-hidden">
        <div class="panel-header">
          <h2 class="headline-md">创建班级</h2>
          <p class="muted mt-1">创建后系统会自动生成邀请码，学生可用邀请码加入。</p>
        </div>
        <form class="grid gap-4 p-5 md:grid-cols-[minmax(0,1fr)_180px_auto]" @submit.prevent="createClass">
          <input v-model="form.className" class="form-input" placeholder="班级名称" />
          <input v-model="form.semester" class="form-input" placeholder="学期" />
          <button class="btn-primary" :disabled="saving">
            <span class="material-symbols-outlined">{{ saving ? 'progress_activity' : 'add' }}</span>
            创建
          </button>
        </form>
        <div v-if="message" class="px-5 pb-5 text-sm font-semibold text-green-700">{{ message }}</div>
        <div v-if="error" class="px-5 pb-5 text-sm font-semibold text-red-700">{{ error }}</div>
      </section>

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
const saving = ref(false)
const message = ref('')
const error = ref('')
const form = ref({ className: '', semester: '' })

onMounted(loadClasses)

async function loadClasses() {
  const { data } = await http.get('/classes')
  list.value = data.data
}

async function createClass() {
  message.value = ''
  error.value = ''
  if (!form.value.className.trim()) {
    error.value = '请输入班级名称'
    return
  }
  saving.value = true
  try {
    const { data } = await http.post('/classes', {
      className: form.value.className.trim(),
      semester: form.value.semester.trim(),
    })
    if (data.code !== 0) throw new Error(data.message || '创建班级失败')
    message.value = `班级已创建，邀请码：${data.data.inviteCode}`
    form.value = { className: '', semester: '' }
    await loadClasses()
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '创建班级失败'
  } finally {
    saving.value = false
  }
}
</script>
