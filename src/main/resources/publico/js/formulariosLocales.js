function obtenerFormulariosLocales() {
    return JSON.parse(localStorage.getItem("formulariosPendientes")) || [];
}

function guardarFormulariosLocales(formularios) {
    localStorage.setItem("formulariosPendientes", JSON.stringify(formularios));
}

function agregarFormularioLocal(formulario) {
    const formularios = obtenerFormulariosLocales();
    formularios.push(formulario);
    guardarFormulariosLocales(formularios);
}

function eliminarFormularioLocal(idLocal) {
    const formularios = obtenerFormulariosLocales().filter(formulario => formulario.idLocal !== idLocal);
    guardarFormulariosLocales(formularios);
}

function actualizarFormularioLocal(idLocal, datosActualizados) {
    const formularios = obtenerFormulariosLocales().map(formulario =>
        formulario.idLocal === idLocal
            ? { ...formulario, ...datosActualizados }
            : formulario
    );

    guardarFormulariosLocales(formularios);
}