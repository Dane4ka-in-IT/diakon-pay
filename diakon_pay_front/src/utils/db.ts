const DB_NAME = 'diakon_pay_local_db'
const DB_VERSION = 2

function initDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION)

    request.onupgradeneeded = (e) => {
      const db = (e.target as IDBOpenDBRequest).result
      const stores = [
        'pending_transactions',
        'cached_transactions',
        'cached_categories',
        'cached_accounts',
      ]
      stores.forEach((name) => {
        if (!db.objectStoreNames.contains(name)) {
          db.createObjectStore(name, { keyPath: 'id' })
        }
      })
    }

    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

export async function saveTransactionOffline(transaction: Record<string, unknown>): Promise<void> {
  const db = await initDB()
  transaction._offline = true
  transaction._createdAt = new Date().toISOString()

  return new Promise((resolve, reject) => {
    const tx = db.transaction('pending_transactions', 'readwrite')
    tx.objectStore('pending_transactions').put(transaction)
    tx.oncomplete = () => resolve()
    tx.onerror = () => reject(tx.error)
  })
}

export async function getPendingTransactions(): Promise<Record<string, unknown>[]> {
  const db = await initDB()
  return new Promise((resolve) => {
    const tx = db.transaction('pending_transactions', 'readonly')
    const req = tx.objectStore('pending_transactions').getAll()
    req.onsuccess = () => resolve(req.result as Record<string, unknown>[])
    req.onerror = () => resolve([])
  })
}

export async function clearPendingTransactions(ids: string[]): Promise<void> {
  if (ids.length === 0) return
  const db = await initDB()
  const tx = db.transaction('pending_transactions', 'readwrite')
  const store = tx.objectStore('pending_transactions')
  ids.forEach((id) => store.delete(id))
  return new Promise((resolve) => {
    tx.oncomplete = () => resolve()
  })
}

async function cacheStore<T extends object>(storeName: string, list: T[]): Promise<void> {
  const db = await initDB()
  return new Promise((resolve, reject) => {
    const tx = db.transaction(storeName, 'readwrite')
    const store = tx.objectStore(storeName)
    store.clear()
    list.forEach((item) => store.put(item))
    tx.oncomplete = () => resolve()
    tx.onerror = () => reject(tx.error)
  })
}

async function getAllFromStore(storeName: string): Promise<unknown[]> {
  const db = await initDB()
  return new Promise((resolve) => {
    const tx = db.transaction(storeName, 'readonly')
    const req = tx.objectStore(storeName).getAll()
    req.onsuccess = () => resolve(req.result)
    req.onerror = () => resolve([])
  })
}

export const cacheTransactions = <T extends object>(list: T[]) =>
  cacheStore('cached_transactions', list)
export const getCachedTransactions = () => getAllFromStore('cached_transactions')

export const cacheCategories = <T extends object>(list: T[]) =>
  cacheStore('cached_categories', list)
export const getCachedCategories = () => getAllFromStore('cached_categories')

export const cacheAccounts = <T extends object>(list: T[]) =>
  cacheStore('cached_accounts', list)
export const getCachedAccounts = () => getAllFromStore('cached_accounts')
