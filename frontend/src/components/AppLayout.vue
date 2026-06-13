<template>
  <div class="app-shell">
    <nav class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <span class="material-symbols-outlined fill-icon">database</span>
        </div>
        <div>
          <div class="brand-title">Academic SQL</div>
          <div class="brand-subtitle">Standard Edition</div>
        </div>
      </div>

      <div class="nav">
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/dashboard">
          <span class="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/questions">
          <span class="material-symbols-outlined">library_books</span>
          <span>Problem Bank</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/exams/new">
          <span class="material-symbols-outlined">assignment_add</span>
          <span>Configure Exam</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/classes">
          <span class="material-symbols-outlined">groups</span>
          <span>Classroom</span>
        </RouterLink>
        <RouterLink v-if="isTeacher" class="nav-link" to="/teacher/scores">
          <span class="material-symbols-outlined">analytics</span>
          <span>Gradebook</span>
        </RouterLink>
        <RouterLink v-if="isAdmin" class="nav-link" to="/admin/users">
          <span class="material-symbols-outlined">manage_accounts</span>
          <span>User Admin</span>
        </RouterLink>

        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/dashboard">
          <span class="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/problems">
          <span class="material-symbols-outlined">task_alt</span>
          <span>Solved Problems</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/submissions">
          <span class="material-symbols-outlined">history</span>
          <span>My Submissions</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/exams">
          <span class="material-symbols-outlined">school</span>
          <span>Learning Paths</span>
        </RouterLink>
        <RouterLink v-if="!isTeacher" class="nav-link" to="/student/classes">
          <span class="material-symbols-outlined">groups</span>
          <span>Classroom</span>
        </RouterLink>
      </div>

      <div class="sidebar-footer">
        <RouterLink to="/problems">
          <span class="material-symbols-outlined">menu_book</span>
          <span>Documentation</span>
        </RouterLink>
        <RouterLink :to="isTeacher ? '/teacher/questions' : '/student/classes'">
          <span class="material-symbols-outlined">help_outline</span>
          <span>Help Center</span>
        </RouterLink>
      </div>
    </nav>

    <div class="workspace">
      <header class="topbar">
        <div class="mobile-brand">
          <span class="material-symbols-outlined">database</span>
          <span>SQL Master</span>
        </div>
        <div class="search-box">
          <span class="material-symbols-outlined">search</span>
          <input v-model="globalSearch" placeholder="Search problems..." @keyup.enter="runGlobalSearch" />
        </div>
        <div class="top-actions">
          <button class="top-icon" aria-label="Notifications" title="当前没有新通知" @click="notice = '当前没有新通知'">
            <span class="material-symbols-outlined">notifications</span>
            <span class="notice-dot"></span>
          </button>
          <button class="top-icon" aria-label="Settings" title="修改密码" @click="showSettings = true">
            <span class="material-symbols-outlined">settings</span>
          </button>
          <button class="btn-secondary" @click="logout">退出</button>
          <div class="avatar">{{ avatarText }}</div>
        </div>
      </header>

      <main class="content">
        <div v-if="notice" class="mb-4 rounded-lg border border-outline-variant bg-white px-4 py-3 text-sm font-semibold text-on-surface">
          {{ notice }}
        </div>
        <slot />
      </main>
    </div>

    <div v-if="showSettings" class="fixed inset-0 z-50 grid place-items-center bg-black/30 p-4" @click.self="closeSettings">
      <section class="panel w-full max-w-md overflow-hidden">
        <div class="panel-header">
          <h2 class="headline-md">修改密码</h2>
          <p class="muted mt-1">修改成功后请使用新密码重新登录。</p>
        </div>
        <form class="grid gap-4 p-6" @submit.prevent="changePassword">
          <label class="grid gap-2">
            <span class="label text-on-surface">旧密码</span>
            <input v-model="passwordForm.oldPassword" class="form-input" type="password" autocomplete="current-password" />
          </label>
          <label class="grid gap-2">
            <span class="label text-on-surface">新密码</span>
            <input v-model="passwordForm.newPassword" class="form-input" type="password" autocomplete="new-password" />
          </label>
          <label class="grid gap-2">
            <span class="label text-on-surface">确认新密码</span>
            <input v-model="passwordForm.confirmPassword" class="form-input" type="password" autocomplete="new-password" />
          </label>
          <div v-if="settingsMessage" class="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-800">
            {{ settingsMessage }}
          </div>
          <div v-if="settingsError" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
            {{ settingsError }}
          </div>
          <div class="flex justify-end gap-3">
            <button class="btn-secondary" type="button" @click="closeSettings">取消</button>
            <button class="btn-primary" type="submit" :disabled="savingPassword">
              <span class="material-symbols-outlined">save</span>
              {{ savingPassword ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import http from '../api/http'

const auth = useAuthStore()
const router = useRouter()
const role = computed(() => auth.user?.role || localStorage.getItem('role') || '')
const isTeacher = computed(() => ['TEACHER', 'ADMIN'].includes(role.value))
const isAdmin = computed(() => role.value === 'ADMIN')
const avatarText = computed(() => (auth.user?.realName || auth.user?.username || 'U').slice(0, 1).toUpperCase())
const notice = ref('')
const globalSearch = ref('')
const showSettings = ref(false)
const savingPassword = ref(false)
const settingsMessage = ref('')
const settingsError = ref('')
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

function logout() {
  auth.logout()
  router.push('/login')
}

function runGlobalSearch() {
  const keyword = globalSearch.value.trim()
  if (!keyword) {
    return
  }
  router.push({
    path: isTeacher.value ? '/teacher/questions' : '/problems',
    query: { keyword },
  })
}

function closeSettings() {
  showSettings.value = false
  settingsMessage.value = ''
  settingsError.value = ''
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

async function changePassword() {
  settingsMessage.value = ''
  settingsError.value = ''
  if (passwordForm.newPassword.length < 6) {
    settingsError.value = '新密码至少需要 6 位'
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    settingsError.value = '两次输入的新密码不一致'
    return
  }
  savingPassword.value = true
  try {
    const { data } = await http.post('/auth/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    if (data.code !== 0) {
      throw new Error(data.message || '修改密码失败')
    }
    settingsMessage.value = '密码已修改，请重新登录'
    setTimeout(() => {
      auth.logout()
      router.push('/login')
    }, 700)
  } catch (err: any) {
    settingsError.value = err?.response?.data?.message || err?.message || '修改密码失败'
  } finally {
    savingPassword.value = false
  }
}
</script>
