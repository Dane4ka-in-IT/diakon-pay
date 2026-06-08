<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { analyticsApi } from '@/api/analytics'
import type { AnalyticsTotals, AnalyticsByCategory, AnalyticsTotalBalance } from '@/types/analytics'
import { formatCurrency, currentMonthPeriod } from '@/utils/format'

ChartJS.register(ArcElement, Tooltip, Legend)

const CHART_COLORS = [
  '#0C76F7', '#25c26e', '#f0a030', '#f04f5a', '#8b5cf6',
  '#06b6d4', '#ec4899', '#f97316', '#84cc16', '#14b8a6',
  '#a78bfa', '#fb923c', '#4ade80', '#38bdf8',
]

const period = ref(currentMonthPeriod())
const totals = ref<AnalyticsTotals | null>(null)
const byCategory = ref<AnalyticsByCategory[]>([])
const totalBalance = ref<AnalyticsTotalBalance | null>(null)
const loading = ref(false)
const error = ref('')

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

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [t, cat, bal] = await Promise.all([
      analyticsApi.getTotals(period.value),
      analyticsApi.getByCategory(period.value),
      analyticsApi.getTotalBalance(),
    ])
    totals.value = t.data
    byCategory.value = cat.data
    totalBalance.value = bal.data
  } catch (e: unknown) {
    error.value = 'Не удалось загрузить аналитику'
  } finally {
    loading.value = false
  }
}

watch(period, loadData)
onMounted(loadData)

const chartData = computed(() => ({
  labels: byCategory.value.map((c) => c.categoryName),
  datasets: [
    {
      data: byCategory.value.map((c) => Number(c.amount)),
      backgroundColor: byCategory.value.map((_, i) => CHART_COLORS[i % CHART_COLORS.length]),
      borderWidth: 0,
      hoverOffset: 6,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '66%',
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: { raw: unknown }) => ` ${formatCurrency(Number(ctx.raw))}`,
      },
    },
  },
}

const netSavingsPositive = computed(() => Number(totals.value?.netSavings ?? 0) >= 0)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <span class="page-title">Аналитика</span>
    </div>

    <!-- Month Nav -->
    <div class="month-nav card">
      <button class="month-btn" @click="prevMonth">‹</button>
      <span class="month-label">{{ monthLabel }}</span>
      <button class="month-btn" @click="nextMonth">›</button>
    </div>

    <div v-if="error" class="error-msg" style="margin-bottom: 12px">{{ error }}</div>
    <div v-if="loading" class="spinner" />

    <template v-else>
      <!-- Total Balance -->
      <div v-if="totalBalance" class="card balance-card">
        <span class="balance-label">Общий баланс</span>
        <span class="balance-value">
          {{ formatCurrency(Number(totalBalance.totalBalance), totalBalance.currencyCode) }}
        </span>
      </div>

      <!-- Totals Cards -->
      <div v-if="totals" class="totals-grid">
        <div class="total-card income-card">
          <span class="total-label">Доходы</span>
          <span class="total-value amount-positive">
            +{{ formatCurrency(Number(totals.totalIncome)) }}
          </span>
        </div>
        <div class="total-card expense-card">
          <span class="total-label">Расходы</span>
          <span class="total-value amount-negative">
            -{{ formatCurrency(Number(totals.totalExpense)) }}
          </span>
        </div>
        <div class="total-card savings-card">
          <span class="total-label">Экономия</span>
          <span
            class="total-value"
            :class="netSavingsPositive ? 'amount-positive' : 'amount-negative'"
          >
            {{ netSavingsPositive ? '+' : '' }}{{ formatCurrency(Number(totals.netSavings)) }}
          </span>
        </div>
      </div>

      <!-- Chart -->
      <div v-if="byCategory.length > 0" class="card chart-section">
        <div class="chart-title">По категориям</div>
        <div class="chart-wrap">
          <Doughnut :data="chartData" :options="chartOptions" />
        </div>

        <!-- Legend -->
        <div class="category-list">
          <div
            v-for="(cat, i) in byCategory"
            :key="cat.categoryName"
            class="category-row"
          >
            <div class="cat-dot" :style="{ background: CHART_COLORS[i % CHART_COLORS.length] }" />
            <span class="cat-name">{{ cat.categoryName }}</span>
            <span class="cat-pct">{{ cat.percentage }}%</span>
            <span class="cat-amount">{{ formatCurrency(Number(cat.amount)) }}</span>
          </div>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-state">
        Нет данных за этот период
      </div>
    </template>
  </div>
</template>

<style scoped>
.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  margin-bottom: 12px;
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

.balance-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #0E40C7 0%, #0C76F7 100%);
  border-color: transparent;
  box-shadow: 0 4px 20px rgba(12, 118, 247, 0.35);
}
.balance-label {
  font-size: 12px;
  font-weight: 700;
  color: rgba(255,255,255,0.75);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.balance-value {
  font-size: 30px;
  font-weight: 800;
  color: #fff;
  margin-top: 4px;
}

.totals-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
  margin-bottom: 12px;
}
.total-card {
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--card-shadow);
  border-radius: var(--radius);
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-align: center;
}
.total-label {
  font-size: 10px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.total-value { font-size: 14px; font-weight: 700; }

.chart-section { margin-bottom: 12px; }
.chart-title { font-size: 13px; font-weight: 700; color: var(--text-muted); margin-bottom: 16px; text-transform: uppercase; letter-spacing: 0.04em; }
.chart-wrap { height: 200px; margin-bottom: 20px; }

.category-list { display: flex; flex-direction: column; gap: 8px; }
.category-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cat-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.cat-name { flex: 1; font-size: 14px; color: var(--text); }
.cat-pct { font-size: 12px; color: var(--text-muted); width: 36px; text-align: right; }
.cat-amount { font-size: 14px; font-weight: 700; color: var(--text); width: 90px; text-align: right; }

@media (min-width: 768px) {
  .totals-grid { grid-template-columns: 1fr 1fr 1fr; gap: 16px; margin-bottom: 16px; }
  .total-value { font-size: 18px; }

  .chart-section {
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto 1fr;
    gap: 0 40px;
    align-items: start;
  }
  .chart-title { grid-column: 1 / -1; margin-bottom: 20px; }
  .chart-wrap  { grid-column: 1; grid-row: 2; height: 300px; margin-bottom: 0; }
  .category-list { grid-column: 2; grid-row: 2; }
}
</style>
