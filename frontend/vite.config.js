import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false, // In case of development
      },
      '/timesheet': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/leave': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
