<template>
  <div class="login-shell">
    <div class="login-card">
      <div class="login-brand">
        <div class="login-mark">
          <span class="material-symbols-outlined fill-icon">database</span>
        </div>
        <div>
          <div class="login-kicker">SQL Master</div>
          <h1>数据库课程实验系统</h1>
        </div>
      </div>
      <form class="login-form" @submit.prevent="submit">
        <input v-model="username" class="login-input" placeholder="用户名" />
        <input v-model="password" type="password" class="login-input" placeholder="密码" />
        <button class="login-button">登录</button>
      </form>
      <div class="login-note">默认账号：student1 / password，teacher1 / password</div>
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
