import apiClient from './client'
import type { AccountResponse, AccountRequest } from '@/types/account'

export const accountsApi = {
  getAll() {
    return apiClient.get<AccountResponse[]>('/api/accounts')
  },

  create(data: AccountRequest) {
    return apiClient.post<{ message: string }>('/api/accounts', data)
  },

  update(id: string, data: AccountRequest) {
    return apiClient.put<{ message: string }>(`/api/accounts/${id}`, data)
  },

  delete(id: string) {
    return apiClient.delete<{ message: string }>(`/api/accounts/${id}`)
  },
}
