let modalEditar;
let vistaActual = "pendientes";

document.addEventListener("DOMContentLoaded", () => {
    const modalElement = document.getElementById("editarFormularioModal");
    modalEditar = new bootstrap.Modal(modalElement);

    document.getElementById("btn-guardar-cambios").addEventListener("click", guardarCambiosFormulario);
    document.getElementById("btn-ver-pendientes").addEventListener("click", () => cambiarVista("pendientes"));
    document.getElementById("btn-ver-sincronizados").addEventListener("click", () => cambiarVista("sincronizados"));
    document.getElementById("btn-sincronizar").addEventListener("click", sincronizarSeleccionados);

    cambiarVista("pendientes");
});

function cambiarVista(vista) {
    vistaActual = vista;

    const btnPendientes = document.getElementById("btn-ver-pendientes");
    const btnSincronizados = document.getElementById("btn-ver-sincronizados");
    const btnSincronizar = document.getElementById("btn-sincronizar");

    if (vista === "pendientes") {
        btnPendientes.className = "btn btn-primary";
        btnSincronizados.className = "btn btn-outline-primary";
        btnSincronizar.classList.remove("d-none");
        cargarPendientes();
    } else {
        btnPendientes.className = "btn btn-outline-primary";
        btnSincronizados.className = "btn btn-primary";
        btnSincronizar.classList.add("d-none");
        cargarSincronizados();
    }
}

