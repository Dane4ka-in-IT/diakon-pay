<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const links = [
  {
    to: '/transactions',
    label: 'Транзакции',
    icon: `<path stroke-linecap="round" stroke-linejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/>`,
  },
  {
    to: '/analytics',
    label: 'Аналитика',
    icon: `<path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>`,
  },
  {
    to: '/profile',
    label: 'Профиль',
    icon: `<path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>`,
  },
]
</script>

<template>
  <nav class="navbar">
    <RouterLink
      v-for="link in links"
      :key="link.to"
      :to="link.to"
      class="nav-item"
      :class="{ active: route.path === link.to }"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.8"
        class="nav-icon"
        v-html="link.icon"
      />
      <span class="nav-label">{{ link.label }}</span>
    </RouterLink>
  </nav>
</template>

<style scoped>
.navbar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 540px;
  background: var(--surface-solid);
  border-top: 1px solid var(--border);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-around;
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom, 0px));
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  text-decoration: none;
  color: var(--text-muted);
  padding: 4px 16px;
  border-radius: var(--radius-sm);
  transition: color 0.15s;
  min-width: 72px;
}

.nav-item.active {
  color: var(--primary);
}

.nav-icon {
  width: 24px;
  height: 24px;
}

.nav-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

@media (min-width: 768px) {
  .navbar { display: none; }
}
</style>
