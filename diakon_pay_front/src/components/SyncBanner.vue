<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { syncPendingTransactions } from '@/utils/sync'
import { useTransactionsStore } from '@/stores/transactions'

const txStore = useTransactionsStore()

const message = ref('')
const type = ref<'info' | 'success' | 'error' | 'warning'>('info')
const visible = ref(false)
let hideTimer: ReturnType<typeof setTimeout> | null = null

function show(msg: string, t: typeof type.value = 'info', duration = 4000) {
  if (hideTimer) clearTimeout(hideTimer)
  message.value = msg
  type.value = t
  visible.value = true
  if (duration > 0) {
    hideTimer = setTimeout(() => {
      visible.value = false
    }, duration)
  }
}

async function doSync() {
  show(`Синхронизирую данные...`, 'info', 0)
  try {
    const result = await syncPendingTransactions()
    if (result.total === 0) {
      visible.value = false
      return
    }
    if (result.failed === 0) {
      show(`✓ Синхронизировано ${result.synced} транзакций`, 'success')
    } else {
      show(`Синхронизовано: ${result.synced}, ошибок: ${result.failed}`, 'error')
    }
    await txStore.refreshPendingCount()
  } catch {
    show('Ошибка синхронизации. Попробуем позже.', 'error')
  }
}

function handleOnline() {
  doSync()
}

function handleOffline() {
  show('⚡ Офлайн-режим. Данные сохраняются локально.', 'warning', 5000)
}

onMounted(() => {
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
  if (!navigator.onLine) handleOffline()
})

onUnmounted(() => {
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
})
</script>

<template>
  <Transition name="banner">
    <div v-if="visible" class="sync-banner" :class="type">
      {{ message }}
    </div>
  </Transition>
</template>

<style scoped>
.sync-banner {
  position: fixed;
  bottom: calc(var(--nav-h) + 12px);
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 22px;
  border-radius: 24px;
  font-size: 13px;
  font-weight: 600;
  z-index: 200;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
  white-space: nowrap;
  max-width: calc(100vw - 32px);
}

.sync-banner.info    { background: var(--primary); color: #fff; }
.sync-banner.success { background: var(--success); color: #fff; }
.sync-banner.error   { background: var(--danger);  color: #fff; }
.sync-banner.warning { background: #d97706;         color: #fff; }

.banner-enter-active, .banner-leave-active { transition: opacity 0.3s, transform 0.3s; }
.banner-enter-from, .banner-leave-to { opacity: 0; transform: translateX(-50%) translateY(12px); }

@media (min-width: 768px) {
  .sync-banner { bottom: 24px; }
}
</style>
