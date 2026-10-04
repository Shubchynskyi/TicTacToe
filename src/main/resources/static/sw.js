const CACHE_NAME = 'tictactoe-ui-v3';
const ASSETS = [
    '/manifest.json', '/css/styles.css', '/js/ui.js', '/js/home.js',
    '/js/localGame.js', '/js/online.js', '/js/onlineGame.js',
    '/images/icon_pwa/icon-v2-192.png', '/images/icon_pwa/icon-v2-512.png',
    '/images/icon_pwa/icon-v2-maskable-512.png',
    '/images/icon_pwa/apple-touch-icon-v2.png', '/favicon-v2.ico'
];
self.addEventListener('install', event => {
    event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', event => {
    event.waitUntil(caches.keys().then(keys => Promise.all(
        keys.filter(key => key.startsWith('tictactoe-') && key !== CACHE_NAME).map(key => caches.delete(key))
    )).then(() => self.clients.claim()));
});
self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);
    // Session pages, game state and WebSocket traffic always go to the server.
    if (event.request.method !== 'GET' || url.origin !== self.location.origin || !ASSETS.includes(url.pathname)) return;
    event.respondWith(fetch(event.request).then(response => {
        if (response.ok) {
            const copy = response.clone();
            event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.put(event.request, copy)));
        }
        return response;
    }).catch(() => caches.match(event.request)));
});
