<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f2f5">
    <el-card style="width: 400px">
      <h2 style="text-align: center">SQL 在线判题系统</h2>
      <el-form :model="form" label-width="60px" style="margin-top: 24px">
        <el-form-item label="账号">
          <el-input v-model="form.id" placeholder="请输入账号（学号/工号）" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
            <el-option label="助教" value="assistant" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-button type="text" @click="$router.push('/register')">没有账号？去注册</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)

const form = reactive({
  id: '',
  password: '',
  role: 'student'
})

async function handleLogin() {
  if (!form.id || !form.password) {
    ElMessage.warning('请填写账号和密码')
    return
  }
  loading.value = true
  try {
    await store.login({ id: form.id, password: form.password, role: form.role })
    ElMessage.success('登录成功')
    router.push('/home')
  } catch {} finally { loading.value = false }
}
</script>
