import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 5173,
    strictPort: true,
    allowedHosts: ['.trycloudflare.com'],
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        headers: {
          Origin: 'http://localhost:5173',
        },
      },
    },
  },
  optimizeDeps: {
    noDiscovery: true,
    include: ['vue', 'vue-router', 'pinia', 'axios'],
  },
})
