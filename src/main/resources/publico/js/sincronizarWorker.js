let estabaOffline = false;

setInterval(async () => {
    try {
        const response = await fetch('/estadoConexion', {
            cache: 'no-store',
            mode: 'no-cors'
        });
        if (estabaOffline) {
            estabaOffline = false;
            postMessage({ tipo: 'CONECTADO' });
        }
    } catch (e) {
        estabaOffline = true;
    }
}, 2000);