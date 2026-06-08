<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useTransactionsStore } from '@/stores/transactions'
import { useCategoriesStore } from '@/stores/categories'
import { useAccountsStore } from '@/stores/accounts'
import TransactionModal from '@/components/TransactionModal.vue'
import type { TransactionResponse, TransactionRequest } from '@/types/transaction'
import { formatCurrency, formatDate, formatDateKey, currentMonthPeriod, periodToDateRange } from '@/utils/format'
import { useDesktop } from '@/utils/desktop'

const txStore = useTransactionsStore()
const catStore = useCategoriesStore()
const accStore = useAccountsStore()
const { isDesktop } = useDesktop()

const period = ref(currentMonthPeriod())
const typeFilter = ref<'ALL' | 'INCOME' | 'EXPENSE'>('ALL')
const modalOpen = ref(false)
const editingTx = ref<TransactionResponse | null>(null)
const deletingId = ref<string | null>(null)
const pageError = ref('')

const monthLabel = computed(() => {
  const [y, m] = period.value.split('-').map(Number)
  const label = new Date(y, m - 1, 1).toLocaleDateString('ru-RU', { month: 'long', year: 'numeric' })
  return label.charAt(0).toUpperCase() + label.slice(1)
})

function prevMonth() {
  const [y, m] = period.value.split('-').map(Number)
  const d = new Date(y, m - 2, 1)
  period.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
function nextMonth() {
  const [y, m] = period.value.split('-').map(Number)
  const d = new Date(y, m, 1)
  period.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

async function loadTransactions() {
  const { startDate, endDate } = periodToDateRange(period.value)
  await txStore.fetchTransactions({ startDate, endDate })
}

watch(period, loadTransactions)

onMounted(async () => {
  await Promise.all([
    loadTransactions(),
    catStore.fetchCategories(),
    accStore.fetchAccounts(),
    txStore.refreshPendingCount(),
  ])
})

function getCategoryName(categoryId: string): string {
  return catStore.categories.find((c) => c.id === categoryId)?.nameCategory ?? '—'
}

function getAccountName(accountId: string): string {
  const acc = accStore.accounts.find((a) => a.id === accountId)
  return acc ? acc.bankName : '—'
}

function formatShortDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })
}

const filteredTransactions = computed(() => {
  const all = txStore.transactions
  if (typeFilter.value === 'INCOME') return all.filter((t) => Number(t.amount) > 0)
  if (typeFilter.value === 'EXPENSE') return all.filter((t) => Number(t.amount) < 0)
  return all
})

const flatTransactions = computed(() =>
  [...filteredTransactions.value].sort(
    (a, b) => new Date(b.transactionDate).getTime() - new Date(a.transactionDate).getTime()
  )
)

const groupedByDay = computed(() => {
  const sorted = [...filteredTransactions.value].sort(
    (a, b) => new Date(b.transactionDate).getTime() - new Date(a.transactionDate).getTime()
  )
  const map = new Map<string, TransactionResponse[]>()
  sorted.forEach((tx) => {
    const key = formatDateKey(tx.transactionDate)
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(tx)
  })
  return Array.from(map.entries()).map(([date, items]) => ({ date, items }))
})

const totalIncome = computed(() =>
  txStore.transactions.filter((t) => Number(t.amount) > 0).reduce((s, t) => s + Number(t.amount), 0)
)
const totalExpense = computed(() =>
  txStore.transactions.filter((t) => Number(t.amount) < 0).reduce((s, t) => s + Math.abs(Number(t.amount)), 0)
)

function openCreate() {
  editingTx.value = null
  modalOpen.value = true
}

function openEdit(tx: TransactionResponse) {
  editingTx.value = tx
  modalOpen.value = true
}

async function onModalSubmit(request: TransactionRequest) {
  pageError.value = ''
  try {
    if (editingTx.value) {
      await txStore.updateTransaction(editingTx.value.id, request)
    } else {
      await txStore.createTransaction(request)
    }
    modalOpen.value = false
    await loadTransactions()
    await accStore.fetchAccounts()
  } catch (e: unknown) {
    const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
    pageError.value = msg || 'Ошибка при сохранении транзакции'
  }
}

async function doDelete() {
  if (!deletingId.value) return
  try {
    await txStore.deleteTransaction(deletingId.value)
    await loadTransactions()
    await accStore.fetchAccounts()
  } catch {
    pageError.value = 'Ошибка при удалении'
  } finally {
    deletingId.value = null
  }
}
</script>

