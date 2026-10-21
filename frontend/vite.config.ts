import { readFileSync } from 'node:fs'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// The repository's VERSION file is the single source of truth for the app version.
const appVersion = readFileSync(new URL('../VERSION', import.meta.url), 'utf-8').trim()

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    __APP_VERSION__: JSON.stringify(appVersion),
  },
  server: {
    port: 5173,
    // In development, forward /api requests to control-api so the browser
    // only ever talks to one origin (no CORS setup needed).
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
})
