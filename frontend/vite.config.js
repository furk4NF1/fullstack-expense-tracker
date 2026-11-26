import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const apiTarget = process.env.API_PROXY_TARGET || 'http://localhost:8080';
const apiProxy = {
  '/api': {
    target: apiTarget,
    changeOrigin: true
  }
};

export default defineConfig({
  plugins: [react()],
  server: {
    host: process.env.VITE_HOST || '127.0.0.1',
    port: 5173,
    proxy: apiProxy
  },
  preview: {
    proxy: apiProxy
  }
});
