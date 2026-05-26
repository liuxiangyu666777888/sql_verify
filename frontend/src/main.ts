import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles.css'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia).use(router)
const auth = useAuthStore(pinia)
auth.restore()
auth.fetchMe().catch(() => auth.logout())
app.mount('#app')
