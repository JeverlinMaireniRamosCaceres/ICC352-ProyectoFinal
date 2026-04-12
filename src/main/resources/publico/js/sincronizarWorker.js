let estabaOffline = false;
let primeraVez = true;

setInterval(async () => {
    try {
        const response = await fetch('/estadoConexion', { cache: 'no-store' });
        if (response.ok) {
            if (estabaOffline || primeraVez) {
                estabaOffline = false;
                primeraVez = false;
                postMessage({ tipo: 'CONECTADO' });
            }
        }
    } catch (e) {
        estabaOffline = true;
        primeraVez = false;
        postMessage({ tipo: 'SIN_CONEXION' });
    }
}, 5000);