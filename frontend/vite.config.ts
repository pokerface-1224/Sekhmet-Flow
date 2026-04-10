import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    proxy: {
      '/api': {
        target: 'http://localhost:1224',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: path.resolve(__dirname, '../backend/src/main/resources/static'),
    emptyOutDir: true,
  },
})
