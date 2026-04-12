let estabaOffline = false;

setInterval(async () => {
    try {
        const response = await fetch('/estadoConexion', { cache: 'no-store' });
        if (response.ok) {
            if (estabaOffline) {
                estabaOffline = false;
                postMessage({ tipo: 'CONECTADO' });
            }
        }
    } catch (e) {
        estabaOffline = true;
        postMessage({ tipo: 'SIN_CONEXION' });
    }
}, 5000);