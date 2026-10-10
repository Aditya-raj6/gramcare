import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In development the Vite server proxies /api to the Spring Cloud Gateway.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: process.env.VITE_GATEWAY_URL || 'http://localhost:8080', changeOrigin: true },
    },
  },
  build: { chunkSizeWarningLimit: 2000 },
});
