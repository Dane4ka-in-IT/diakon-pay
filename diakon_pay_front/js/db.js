const DB_NAME = 'diakon_pay_local_db';
const DB_VERSION = 2;

const initDB = () => {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, DB_VERSION);

        request.onupgradeneeded = (e) => {
            const db = e.target.result;

            if (!db.objectStoreNames.contains('pending_transactions')) {
                db.createObjectStore('pending_transactions', { keyPath: 'id' });
            }

            if (!db.objectStoreNames.contains('cached_transactions')) {
                db.createObjectStore('cached_transactions', { keyPath: 'id' });
            }
            if (!db.objectStoreNames.contains('cached_categories')) {
                db.createObjectStore('cached_categories', { keyPath: 'id' });
            }
            if (!db.objectStoreNames.contains('cached_accounts')) {
                db.createObjectStore('cached_accounts', { keyPath: 'id' });
            }
        };

        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
    });
};

const generateUUID = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
};

const saveTransactionOffline = async (transaction) => {
    const db = await initDB();
    if (!transaction.id) transaction.id = generateUUID();
    transaction._offline = true;
    transaction._createdAt = new Date().toISOString();

    return new Promise((resolve) => {
        const tx = db.transaction('pending_transactions', 'readwrite');
        tx.objectStore('pending_transactions').put(transaction);
        tx.oncomplete = () => resolve(transaction);
    });
};

const getPendingTransactions = async () => {
    const db = await initDB();
    return new Promise((resolve) => {
        const tx = db.transaction('pending_transactions', 'readonly');
        const request = tx.objectStore('pending_transactions').getAll();
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => resolve([]);
    });
};

const clearPendingTransactions = async (ids) => {
    const db = await initDB();
    const tx = db.transaction('pending_transactions', 'readwrite');
    const store = tx.objectStore('pending_transactions');
    ids.forEach(id => store.delete(id));
    return new Promise((resolve) => tx.oncomplete = () => resolve());
};

const cacheTransactions = async (list) => {
    const db = await initDB();
    const tx = db.transaction('cached_transactions', 'readwrite');
    const store = tx.objectStore('cached_transactions');
    store.clear();
    list.forEach(item => store.put(item));
    return new Promise((resolve) => tx.oncomplete = () => resolve());
};

const getCachedTransactions = async () => {
    const db = await initDB();
    return new Promise((resolve) => {
        const tx = db.transaction('cached_transactions', 'readonly');
        const request = tx.objectStore('cached_transactions').getAll();
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => resolve([]);
    });
};

const cacheCategories = async (list) => {
    const db = await initDB();
    const tx = db.transaction('cached_categories', 'readwrite');
    const store = tx.objectStore('cached_categories');
    store.clear();
    list.forEach(item => store.put(item));
    return new Promise((resolve) => tx.oncomplete = () => resolve());
};

const getCachedCategories = async () => {
    const db = await initDB();
    return new Promise((resolve) => {
        const tx = db.transaction('cached_categories', 'readonly');
        const request = tx.objectStore('cached_categories').getAll();
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => resolve([]);
    });
};

const cacheAccounts = async (list) => {
    const db = await initDB();
    const tx = db.transaction('cached_accounts', 'readwrite');
    const store = tx.objectStore('cached_accounts');
    store.clear();
    list.forEach(item => store.put(item));
    return new Promise((resolve) => tx.oncomplete = () => resolve());
};

const getCachedAccounts = async () => {
    const db = await initDB();
    return new Promise((resolve) => {
        const tx = db.transaction('cached_accounts', 'readonly');
        const request = tx.objectStore('cached_accounts').getAll();
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => resolve([]);
    });
};