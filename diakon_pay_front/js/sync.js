const API_BASE = 'http://localhost:8080';

const syncTransactions = async () => {
    const pending = await getPendingTransactions();
    if (pending.length === 0) return;

    console.log(`[Sync] Найдено ${pending.length} офлайн-транзакций. Синхронизирую...`);
    showSyncBanner(`Синхронизирую ${pending.length} транзакций...`);

    const userId = localStorage.getItem('userId');

    try {
        const response = await fetch(`${API_BASE}/api/transactions/sync?userId=${userId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(pending)
        });

        if (response.ok) {
            const result = await response.json();
            const syncedIds = (result.updatedTransactions || [])
                .filter(t => t.status === 'SYNCED')
                .map(t => t.id);

            await clearPendingTransactions(syncedIds.length ? syncedIds : pending.map(t => t.id));

            console.log('[Sync] Успешно синхронизировано:', syncedIds.length || pending.length);
            showSyncBanner('✓ Данные синхронизированы', 'success');

            setTimeout(() => window.location.reload(), 1500);
        } else {
            console.warn('[Sync] Бэк вернул ошибку:', response.status);
            showSyncBanner('Ошибка синхронизации. Попробуем позже.', 'error');
        }
    } catch (e) {
        console.warn('[Sync] Бэк недоступен, работаем в мок-режиме:', e.message);
        showSyncBanner('✓ Работаем в офлайн-режиме', 'warning');
    }
};

const showSyncBanner = (message, type = 'info') => {
    let banner = document.getElementById('sync-banner');
    if (!banner) {
        banner = document.createElement('div');
        banner.id = 'sync-banner';
        banner.style.cssText = `
            position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%);
            padding: 10px 24px; border-radius: 24px; font-size: 14px; font-weight: 600;
            z-index: 9999; transition: opacity 0.5s; box-shadow: 0 4px 16px rgba(0,0,0,0.18);
        `;
        document.body.appendChild(banner);
    }

    const colors = {
        info: { bg: '#0C76F7', color: '#fff' },
        success: { bg: '#28a745', color: '#fff' },
        error: { bg: '#dc3545', color: '#fff' },
        warning: { bg: '#f5a623', color: '#fff' }
    };
    const c = colors[type] || colors.info;
    banner.style.background = c.bg;
    banner.style.color = c.color;
    banner.style.opacity = '1';
    banner.textContent = message;

    setTimeout(() => { banner.style.opacity = '0'; }, 4000);
};

window.addEventListener('online', () => {
    console.log('[Sync] Сеть восстановлена');
    syncTransactions();
});

window.addEventListener('offline', () => {
    showSyncBanner('⚡ Офлайн-режим. Данные сохраняются локально.', 'warning');
});