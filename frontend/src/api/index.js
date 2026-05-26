import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: 'http://localhost:5000/api',
  timeout: 30000
})

http.interceptors.request.use(config => {
  const session = localStorage.getItem('session')
  if (session) {
    config.headers.session = session
  }
  return config
})

http.interceptors.response.use(
  response => response,
  error => {
    const msg = error.response?.data?.message || '请求失败'
    ElMessage.error(msg)
    if (error.response?.status === 401) {
      localStorage.removeItem('session')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default http
