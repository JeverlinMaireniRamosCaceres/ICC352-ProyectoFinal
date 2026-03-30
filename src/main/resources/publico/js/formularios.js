let modalEditar;

document.addEventListener("DOMContentLoaded", () => {
    const modalElement = document.getElementById("editarFormularioModal");
    modalEditar = new bootstrap.Modal(modalElement);

    cargarFormularios();

    document.getElementById("btn-guardar-cambios").addEventListener("click", guardarCambiosFormulario);
});

async function cargarFormularios() {
    const cardsContainer = document.getElementById("cards-formularios");
    const mensaje = document.getElementById("mensaje");

    try {
        const response = await fetch("/api/formularios");
        const formularios = await response.json();

        if (!response.ok) {
            throw new Error(formularios.error || "No se pudieron cargar los formularios.");
        }

        if (!formularios.length) {
            cardsContainer.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-light text-center shadow-sm">
                        No hay formularios registrados.
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
                        <h5 class="card-title fw-bold mb-3">
                            ${formulario.nombre || ""} ${formulario.apellido || ""}
                        </h5>

                        <p class="mb-2"><strong>Sector:</strong> ${formulario.sector || ""}</p>
                        <p class="mb-2"><strong>Nivel Escolar:</strong> ${formulario.nivelEscolar || ""}</p>
                        <p class="mb-2"><strong>Fecha:</strong> ${formulario.fechaRegistro || ""}</p>
                        <p class="mb-2"><strong>Latitud:</strong> ${formulario.latitud ?? ""}</p>
                        <p class="mb-2"><strong>Longitud:</strong> ${formulario.longitud ?? ""}</p>
                        <p class="mb-3">
                            <strong>Sincronizado:</strong>
                            <span class="badge ${formulario.sincronizado ? "bg-success" : "bg-warning text-dark"}">
                                ${formulario.sincronizado ? "Sí" : "No"}
                            </span>
                        </p>

                        <div class="d-flex gap-2">
                            <button class="btn btn-outline-primary btn-sm w-50" onclick="abrirModalEditar('${formulario.id}')">
                                <i class="bi bi-pencil-square me-1"></i> Editar
                            </button>
                            <button class="btn btn-outline-danger btn-sm w-50" onclick="eliminarFormulario('${formulario.id}')">
                                <i class="bi bi-trash me-1"></i> Eliminar
                            </button>
                        </div>
                    </div>
                </div>
            `;

            cardsContainer.appendChild(card);
        });

    } catch (error) {
        console.error("Error al cargar formularios:", error);

        cardsContainer.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger text-center shadow-sm">
                    Error al cargar formularios.
                </div>
            </div>
        `;

        mensaje.classList.remove("d-none", "alert-success");
        mensaje.classList.add("alert-danger");
        mensaje.textContent = error.message;
    }
}

async function eliminarFormulario(id) {
    const confirmar = confirm("¿Seguro que deseas eliminar este formulario?");

    if (!confirmar) return;

    const mensaje = document.getElementById("mensaje");

    try {
        const response = await fetch(`/api/formularios/${id}`, {
            method: "DELETE"
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "No se pudo eliminar el formulario.");
        }

        mensaje.classList.remove("d-none", "alert-danger");
        mensaje.classList.add("alert-success");
        mensaje.textContent = data.mensaje || "Formulario eliminado correctamente.";

        cargarFormularios();

    } catch (error) {
        console.error("Error al eliminar formulario:", error);

        mensaje.classList.remove("d-none", "alert-success");
        mensaje.classList.add("alert-danger");
        mensaje.textContent = error.message;
    }
}

async function abrirModalEditar(id) {
    const mensaje = document.getElementById("mensaje");

    try {
        const response = await fetch(`/api/formularios/${id}`);
        const formulario = await response.json();

        if (!response.ok) {
            throw new Error(formulario.error || "No se pudo cargar el formulario.");
        }

        document.getElementById("editar-id").value = formulario.id || "";
        document.getElementById("editar-nombre").value = formulario.nombre || "";
        document.getElementById("editar-apellido").value = formulario.apellido || "";
        document.getElementById("editar-sector").value = formulario.sector || "";
        document.getElementById("editar-nivelEscolar").value = formulario.nivelEscolar || "";
        document.getElementById("editar-latitud").value = formulario.latitud ?? "";
        document.getElementById("editar-longitud").value = formulario.longitud ?? "";

        modalEditar.show();

    } catch (error) {
        console.error("Error al cargar formulario para editar:", error);

        mensaje.classList.remove("d-none", "alert-success");
        mensaje.classList.add("alert-danger");
        mensaje.textContent = error.message;
    }
}

async function guardarCambiosFormulario() {
    const mensaje = document.getElementById("mensaje");
    const id = document.getElementById("editar-id").value;

    const formularioActualizado = {
        nombre: document.getElementById("editar-nombre").value.trim(),
        apellido: document.getElementById("editar-apellido").value.trim(),
        sector: document.getElementById("editar-sector").value.trim(),
        nivelEscolar: document.getElementById("editar-nivelEscolar").value
    };

    try {
        const response = await fetch(`/api/formularios/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(formularioActualizado)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "No se pudo actualizar el formulario.");
        }

        modalEditar.hide();

        mensaje.classList.remove("d-none", "alert-danger");
        mensaje.classList.add("alert-success");
        mensaje.textContent = data.mensaje || "Formulario actualizado correctamente.";

        cargarFormularios();

    } catch (error) {
        console.error("Error al actualizar formulario:", error);

        mensaje.classList.remove("d-none", "alert-success");
        mensaje.classList.add("alert-danger");
        mensaje.textContent = error.message;
    }
}
