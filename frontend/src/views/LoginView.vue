<template>
  <div class="login-page">
    <section class="login-card">
      <div class="brand" style="color:#172033; margin-bottom: 22px;">
        <div class="brand-mark">SQL</div>
        <div>
          <strong>SQL Judge</strong>
          <span>数据库练习与考试系统</span>
        </div>
      </div>
      <form class="form-stack" @submit.prevent="submit">
        <input v-model="username" placeholder="用户名" autocomplete="username" />
        <input v-model="password" placeholder="密码" type="password" autocomplete="current-password" />
        <button class="btn-primary" type="submit">登录</button>
        <p v-if="error" class="muted" style="color:#dc2626">{{ error }}</p>
      </form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('student1')
const password = ref('password')
const error = ref('')

async function submit() {
  error.value = ''
  try {
    await auth.login(username.value, password.value)
    const role = auth.user?.role
    router.push(['TEACHER', 'ASSISTANT', 'ADMIN'].includes(role || '') ? '/teacher/dashboard' : '/student/dashboard')
  } catch (e: any) {
    error.value = e?.response?.data?.message || '登录失败'
  }
}
</script>