<template>
  <div class="page">
    <!-- Page header -->
    <div class="page-header">
      <span class="page-title">Транзакции</span>
      <div class="header-right">
        <span v-if="txStore.pendingCount > 0" class="badge badge-warning">
          ⚡ {{ txStore.pendingCount }} не синхр.
        </span>
        <button class="btn btn-primary btn-sm desktop-add-btn" @click="openCreate">
          + Добавить
        </button>
      </div>
    </div>

    <!-- Controls: month nav + stats + filter tabs -->
    <div class="controls-wrap">
      <div class="month-nav card">
        <button class="month-btn" @click="prevMonth">‹</button>
        <span class="month-label">{{ monthLabel }}</span>
        <button class="month-btn" @click="nextMonth">›</button>
      </div>

      <div class="stats-row">
        <div class="stat-card income">
          <span class="stat-label">Доходы</span>
          <span class="stat-value amount-positive">{{ formatCurrency(totalIncome) }}</span>
        </div>
        <div class="stat-card expense">
          <span class="stat-label">Расходы</span>
          <span class="stat-value amount-negative">{{ formatCurrency(totalExpense) }}</span>
        </div>
      </div>

      <div class="filter-tabs">
        <button
          v-for="f in (['ALL', 'INCOME', 'EXPENSE'] as const)"
          :key="f"
          class="filter-tab"
          :class="{ active: typeFilter === f }"
          @click="typeFilter = f"
        >
          {{ f === 'ALL' ? 'Все' : f === 'INCOME' ? 'Доходы' : 'Расходы' }}
        </button>
      </div>
    </div>

    <div v-if="pageError" class="error-msg" style="margin-bottom: 12px">{{ pageError }}</div>
    <div v-if="txStore.loading" class="spinner" />

    <!-- ── Desktop: table view ── -->
    <template v-else-if="isDesktop">
      <div v-if="flatTransactions.length === 0" class="empty-state">
        Нет транзакций за этот период
      </div>
      <div v-else class="tx-table card">
        <div class="tx-table-head">
          <span>Дата</span>
          <span>Категория</span>
          <span>Счёт</span>
          <span>Описание</span>
          <span class="col-right">Сумма</span>
          <span></span>
        </div>
        <div
          v-for="tx in flatTransactions"
          :key="tx.id"
          class="tx-table-row"
          @click="openEdit(tx)"
        >
          <span class="td-date">{{ formatShortDate(tx.transactionDate) }}</span>
          <span class="td-category">
            <span class="ind-dot" :class="Number(tx.amount) > 0 ? 'income' : 'expense'" />
            {{ getCategoryName(tx.categoryId) }}
          </span>
          <span class="td-account">{{ getAccountName(tx.accountId) }}</span>
          <span class="td-desc">{{ tx.transactionDescription || '—' }}</span>
          <span
            class="td-amount col-right"
            :class="Number(tx.amount) > 0 ? 'amount-positive' : 'amount-negative'"
          >
            {{ Number(tx.amount) > 0 ? '+' : '' }}{{ formatCurrency(Number(tx.amount)) }}
          </span>
          <span class="td-actions" @click.stop>
            <button class="tx-del-btn" @click="deletingId = tx.id" title="Удалить">×</button>
          </span>
        </div>
      </div>
    </template>

    <!-- ── Mobile: grouped card view ── -->
    <template v-else>
      <div v-if="groupedByDay.length === 0" class="empty-state">
        Нет транзакций за этот период
      </div>
      <template v-else>
        <div v-for="group in groupedByDay" :key="group.date" class="tx-group">
          <div class="tx-date-label">
            {{ formatDate(group.date + 'T12:00:00Z') }}
          </div>
          <div class="card tx-list">
            <div
              v-for="(tx, i) in group.items"
              :key="tx.id"
              class="tx-item"
              :class="{ 'not-last': i < group.items.length - 1 }"
              @click="openEdit(tx)"
            >
              <div class="tx-indicator" :class="Number(tx.amount) > 0 ? 'income' : 'expense'" />
              <div class="tx-info">
                <span class="tx-category">{{ getCategoryName(tx.categoryId) }}</span>
                <span v-if="tx.transactionDescription" class="tx-desc">
                  {{ tx.transactionDescription }}
                </span>
              </div>
              <div class="tx-right">
                <span
                  class="tx-amount"
                  :class="Number(tx.amount) > 0 ? 'amount-positive' : 'amount-negative'"
                >
                  {{ Number(tx.amount) > 0 ? '+' : '' }}{{ formatCurrency(Number(tx.amount)) }}
                </span>
                <button class="tx-del-btn" @click.stop="deletingId = tx.id" title="Удалить">×</button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </template>

    <!-- FAB (mobile only) -->
    <button class="fab" @click="openCreate" title="Новая транзакция">+</button>

    <!-- Transaction Modal -->
    <TransactionModal
      v-if="modalOpen"
      :transaction="editingTx"
      :accounts="accStore.accounts"
      :categories="catStore.categories"
      @submit="onModalSubmit"
      @close="modalOpen = false"
    />

    <!-- Delete Confirm -->
    <div v-if="deletingId" class="modal-overlay" @click.self="deletingId = null">
      <div class="modal-sheet" style="padding-bottom: 24px">
        <div class="modal-title">Удалить транзакцию?</div>
        <p style="color: var(--text-muted); margin-bottom: 20px; font-size: 14px">
          Баланс счёта будет пересчитан автоматически.
        </p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="deletingId = null">Отмена</button>
          <button class="btn btn-danger" @click="doDelete">Удалить</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ── Page header ── */
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.desktop-add-btn { display: none; }

