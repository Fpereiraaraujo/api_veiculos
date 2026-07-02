import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      boxShadow: {
        soft: '0 16px 40px rgba(15, 23, 42, 0.12)',
      },
      colors: {
        ink: {
          50: '#f8fafc',
          100: '#e2e8f0',
          200: '#cbd5e1',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
        },
      },
    },
  },
  plugins: [],
} satisfies Config;
