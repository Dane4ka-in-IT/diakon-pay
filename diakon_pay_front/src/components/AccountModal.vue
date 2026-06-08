<script setup lang="ts">
import { ref, watch } from 'vue'
import type { AccountResponse, AccountRequest } from '@/types/account'

const props = defineProps<{
  account: AccountResponse | null
}>()

const emit = defineEmits<{
  submit: [data: AccountRequest]
  close: []
}>()

const CURRENCIES = ['RUB', 'USD', 'EUR', 'CNY', 'BYN', 'KZT']

const form = ref({
  bankName: '',
  accountNumber: '',
  balance: '',
  currencyCode: 'RUB',
})
const error = ref('')

watch(
  () => props.account,
  (acc) => {
    if (acc) {
      form.value = {
        bankName: acc.bankName,
        accountNumber: acc.accountNumber,
        balance: String(acc.balance),
        currencyCode: acc.currencyCode,
      }
    } else {
      form.value = { bankName: '', accountNumber: '', balance: '', currencyCode: 'RUB' }
    }
    error.value = ''
  },
  { immediate: true }
)

function onSubmit() {
  error.value = ''
  if (!form.value.bankName.trim()) { error.value = 'Введите название банка'; return }
  if (!form.value.accountNumber.trim()) { error.value = 'Введите номер счёта'; return }

  const request: AccountRequest = {
    id: props.account?.id,
    bankName: form.value.bankName.trim(),
    accountNumber: form.value.accountNumber.trim(),
    balance: parseFloat(form.value.balance) || 0,
    currencyCode: form.value.currencyCode,
  }
  emit('submit', request)
}
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-sheet">
      <div class="modal-title">{{ account ? 'Редактировать счёт' : 'Новый счёт' }}</div>

      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label class="form-label">Название банка</label>
          <input v-model="form.bankName" type="text" class="form-input" placeholder="Т-Банк" maxlength="50" />
        </div>

        <div class="form-group">
          <label class="form-label">Номер / маска</label>
          <input v-model="form.accountNumber" type="text" class="form-input" placeholder="*4455" maxlength="20" />
        </div>

        <div class="form-group">
          <label class="form-label">Баланс</label>
          <input v-model="form.balance" type="number" step="0.01" class="form-input" placeholder="0.00" />
        </div>

        <div class="form-group">
          <label class="form-label">Валюта</label>
          <select v-model="form.currencyCode" class="form-input">
            <option v-for="c in CURRENCIES" :key="c" :value="c">{{ c }}</option>
          </select>
        </div>

        <div v-if="error" class="error-msg" style="margin-bottom: 12px">{{ error }}</div>

        <div class="modal-actions">
          <button type="button" class="btn btn-ghost" @click="emit('close')">Отмена</button>
          <button type="submit" class="btn btn-primary">
            {{ account ? 'Сохранить' : 'Создать' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
