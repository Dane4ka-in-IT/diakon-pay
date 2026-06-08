import { transactionsApi } from '@/api/transactions'
import type { TransactionRequest } from '@/types/transaction'
import { getPendingTransactions, clearPendingTransactions } from './db'

export interface SyncResult {
  synced: number
  failed: number
  total: number
}

export async function syncPendingTransactions(): Promise<SyncResult> {
  const pending = await getPendingTransactions()
  if (pending.length === 0) return { synced: 0, failed: 0, total: 0 }

  const requests: TransactionRequest[] = pending.map((t) => ({
    id: t.id as string,
    accountId: t.accountId as string,
    categoryId: t.categoryId as string,
    amount: t.amount as number,
    exchangeRate: (t.exchangeRate as number | null) ?? null,
    transactionDescription: (t.transactionDescription as string | null) ?? null,
    transactionDate: (t.transactionDate as string | null) ?? null,
  }))

  const { data } = await transactionsApi.sync(requests)

  const syncedIds = data.updatedTransactions
    .filter((item) => item.status === 'SYNCED')
    .map((item) => item.id)

  const idsToRemove = syncedIds.length > 0 ? syncedIds : pending.map((t) => t.id as string)
  await clearPendingTransactions(idsToRemove)

  return {
    synced: data.processedCount,
    failed: data.failedCount,
    total: pending.length,
  }
}