/* ── Controls wrap ── */
.controls-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

/* ── Month nav ── */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
}
.month-btn {
  background: none;
  border: none;
  color: var(--text);
  font-size: 22px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
}
.month-btn:hover { background: var(--surface-2); }
.month-label { font-size: 15px; font-weight: 700; color: var(--heading); }

/* ── Stats row ── */
.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.stat-card {
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--card-shadow);
  border-radius: var(--radius);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.stat-card.income { border-left: 3px solid var(--success); }
.stat-card.expense { border-left: 3px solid var(--danger); }
.stat-label { font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.04em; }
.stat-value { font-size: 16px; font-weight: 700; }

/* ── Filter tabs ── */
.filter-tabs {
  display: flex;
  gap: 6px;
}
.filter-tab {
  flex: 1;
  padding: 9px 6px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.filter-tab.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}

/* ── Desktop table ── */
.tx-table {
  padding: 0;
  overflow: hidden;
}
.tx-table-head,
.tx-table-row {
  display: grid;
  grid-template-columns: 110px 1fr 140px 1fr 150px 48px;
  align-items: center;
  padding: 0 16px;
  gap: 8px;
}
.tx-table-head {
  height: 40px;
  background: var(--surface-2);
  border-bottom: 1px solid var(--border);
  font-size: 12px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.tx-table-row {
  height: 52px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.12s;
}
.tx-table-row:last-child { border-bottom: none; }
.tx-table-row:hover { background: var(--surface-2); }

.col-right { text-align: right; }
.td-date { font-size: 13px; color: var(--text-muted); }
.td-category {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}
.ind-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.ind-dot.income { background: var(--success); }
.ind-dot.expense { background: var(--danger); }
.td-account { font-size: 13px; color: var(--text-muted); }
.td-desc {
  font-size: 13px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.td-amount { font-size: 15px; font-weight: 700; }
.td-actions { display: flex; justify-content: center; }

/* ── Mobile grouped list ── */
.tx-group { margin-bottom: 16px; }
.tx-date-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
  padding-left: 2px;
}
.tx-list { padding: 0; overflow: hidden; }
.tx-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.12s;
}
.tx-item:hover { background: var(--surface-2); }
.tx-item.not-last { border-bottom: 1px solid var(--border); }
.tx-indicator {
  width: 4px;
  height: 38px;
  border-radius: 2px;
  flex-shrink: 0;
}
.tx-indicator.income { background: var(--success); }
.tx-indicator.expense { background: var(--danger); }
.tx-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.tx-category { font-size: 14px; font-weight: 600; color: var(--text); }
.tx-desc {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tx-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.tx-amount { font-size: 15px; font-weight: 700; }

/* ── Shared delete button ── */
.tx-del-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 20px;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
  opacity: 0.5;
  transition: opacity 0.15s, color 0.15s;
}
.tx-del-btn:hover { opacity: 1; color: var(--danger); }

/* ── FAB (mobile only) ── */
.fab {
  position: fixed;
  bottom: calc(var(--nav-h) + 20px);
  right: max(20px, calc(50vw - 254px));
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2a6fd8, #0E40C7);
  color: #fff;
  border: none;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(12, 118, 247, 0.45);
  z-index: 50;
  transition: opacity 0.15s;
}
.fab:hover { opacity: 0.9; }

/* ── Desktop overrides ── */
@media (min-width: 768px) {
  .desktop-add-btn { display: inline-flex; }
  .fab { display: none; }

  .controls-wrap {
    flex-direction: row;
    align-items: stretch;
    gap: 16px;
    margin-bottom: 20px;
  }
  .controls-wrap .month-nav {
    flex-shrink: 0;
    width: 250px;
  }
  .controls-wrap .stats-row {
    flex: 1;
  }
  .controls-wrap .filter-tabs {
    flex-shrink: 0;
    flex-direction: column;
    gap: 4px;
  }
  .controls-wrap .filter-tab {
    flex: none;
    padding: 0 20px;
    height: 100%;
  }
}
</style>
