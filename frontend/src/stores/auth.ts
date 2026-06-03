import { defineStore } from 'pinia'
import http from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as null | { userId: number; username: string; realName?: string; role: string },
  }),
  actions: {
    async login(username: string, password: string) {
      const { data } = await http.post('/auth/login', { username, password })
      this.token = data.data.token
      this.user = data.data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('role', this.user?.role || '')
    },
    async fetchMe() {
      if (!this.token) {
        return
      }
      const { data } = await http.get('/auth/me')
      this.user = data.data
      localStorage.setItem('role', this.user?.role || '')
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('role')
    },
    restore() {
      const role = localStorage.getItem('role')
      const token = localStorage.getItem('token')
      if (token) {
        this.token = token
      }
      if (role) {
        this.user = { userId: 0, username: '', realName: '', role }
      }
    },
  },
})