function cargarPendientes() {
    const cardsContainer = document.getElementById("cards-formularios");
    const formularios = obtenerFormulariosLocales();

    if (!formularios.length) {
        cardsContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-light text-center shadow-sm">
                    No hay formularios pendientes.
                </div>
            </div>
        `;
        return;
    }

    cardsContainer.innerHTML = "";

    formularios.forEach(formulario => {
        const card = document.createElement("div");
        card.className = "col-12 col-md-6 col-lg-4";

        card.innerHTML = `
            <div class="card h-100 shadow-sm border-0">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox"
                                   ${formulario.seleccionado ? "checked" : ""}
                                   onchange="toggleSeleccion('${formulario.idLocal}', this.checked)">
                            <label class="form-check-label fw-semibold">
                                Seleccionar
                            </label>
                        </div>

                        <span class="badge bg-warning text-dark">Pendiente</span>
                    </div>

                    <h5 class="card-title fw-bold mb-3">
                        ${formulario.nombre || ""} ${formulario.apellido || ""}
                    </h5>

                    <p class="mb-2"><strong>Sector:</strong> ${formulario.sector || ""}</p>
                    <p class="mb-2"><strong>Nivel Escolar:</strong> ${formulario.nivelEscolar || ""}</p>
                    <p class="mb-2"><strong>Fecha:</strong> ${formulario.fechaRegistro || ""}</p>
                    <p class="mb-2"><strong>Latitud:</strong> ${formulario.posicion?.latitud ?? ""}</p>
                    <p class="mb-2"><strong>Longitud:</strong> ${formulario.posicion?.longitud ?? ""}</p>

                    ${formulario.foto ? `
                        <div class="mt-2 mb-2">
                            <img src="${formulario.foto}" alt="Foto del registro" 
                                 class="img-fluid rounded" style="max-height: 150px; width: 100%; object-fit: cover;">
                        </div>
                    ` : ''}

                    <div class="d-flex gap-2 mt-3">
                        <button class="btn btn-outline-primary btn-sm w-50" onclick="abrirModalEditarLocal('${formulario.idLocal}')">
                            <i class="bi bi-pencil-square me-1"></i> Editar
                        </button>
                        <button class="btn btn-outline-danger btn-sm w-50" onclick="eliminarFormularioPendiente('${formulario.idLocal}')">
                            <i class="bi bi-trash me-1"></i> Eliminar
                        </button>
                    </div>
                </div>
            </div>
        `;

        cardsContainer.appendChild(card);
    });
}

async function cargarSincronizados() {
    const cardsContainer = document.getElementById("cards-formularios");
    const mensaje = document.getElementById("mensaje");

    cardsContainer.innerHTML = `
        <div class="col-12">
            <div class="alert alert-light text-center shadow-sm">
                Cargando formularios...
            </div>
        </div>
    `;

    try {
        const response = await fetch("/formularios/sincronizados");
        const formularios = await response.json();

        if (!response.ok) {
            throw new Error(formularios.error || "No se pudieron cargar los formularios sincronizados.");
        }

        if (!formularios.length) {
            cardsContainer.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-light text-center shadow-sm">
                        No hay formularios sincronizados.
                    </div>
                </div>
            `;
            return;
        }

        cardsContainer.innerHTML = "";

        formularios.forEach(formulario => {
            const card = document.createElement("div");
            card.className = "col-12 col-md-6 col-lg-4";

            card.innerHTML = `
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body">
                        <div class="d-flex justify-content-end mb-3">
                            <span class="badge bg-success">Sincronizado</span>
                        </div>

                        <h5 class="card-title fw-bold mb-3">
                            ${formulario.nombre || ""} ${formulario.apellido || ""}
                        </h5>

                        <p class="mb-2"><strong>Sector:</strong> ${formulario.sector || ""}</p>
                        <p class="mb-2"><strong>Nivel Escolar:</strong> ${formulario.nivelEscolar || ""}</p>
                        <p class="mb-2"><strong>Fecha:</strong> ${formulario.fechaRegistro || ""}</p>
                        <p class="mb-2"><strong>Latitud:</strong> ${formulario.latitud ?? ""}</p>
                        <p class="mb-2"><strong>Longitud:</strong> ${formulario.longitud ?? ""}</p>

                        ${formulario.foto ? `
                            <div class="mt-2 mb-2">
                                <img src="${formulario.foto}" alt="Foto del registro" 
                                     class="img-fluid rounded" style="max-height: 150px; width: 100%; object-fit: cover;">
                            </div>
                        ` : ''}
                    </div>
                </div>
            `;

            cardsContainer.appendChild(card);
        });

    } catch (error) {
        console.error("Error al cargar sincronizados:", error);

        cardsContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger text-center shadow-sm">
                    Error al cargar formularios sincronizados.
                </div>
            </div>
        `;

        mensaje.classList.remove("d-none", "alert-success");
        mensaje.classList.add("alert-danger");
        mensaje.textContent = error.message;
    }
}

function toggleSeleccion(idLocal, checked) {
    actualizarFormularioLocal(idLocal, { seleccionado: checked });
}

function eliminarFormularioPendiente(idLocal) {
    const confirmar = confirm("¿Seguro que deseas eliminar este formulario pendiente?");
    if (!confirmar) return;

    const mensaje = document.getElementById("mensaje");

    eliminarFormularioLocal(idLocal);

    mensaje.classList.remove("d-none", "alert-danger");
    mensaje.classList.add("alert-success");
    mensaje.textContent = "Formulario pendiente eliminado correctamente.";

    cargarPendientes();
}

function abrirModalEditarLocal(idLocal) {
    const formularios = obtenerFormulariosLocales();
    const formulario = formularios.find(f => f.idLocal === idLocal);

    if (!formulario) return;

    document.getElementById("editar-id").value = formulario.idLocal || "";
    document.getElementById("editar-nombre").value = formulario.nombre || "";
    document.getElementById("editar-apellido").value = formulario.apellido || "";
    document.getElementById("editar-sector").value = formulario.sector || "";
    document.getElementById("editar-nivelEscolar").value = formulario.nivelEscolar || "";
    document.getElementById("editar-latitud").value = formulario.posicion?.latitud ?? "";
    document.getElementById("editar-longitud").value = formulario.posicion?.longitud ?? "";

    modalEditar.show();
}

function guardarCambiosFormulario() {
    const mensaje = document.getElementById("mensaje");
    const idLocal = document.getElementById("editar-id").value;

    const formularios = obtenerFormulariosLocales();
    const formularioActual = formularios.find(f => f.idLocal === idLocal);

    if (!formularioActual) return;

    const datosActualizados = {
        ...formularioActual,
        nombre: document.getElementById("editar-nombre").value.trim(),
        apellido: document.getElementById("editar-apellido").value.trim(),
        sector: document.getElementById("editar-sector").value.trim(),
        nivelEscolar: document.getElementById("editar-nivelEscolar").value
    };

    actualizarFormularioLocal(idLocal, datosActualizados);

    modalEditar.hide();

    mensaje.classList.remove("d-none", "alert-danger");
    mensaje.classList.add("alert-success");
    mensaje.textContent = "Formulario pendiente actualizado correctamente.";

    cargarPendientes();
}

async function sincronizarSeleccionados() {
    const mensaje = document.getElementById("mensaje");
    const formularios = obtenerFormulariosLocales();
    const seleccionados = formularios.filter(f => f.seleccionado);

    if (!seleccionados.length) {
        mensaje.classList.remove("d-none", "alert-success");
        mensaje.classList.add("alert-danger");
        mensaje.textContent = "No hay formularios seleccionados para sincronizar.";
        return;
    }

    let sincronizados = 0;
    const restantes = [...formularios];

    for (const formulario of seleccionados) {
        try {
            const response = await fetch("/formularios/sincronizar", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(formulario)
            });

            const data = await response.json();

            if (response.ok) {
                const index = restantes.findIndex(f => f.idLocal === formulario.idLocal);
                if (index !== -1) {
                    restantes.splice(index, 1);
                }
                sincronizados++;
            } else {
                console.error("No se pudo sincronizar:", data.error);
            }
        } catch (error) {
            console.error("Error al sincronizar formulario:", error);
        }
    }

    guardarFormulariosLocales(restantes);

    if (sincronizados > 0) {
        mensaje.classList.remove("d-none", "alert-danger");
        mensaje.classList.add("alert-success");
        mensaje.textContent = `${sincronizados} formulario(s) sincronizado(s) correctamente.`;
    } else {
        mensaje.classList.remove("d-none", "alert-success");
        mensaje.classList.add("alert-danger");
        mensaje.textContent = "No se pudo sincronizar ningún formulario.";
    }

    cargarPendientes();
}

const worker = new Worker('/js/sincronizarWorker.js');

worker.onmessage = function(e) {
    console.log('Mensaje del Worker:', e.data);
    if (e.data.tipo === 'CONECTADO') {
        sincronizarAutomatico();
    }
};

function sincronizarAutomatico() {
    const formularios = obtenerFormulariosLocales();
    const pendientes = formularios.filter(f => f.seleccionado && !f.sincronizado);

    console.log('Pendientes a sincronizar:', pendientes.length);

    if (pendientes.length === 0) return;

    const protocolo = window.location.protocol === "https:" ? "wss" : "ws";
    const url = `${protocolo}://${window.location.host}/sync`;
    console.log('Conectando WebSocket a:', url);

    const ws = new WebSocket(url);

    ws.onopen = function() {
        console.log('WebSocket abierto');
        pendientes.forEach(f => ws.send(JSON.stringify(f)));
    };

    ws.onmessage = function(e) {
        console.log('Respuesta del servidor:', e.data);
        const respuesta = JSON.parse(e.data);
        if (respuesta.estado === "OK") {
            const restantes = obtenerFormulariosLocales()
                .filter(f => f.idLocal !== respuesta.idLocal);
            guardarFormulariosLocales(restantes);
            cargarPendientes();
        }
    };

    ws.onerror = function(e) {
        console.log('Error WebSocket:', e);
    };

    ws.onclose = function(e) {
        console.log('WebSocket cerrado:', e.code, e.reason);
    };
}