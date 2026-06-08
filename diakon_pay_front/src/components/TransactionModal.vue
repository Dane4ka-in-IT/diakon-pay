<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TransactionResponse, TransactionRequest } from '@/types/transaction'
import type { AccountResponse } from '@/types/account'
import type { CategoryResponse } from '@/types/category'
import { toInputDatetime, fromInputDatetime } from '@/utils/format'

const props = defineProps<{
  transaction: TransactionResponse | null
  accounts: AccountResponse[]
  categories: CategoryResponse[]
}>()

const emit = defineEmits<{
  submit: [data: TransactionRequest]
  close: []
}>()

const form = ref({
  accountId: '',
  categoryId: '',
  amount: '',
  description: '',
  date: '',
})
const error = ref('')
const submitting = ref(false)

const incomeCategories = computed(() => props.categories.filter((c) => c.type === 'INCOME'))
const expenseCategories = computed(() => props.categories.filter((c) => c.type === 'EXPENSE'))

function nowDatetime(): string {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
}

watch(
  () => props.transaction,
  (tx) => {
    if (tx) {
      form.value = {
        accountId: tx.accountId,
        categoryId: tx.categoryId,
        amount: String(Math.abs(Number(tx.amount))),
        description: tx.transactionDescription ?? '',
        date: toInputDatetime(tx.transactionDate),
      }
    } else {
      form.value = {
        accountId: props.accounts[0]?.id ?? '',
        categoryId: '',
        amount: '',
        description: '',
        date: nowDatetime(),
      }
    }
    error.value = ''
  },
  { immediate: true }
)

async function onSubmit() {
  error.value = ''

  if (!form.value.accountId) { error.value = 'Выберите счёт'; return }
  if (!form.value.categoryId) { error.value = 'Выберите категорию'; return }
  const amount = parseFloat(form.value.amount)
  if (!amount || amount <= 0) { error.value = 'Введите сумму больше 0'; return }

  submitting.value = true
  try {
    const request: TransactionRequest = {
      id: props.transaction?.id,
      accountId: form.value.accountId,
      categoryId: form.value.categoryId,
      amount,
      exchangeRate: 1,
      transactionDescription: form.value.description || null,
      transactionDate: form.value.date ? fromInputDatetime(form.value.date) : new Date().toISOString(),
    }
    emit('submit', request)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-sheet">
      <div class="modal-title">
        {{ transaction ? 'Редактировать транзакцию' : 'Новая транзакция' }}
      </div>

      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label class="form-label">Счёт</label>
          <select v-model="form.accountId" class="form-input">
            <option value="" disabled>Выберите счёт</option>
            <option v-for="acc in accounts" :key="acc.id" :value="acc.id">
              {{ acc.bankName }} ({{ acc.currencyCode }})
            </option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Категория</label>
          <select v-model="form.categoryId" class="form-input">
            <option value="" disabled>Выберите категорию</option>
            <optgroup label="Доходы" v-if="incomeCategories.length">
              <option v-for="cat in incomeCategories" :key="cat.id" :value="cat.id">
                {{ cat.nameCategory }}
              </option>
            </optgroup>
            <optgroup label="Расходы" v-if="expenseCategories.length">
              <option v-for="cat in expenseCategories" :key="cat.id" :value="cat.id">
                {{ cat.nameCategory }}
              </option>
            </optgroup>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Сумма (₽)</label>
          <input
            v-model="form.amount"
            type="number"
            step="0.01"
            min="0.01"
            class="form-input"
            placeholder="0.00"
          />
        </div>

        <div class="form-group">
          <label class="form-label">Описание</label>
          <input
            v-model="form.description"
            type="text"
            class="form-input"
            placeholder="Необязательно"
          />
        </div>

        <div class="form-group">
          <label class="form-label">Дата и время</label>
          <input v-model="form.date" type="datetime-local" class="form-input" />
        </div>

        <div v-if="error" class="error-msg" style="margin-bottom: 12px">{{ error }}</div>

        <div class="modal-actions">
          <button type="button" class="btn btn-ghost" @click="emit('close')">Отмена</button>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ transaction ? 'Сохранить' : 'Добавить' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
