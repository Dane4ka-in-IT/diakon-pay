export interface TransactionResponse {
  id: string
  userId: number
  accountId: string
  categoryId: string
  amount: number
  exchangeRate: number | null
  transactionDescription: string | null
  transactionDate: string
}

export interface TransactionRequest {
  id?: string
  accountId: string
  categoryId: string
  amount: number
  exchangeRate?: number | null
  transactionDescription?: string | null
  transactionDate?: string | null
}

export interface TransactionFilters {
  startDate: string
  endDate: string
  type?: string
  category?: string
}

export interface TransactionSyncItem {
  id: string
  status: 'SYNCED' | 'FAILED'
}

export interface TransactionSyncResponse {
  processedCount: number
  failedCount: number
  syncStatus: 'SUCCESS' | 'PARTIAL' | 'FAILED'
  updatedTransactions: TransactionSyncItem[]
}

export interface PendingTransaction {
  id: string
  accountId: string
  categoryId: string
  amount: number
  exchangeRate?: number | null
  transactionDescription?: string | null
  transactionDate?: string | null
  _offline: boolean
  _createdAt: string
}
