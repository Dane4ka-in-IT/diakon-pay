import { defineStore } from 'pinia'
import { ref } from 'vue'
import { transactionsApi } from '@/api/transactions'
import type { TransactionResponse, TransactionRequest, TransactionFilters } from '@/types/transaction'
import { cacheTransactions, getCachedTransactions, saveTransactionOffline, getPendingTransactions } from '@/utils/db'

export const useTransactionsStore = defineStore('transactions', () => {
  const transactions = ref<TransactionResponse[]>([])
  const loading = ref(false)
  const pendingCount = ref(0)

  async function refreshPendingCount() {
    const pending = await getPendingTransactions()
    pendingCount.value = pending.length
  }

  async function fetchTransactions(filters: TransactionFilters) {
    loading.value = true
    try {
      const { data } = await transactionsApi.getAll(filters)
      transactions.value = data
      await cacheTransactions(data)
    } catch {
      transactions.value = (await getCachedTransactions()) as TransactionResponse[]
    } finally {
      loading.value = false
    }
  }

  async function createTransaction(request: TransactionRequest): Promise<{ newTotalBalance: number } | null> {
    if (!navigator.onLine) {
      const id = request.id ?? crypto.randomUUID()
      await saveTransactionOffline({ ...request, id })
      await refreshPendingCount()
      return null
    }
    const { data } = await transactionsApi.create(request)
    return { newTotalBalance: data.newTotalBalance }
  }

  async function updateTransaction(id: string, request: TransactionRequest) {
    const { data } = await transactionsApi.update(id, request)
    return data
  }

  async function deleteTransaction(id: string) {
    const { data } = await transactionsApi.delete(id)
    return data
  }

  return {
    transactions,
    loading,
    pendingCount,
    fetchTransactions,
    createTransaction,
    updateTransaction,
    deleteTransaction,
    refreshPendingCount,
  }
})
