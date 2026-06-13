<template>
  <AppLayout>
    <div class="page-inner space-y-6">
      <section class="welcome-card">
        <div class="welcome-content">
          <div>
            <div class="label">Administration</div>
            <h1 class="headline-lg">用户管理</h1>
            <p class="muted mt-2">创建账号，调整用户角色和启用状态。</p>
          </div>
          <button class="btn-primary" @click="resetForm">
            <span class="material-symbols-outlined">person_add</span>
            新增用户
          </button>
        </div>
      </section>

      <div class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
        <section class="panel overflow-hidden">
          <table class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>姓名</th>
                <th>角色</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.userId">
                <td>{{ user.userId }}</td>
                <td class="font-semibold">{{ user.username }}</td>
                <td>{{ user.realName || '-' }}</td>
                <td>{{ user.role }}</td>
                <td>
                  <span class="status-pill" :class="user.status === 'ACTIVE' ? 'bg-green-700' : 'bg-slate-600'">
                    {{ user.status }}
                  </span>
                </td>
                <td>
                  <button class="btn-secondary" @click="editUser(user)">编辑</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <aside class="panel overflow-hidden">
          <div class="panel-header">
            <h2 class="headline-md">{{ editingId ? '编辑用户' : '新增用户' }}</h2>
            <p class="muted mt-1">密码只在创建用户时填写。</p>
          </div>
          <form class="grid gap-4 p-6" @submit.prevent="saveUser">
            <label class="grid gap-2">
              <span class="label text-on-surface">用户名</span>
              <input v-model="form.username" class="form-input" :disabled="Boolean(editingId)" />
            </label>
            <label v-if="!editingId" class="grid gap-2">
              <span class="label text-on-surface">初始密码</span>
              <input v-model="form.password" class="form-input" type="password" />
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">姓名</span>
              <input v-model="form.realName" class="form-input" />
            </label>
            <label v-if="!editingId" class="grid gap-2">
              <span class="label text-on-surface">邮箱</span>
              <input v-model="form.email" class="form-input" />
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">角色</span>
              <select v-model="form.role" class="form-input">
                <option value="STUDENT">STUDENT</option>
                <option value="TEACHER">TEACHER</option>
                <option value="ASSISTANT">ASSISTANT</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </label>
            <label class="grid gap-2">
              <span class="label text-on-surface">状态</span>
              <select v-model="form.status" class="form-input">
                <option value="ACTIVE">ACTIVE</option>
                <option value="DISABLED">DISABLED</option>
              </select>
            </label>
            <div v-if="message" class="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-800">
              {{ message }}
            </div>
            <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
              {{ error }}
            </div>
            <div class="flex justify-end gap-3">
              <button class="btn-secondary" type="button" @click="resetForm">重置</button>
              <button class="btn-primary" type="submit" :disabled="saving">
                <span class="material-symbols-outlined">save</span>
                {{ saving ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </aside>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import http from '../../api/http'

type AdminUser = {
  userId: number
  username: string
  realName?: string
  email?: string
  role: string
  status: string
}

const users = ref<AdminUser[]>([])
const editingId = ref<number | null>(null)
const saving = ref(false)
const message = ref('')
const error = ref('')
const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  role: 'STUDENT',
  status: 'ACTIVE',
})

onMounted(loadUsers)

async function loadUsers() {
  const { data } = await http.get('/admin/users')
  users.value = data.data || []
}

function editUser(user: AdminUser) {
  editingId.value = user.userId
  form.username = user.username
  form.password = ''
  form.realName = user.realName || ''
  form.email = user.email || ''
  form.role = user.role
  form.status = user.status
  message.value = ''
  error.value = ''
}

function resetForm() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.role = 'STUDENT'
  form.status = 'ACTIVE'
  message.value = ''
  error.value = ''
}

async function saveUser() {
  message.value = ''
  error.value = ''
  saving.value = true
  try {
    const request = editingId.value
      ? http.put(`/admin/users/${editingId.value}`, { role: form.role, status: form.status })
      : http.post('/admin/users', {
          username: form.username,
          password: form.password,
          realName: form.realName,
          email: form.email,
          role: form.role,
        })
    const { data } = await request
    if (data.code !== 0) throw new Error(data.message || '保存用户失败')
    message.value = '用户已保存'
    await loadUsers()
    if (!editingId.value) resetForm()
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '保存用户失败'
  } finally {
    saving.value = false
  }
}
</script>
