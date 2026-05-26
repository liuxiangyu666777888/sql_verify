<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f2f5">
    <el-card style="width: 420px">
      <h2 style="text-align: center">用户注册</h2>
      <el-form :model="form" label-width="80px" style="margin-top: 24px">
        <el-form-item label="用户编号">
          <el-input v-model="form.id" placeholder="学号/工号" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="助教" value="assistant" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleRegister" :loading="loading">注册</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-button type="text" @click="$router.push('/login')">已有账号？去登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  id: '',
  username: '',
  password: '',
  role: 'student'
})

async function handleRegister() {
  if (!form.id || !form.username || !form.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {} finally { loading.value = false }
}
</script>
