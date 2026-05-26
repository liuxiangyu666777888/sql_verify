import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getSession } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const session = ref(localStorage.getItem('session') || '')

  const isLoggedIn = computed(() => !!session.value)
  const role = computed(() => user.value?.role)
  const roleName = computed(() => {
    const map = { 0: '学生', 1: '教师', 2: '管理员', 3: '助教' }
    return map[role.value] || ''
  })

  async function login(credentials) {
    const res = await loginApi(credentials)
    const u = res.data
    user.value = { id: u.id, username: u.username, role: u.role }
    session.value = u.session
    localStorage.setItem('user', JSON.stringify(user.value))
    localStorage.setItem('session', session.value)
    return u
  }

  function logout() {
    user.value = null
    session.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('session')
  }

  async function checkSession() {
    try {
      const res = await getSession()
      user.value = { id: res.data.id, username: res.data.name, role: res.data.role }
      localStorage.setItem('user', JSON.stringify(user.value))
      return true
    } catch {
      logout()
      return false
    }
  }

  return { user, session, isLoggedIn, role, roleName, login, logout, checkSession }
})
