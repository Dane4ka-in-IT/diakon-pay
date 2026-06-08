<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTheme } from '@/utils/theme'

const { theme, toggle } = useTheme()
const auth = useAuthStore()
const route = useRoute()

const navLinks = [
  { to: '/transactions', label: 'Транзакции' },
  { to: '/analytics',    label: 'Аналитика'  },
  { to: '/profile',      label: 'Профиль'    },
]

const showNav = computed(() => auth.isAuthenticated)
</script>

<template>
  <header class="app-header">
    <div class="header-inner">
      <RouterLink to="/" class="logo">Diakon<span>Pay</span></RouterLink>

      <nav v-if="showNav" class="desktop-nav">
        <RouterLink
          v-for="link in navLinks"
          :key="link.to"
          :to="link.to"
          class="nav-link"
          :class="{ active: route.path === link.to }"
        >
          {{ link.label }}
        </RouterLink>
      </nav>

      <button class="theme-btn" @click="toggle" :title="theme === 'dark' ? 'Светлая тема' : 'Тёмная тема'">
        {{ theme === 'dark' ? '☀' : '☾' }}
      </button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background: linear-gradient(90deg, #0E40C7 0%, #0E45D5 22%, #0C76F7 38%, #0C8DFD 51%, #0C76F7 66%, #0C76F7 79%, #0E4AE3 100%);
  position: sticky;
  top: 0;
  z-index: 200;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.logo {
  color: #cfe0ff;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.3px;
  text-decoration: none;
  font-family: 'Segoe UI', -apple-system, sans-serif;
  flex-shrink: 0;
}
.logo span { color: #ffffff; }

.desktop-nav {
  display: none;
  flex: 1;
  justify-content: center;
  gap: 4px;
}
.nav-link {
  color: rgba(255, 255, 255, 0.82);
  text-decoration: none;
  font-size: 15px;
  font-weight: 600;
  padding: 7px 16px;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.nav-link:hover { background: rgba(255, 255, 255, 0.13); color: #fff; }
.nav-link.active { background: rgba(255, 255, 255, 0.2); color: #fff; }

.theme-btn {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.28);
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #cfe0ff;
  font-size: 17px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
  flex-shrink: 0;
}
.theme-btn:hover { background: rgba(255, 255, 255, 0.22); }

@media (min-width: 768px) {
  .desktop-nav { display: flex; }
}
</style>
