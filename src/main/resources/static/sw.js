const CACHE_NAME = 'tictactoe-v1';

// Files to cache todo need to update
const ASSETS = [
    '/index.html',
    '/manifest.json',
    '/css/styles.css',
    '/js/home.js',
];

self.addEventListener('install', evt => {
    evt.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(ASSETS);
        })
    );
});

self.addEventListener('activate', evt => {
    evt.waitUntil(
        caches.keys().then(keys => {
            return Promise.all(
                keys.map(key => {
                    if (key !== CACHE_NAME) {
                        return caches.delete(key);
                    }
                })
            );
        })
    );
});

self.addEventListener('fetch', evt => {
    evt.respondWith(
        caches.match(evt.request).then(cachedResp => {
            return cachedResp || fetch(evt.request);
        })
    );
});