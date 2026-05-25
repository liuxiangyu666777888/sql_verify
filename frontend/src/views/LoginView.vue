<template>
  <div class="app-shell min-h-screen flex items-center justify-center p-6">
    <div class="panel w-full max-w-md p-8 space-y-6">
      <div>
        <p class="text-sm text-slate-500">SQL Judge Exam</p>
        <h1 class="text-3xl font-bold mt-2">数据库课程实验系统</h1>
      </div>
      <form class="space-y-4" @submit.prevent="submit">
        <input v-model="username" class="w-full border rounded-lg px-4 py-3" placeholder="用户名" />
        <input v-model="password" type="password" class="w-full border rounded-lg px-4 py-3" placeholder="密码" />
        <button class="btn-primary w-full rounded-lg px-4 py-3 font-medium">登录</button>
      </form>
      <p class="text-sm text-slate-500">教师/学生种子账号密码均为 password</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('student1')
const password = ref('password')
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  await auth.login(username.value, password.value)
  if (auth.user?.role === 'TEACHER' || auth.user?.role === 'ADMIN') {
    await router.push('/teacher/dashboard')
  } else {
    await router.push('/student/dashboard')
  }
}
</script>
