<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <header>
        <div class="label">My Classes</div>
        <h1 class="headline-lg">我的班级</h1>
      </header>
      <section class="panel overflow-hidden">
        <div class="panel-header">
          <h2 class="headline-md">加入班级</h2>
          <p class="muted mt-1">输入教师提供的邀请码，加入对应课程班级。</p>
        </div>
        <form class="grid gap-3 p-5 md:grid-cols-[minmax(0,1fr)_120px]" @submit.prevent="joinClass">
          <input v-model="inviteCode" class="form-input" placeholder="例如 DB2026A" />
          <button class="btn-primary" type="submit" :disabled="joining">
            <span class="material-symbols-outlined">login</span>
            {{ joining ? '加入中' : '加入' }}
          </button>
          <div v-if="message" class="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-800 md:col-span-2">
            {{ message }}
          </div>
          <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700 md:col-span-2">
            {{ error }}
          </div>
        </form>
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
const inviteCode = ref('')
const joining = ref(false)
const message = ref('')
const error = ref('')

onMounted(loadClasses)

async function loadClasses() {
  const { data } = await http.get('/classes')
  list.value = data.data
}

async function joinClass() {
  message.value = ''
  error.value = ''
  if (!inviteCode.value.trim()) {
    error.value = '请输入班级邀请码'
    return
  }
  joining.value = true
  try {
    const { data } = await http.post('/classes/join', { inviteCode: inviteCode.value.trim() })
    if (data.code !== 0) {
      throw new Error(data.message || '加入班级失败')
    }
    message.value = '已加入班级'
    inviteCode.value = ''
    await loadClasses()
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '加入班级失败'
  } finally {
    joining.value = false
  }
}
</script>
