setInterval(() => {
    if (navigator.onLine) {
        postMessage({ tipo: 'CONECTADO' });
    } else {
        postMessage({ tipo: 'SIN_CONEXION' });
    }
}, 5000);