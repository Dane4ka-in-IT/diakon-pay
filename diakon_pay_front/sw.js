const CACHE_NAME = 'diakon-v2';
const ASSETS = [
    '/',
    '/login.html',
    '/register.html',
    '/transactions.html',
    '/js/db.js',
    '/js/sync.js',
    '/manifest.json',
    '/111.png',
    '/mocks/transactions.json',
    '/mocks/categories.json',
    '/mocks/accounts.json'
];

self.addEventListener('install', (e) => {
    e.waitUntil(
        caches.open(CACHE_NAME)
            .then(c => c.addAll(ASSETS))
            .then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', (e) => {
    e.waitUntil(
        caches.keys().then(keys =>
            Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
        ).then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', (e) => {
    if (e.request.method !== 'GET') return;
    const url = new URL(e.request.url);

    if (url.pathname.startsWith('/api/')) {
        e.respondWith(fetch(e.request).catch(() => new Response(null, { status: 503 })));
        return;
    }

    e.respondWith(
        caches.match(e.request).then(cached => {
            if (cached) return cached;
            return fetch(e.request).then(res => {
                const clone = res.clone();
                caches.open(CACHE_NAME).then(c => c.put(e.request, clone));
                return res;
            }).catch(() => new Response('Offline', { status: 503 }));
        })
    );
});