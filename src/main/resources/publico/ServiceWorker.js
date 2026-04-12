const CACHE_NAME = 'encuestas-cache-v1';
const URLS_A_CACHEAR = [
    '/',
    '/index',
    '/formulario',
    '/formularios',
    '/mapa',
    '/usuarios'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(URLS_A_CACHEAR);
        })
    );
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    event.waitUntil(clients.claim());
});

self.addEventListener('fetch', event => {
    if (!event.request.url.startsWith('http')) return;
    if (event.request.method !== 'GET') return;
    if (event.request.url.includes('/sync')) return;
    if (event.request.url.includes('/estadoConexion')) return;

    event.respondWith(
        fetch(event.request)
            .then(response => {

                if (!response || response.status !== 200 || response.type === 'opaque') {
                    return response;
                }
                const copia = response.clone();
                caches.open(CACHE_NAME).then(cache => {
                    cache.put(event.request, copia);
                });
                return response;
            })
            .catch(() => {
                return caches.match(event.request);
            })
    );
});