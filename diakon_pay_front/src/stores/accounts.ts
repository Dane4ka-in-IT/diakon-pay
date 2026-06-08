import { defineStore } from 'pinia'
import { ref } from 'vue'
import { accountsApi } from '@/api/accounts'
import type { AccountResponse, AccountRequest } from '@/types/account'
import { cacheAccounts, getCachedAccounts } from '@/utils/db'

export const useAccountsStore = defineStore('accounts', () => {
  const accounts = ref<AccountResponse[]>([])
  const loading = ref(false)

  async function fetchAccounts() {
    loading.value = true
    try {
      const { data } = await accountsApi.getAll()
      accounts.value = data
      await cacheAccounts(data)
    } catch {
      accounts.value = (await getCachedAccounts()) as AccountResponse[]
    } finally {
      loading.value = false
    }
  }

  async function createAccount(request: AccountRequest) {
    await accountsApi.create(request)
    await fetchAccounts()
  }

  async function updateAccount(id: string, request: AccountRequest) {
    await accountsApi.update(id, request)
    await fetchAccounts()
  }

  async function deleteAccount(id: string) {
    await accountsApi.delete(id)
    await fetchAccounts()
  }

  return { accounts, loading, fetchAccounts, createAccount, updateAccount, deleteAccount }
})
