import apiClient from './client'
import type {
  TransactionResponse,
  TransactionRequest,
  TransactionFilters,
  TransactionSyncResponse,
} from '@/types/transaction'

export const transactionsApi = {
  getAll(filters: TransactionFilters) {
    return apiClient.get<TransactionResponse[]>('/api/transactions', { params: filters })
  },

  create(data: TransactionRequest) {
    return apiClient.post<{ id: string; message: string; newTotalBalance: number }>(
      '/api/transactions',
      data
    )
  },

  update(id: string, data: TransactionRequest) {
    return apiClient.put<{ message: string; newTotalBalance: number }>(
      `/api/transactions/${id}`,
      data
    )
  },

  delete(id: string) {
    return apiClient.delete<{ id: string; message: string; newTotalBalance: number }>(
      `/api/transactions/${id}`
    )
  },

  sync(data: TransactionRequest[]) {
    return apiClient.post<TransactionSyncResponse>('/api/transactions/sync', data)
  },
}
