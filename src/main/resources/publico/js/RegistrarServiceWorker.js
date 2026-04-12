if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/ServiceWorker.js')
        .then(() => console.log('Service Worker registrado'))
        .catch(err => console.log('Error:', err));
}