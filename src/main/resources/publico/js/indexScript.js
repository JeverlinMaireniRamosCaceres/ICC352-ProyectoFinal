document.addEventListener('DOMContentLoaded', function() {
    actualizarEstadoSincronizacion();
});

function actualizarEstadoSincronizacion() {
    const formularios = JSON.parse(localStorage.getItem('formulariosPendientes') || '[]');
    const pendientes = formularios.filter(f => !f.sincronizado);

    const syncStatus = document.getElementById('sync-status');

    if (pendientes.length === 0) {
        syncStatus.textContent = 'Todas las encuestas están sincronizadas.';
    } else {
        syncStatus.textContent = `Tienes ${pendientes.length} encuesta(s) pendiente(s) de subir al servidor.`;
    }
}