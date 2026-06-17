import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  base: './',
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return undefined
          }
          if (id.includes('naive-ui') || id.includes('@vicons')) {
            return 'ui-vendor'
          }
          if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) {
            return 'vue-vendor'
          }
          if (id.includes('axios')) {
            return 'http-vendor'
          }
          if (id.includes('electron-log') || id.includes('electron-updater')) {
            return 'electron-vendor'
          }
          return 'shared-vendor'
        }
      }
    }
  }
})
