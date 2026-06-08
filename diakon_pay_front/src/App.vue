<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/AppHeader.vue'
import NavBar from '@/components/NavBar.vue'
import SyncBanner from '@/components/SyncBanner.vue'
import { useTheme } from '@/utils/theme'

const route = useRoute()
const auth = useAuthStore()
const showNav = computed(() => !route.meta.public && auth.isAuthenticated)

useTheme()
</script>

<template>
  <div class="app">
    <AppHeader />
    <RouterView />
    <NavBar v-if="showNav" />
    <SyncBanner />
  </div>
</template>

<style>
/* ── Reset ── */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

/* ── Design tokens — Light (default) ── */
:root {
  --primary:          #0C76F7;
  --primary-dark:     #0A60D4;
  --success:          #28a745;
  --danger:           #dc3545;
  --warning:          #c47f00;
  --radius:           14px;
  --radius-sm:        8px;
  --nav-h:            64px;

  /* light-specific */
  --body-bg:          linear-gradient(90deg, #F8F8F8 0%, #EAEAEA 30%, #E5F0FF 52%, #49A9FD 87%, #2A9BFD 100%);
  --surface:          rgba(255, 255, 255, 0.92);
  --surface-2:        #eef4ff;
  --surface-solid:    #ffffff;
  --border:           #b9c8e0;
  --card-shadow:      0 4px 24px rgba(0, 0, 0, 0.09);
  --text:             #1a3560;
  --text-muted:       #5d7aa8;
  --heading:          #1a4fa8;
  --input-bg:         #f5f8ff;
  --input-border:     #b9c8e0;
  --btn-ghost-bg:     rgba(255, 255, 255, 0.65);
  --btn-ghost-text:   #1a4fa8;
}

/* ── Design tokens — Dark ── */
[data-theme="dark"] {
  --body-bg:          #0c1220;
  --surface:          #131f35;
  --surface-2:        #1a2d4a;
  --surface-solid:    #131f35;
  --border:           #233554;
  --card-shadow:      none;
  --text:             #e2eaf5;
  --text-muted:       #7a97bd;
  --heading:          #e2eaf5;
  --input-bg:         #1a2d4a;
  --input-border:     #233554;
  --btn-ghost-bg:     #1a2d4a;
  --btn-ghost-text:   #e2eaf5;
  --success:          #25c26e;
  --danger:           #f04f5a;
  --warning:          #f0a030;
}

/* ── Background — fixed so it fills viewport on all page lengths ── */
html {
  min-height: 100%;
}
html::before {
  content: '';
  position: fixed;
  inset: 0;
  z-index: -1;
  background: var(--body-bg);
}

html, body {
  color: var(--text);
  font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif;
  font-size: 16px;
  line-height: 1.5;
  min-height: 100dvh;
  -webkit-font-smoothing: antialiased;
}

/* ── App shell ── */
.app {
  width: 100%;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
}

/* ── Authenticated page wrapper ── */
.page {
  max-width: 540px;
  margin: 0 auto;
  width: 100%;
  padding: 16px 16px calc(var(--nav-h) + 16px);
}

.page-header {
  padding: 16px 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--heading);
}

/* ── Card ── */
.card {
  background: var(--surface);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--card-shadow);
  padding: 16px;
}

/* ── Buttons ── */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 20px;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity 0.15s, background 0.15s;
  width: 100%;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-primary {
  background: linear-gradient(90deg, #2a6fd8, #1a4fa8);
  color: #fff;
}
.btn-primary:hover:not(:disabled) { opacity: 0.9; }

.btn-ghost {
  background: var(--btn-ghost-bg);
  color: var(--btn-ghost-text);
  border: 1px solid var(--border);
}
.btn-ghost:hover:not(:disabled) { opacity: 0.85; }

.btn-danger { background: var(--danger); color: #fff; }
.btn-danger:hover:not(:disabled) { opacity: 0.9; }

.btn-sm { padding: 8px 14px; font-size: 13px; width: auto; }

/* ── Form ── */
.form-group { display: flex; flex-direction: column; gap: 5px; margin-bottom: 14px; }
.form-label  { font-size: 13px; font-weight: 600; color: var(--text-muted); }
.form-input {
  background: var(--input-bg);
  border: 1px solid var(--input-border);
  color: var(--text);
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
  width: 100%;
}
.form-input::placeholder { color: var(--text-muted); opacity: 0.7; }
.form-input:focus { border-color: var(--primary); }
.form-input option { background: var(--surface-solid); color: var(--text); }

.error-msg {
  color: var(--danger);
  font-size: 13px;
  padding: 10px 14px;
  background: rgba(220, 53, 69, 0.09);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(220, 53, 69, 0.25);
}

/* ── Badges ── */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}
.badge-warning { background: rgba(240,160,48,0.15); color: var(--warning); }
.badge-success { background: rgba(37,194,110,0.15); color: var(--success); }

.divider { height: 1px; background: var(--border); margin: 12px 0; }

/* ── Modals — centered ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 300;
}
.modal-sheet {
  background: var(--surface-solid);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.22);
  padding: 28px 28px 24px;
  width: 100%;
  max-width: 420px;
  max-height: 90dvh;
  overflow-y: auto;
}
.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--heading);
  margin-bottom: 20px;
  text-align: center;
}
.modal-actions { display: flex; gap: 10px; margin-top: 8px; }

/* ── Amount colours ── */
.amount-positive { color: var(--success); }
.amount-negative { color: var(--danger); }

/* ── Spinner ── */
.spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin: 40px auto;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Empty state ── */
.empty-state {
  text-align: center;
  color: var(--text-muted);
  padding: 48px 0;
  font-size: 15px;
}

@media (min-width: 768px) {
  .page {
    max-width: 1080px;
    padding: 28px 40px 48px;
  }
  .page-header { padding: 20px 0 16px; }
}
</style>
