let webcam;
let fotoBase64 = "";

document.addEventListener("DOMContentLoaded", () => {
    iniciarGeolocalizacion();
    iniciarCamara();
    configurarFormularioGrpc();
    configurarListadoGrpc();
    actualizarVistaGrpcPorRol();
});

function iniciarGeolocalizacion() {
    const geoStatus = document.getElementById("geo-status");
    const latitudInput = document.getElementById("latitud");
    const longitudInput = document.getElementById("longitud");

    if (!navigator.geolocation) {
        geoStatus.textContent = "Geolocalización no soportada";
        return;
    }

    navigator.geolocation.getCurrentPosition(
        (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            latitudInput.value = lat;
            longitudInput.value = lng;
            geoStatus.textContent = `Lat: ${lat.toFixed(5)}, Lng: ${lng.toFixed(5)}`;
        },
        () => {
            geoStatus.textContent = "No se pudo obtener la ubicación";
        }
    );
}

function iniciarCamara() {
    const videoElement = document.getElementById("webcam");
    const canvasElement = document.getElementById("canvas");

    if (!videoElement || !canvasElement) return;

    webcam = new Webcam(videoElement, "user", canvasElement);

    webcam.start()
        .catch((error) => {
            console.error("Error al iniciar la cámara:", error);
            mostrarMensaje("danger", "No se pudo iniciar la cámara.");
        });
}

function tomarFoto() {
    try {
        if (!webcam) {
            mostrarMensaje("danger", "La cámara no está disponible.");
            return;
        }

        fotoBase64 = webcam.snap();

        const previewContainer = document.getElementById("preview-container");
        if (previewContainer) {
            previewContainer.style.display = "block";
        }

        mostrarMensaje("success", "Foto capturada correctamente.");

    } catch (error) {
        console.error("Error al tomar la foto:", error);
        mostrarMensaje("danger", "No se pudo capturar la foto.");
    }
}

function configurarFormularioGrpc() {
    const formulario = document.getElementById("grpcForm");

    formulario.addEventListener("submit", async (e) => {
        e.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const apellido = document.getElementById("apellido").value.trim();
        const sector = document.getElementById("sector").value.trim();
        const nivelEscolar = document.getElementById("nivelEscolar").value;
        const latitud = document.getElementById("latitud").value;
        const longitud = document.getElementById("longitud").value;

        if (!nombre || !sector || !nivelEscolar) {
            mostrarMensaje("danger", "Por favor, complete los campos obligatorios.");
            return;
        }

        const payload = {
            nombre: nombre,
            apellido: apellido,
            sector: sector,
            nivelEscolar: nivelEscolar,
            foto: fotoBase64,
            posicion: {
                latitud: latitud ? parseFloat(latitud) : 0,
                longitud: longitud ? parseFloat(longitud) : 0
            }
        };

        try {
            const response = await fetch("/grpc/formularios", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || "No se pudo crear el formulario por gRPC.");
            }

            mostrarMensaje("success", data.mensaje || "Formulario creado correctamente vía gRPC.");
            formulario.reset();

            document.getElementById("latitud").value = "";
            document.getElementById("longitud").value = "";
            document.getElementById("geo-status").textContent = "Detectando GPS...";
            document.getElementById("preview-container").style.display = "none";

            fotoBase64 = "";

            iniciarGeolocalizacion();
            cargarFormulariosGrpc();

        } catch (error) {
            console.error("Error al crear formulario gRPC:", error);
            mostrarMensaje("danger", error.message);
        }
    });
}

function configurarListadoGrpc() {
    const boton = document.getElementById("btn-cargar-formularios");
    if (boton) {
        boton.addEventListener("click", cargarFormulariosGrpc);
    }
}
function actualizarVistaGrpcPorRol() {
    const bloqueEmailAdmin = document.getElementById("bloque-email-admin-grpc");

    if (!bloqueEmailAdmin) return;

    if (typeof rolUsuarioGrpc !== "undefined" && rolUsuarioGrpc === "ADMIN") {
        bloqueEmailAdmin.style.display = "block";
    } else {
        bloqueEmailAdmin.style.display = "none";
    }
}
async function cargarFormulariosGrpc() {
    const contenedor = document.getElementById("grpc-listado");

    let url = "/grpc/formularios";

    if (typeof rolUsuarioGrpc !== "undefined" && rolUsuarioGrpc === "ADMIN") {
        const emailBusqueda = document.getElementById("emailBusquedaGrpc")?.value.trim();

        if (!emailBusqueda) {
            mostrarMensaje("danger", "Digite el correo del usuario para consultar sus formularios.");
            return;
        }

        url += `?email=${encodeURIComponent(emailBusqueda)}`;
    }

    try {
        const response = await fetch(url);
        const formularios = await response.json();

        if (!response.ok) {
            throw new Error(formularios.error || "No se pudieron cargar formularios por gRPC.");
        }

        if (!formularios.length) {
            contenedor.innerHTML = `
                <div class="col-12">
                    <div class="estado-vacio text-center text-muted">
                        No hay formularios para mostrar.
                    </div>
                </div>
            `;
            return;
        }

        contenedor.innerHTML = "";

        formularios.forEach(formulario => {
            const item = document.createElement("div");
            item.className = "col-12 col-md-6";

            item.innerHTML = `
                <div class="grpc-card">
                    <div class="d-flex justify-content-between align-items-start gap-2">
                        <h6>${formulario.nombre || ""} ${formulario.apellido || ""}</h6>
                        <span class="badge grpc-badge bg-success">
                            Sincronizado
                        </span>
                    </div>

                    <div class="grpc-meta"><strong>Sector:</strong> ${formulario.sector || ""}</div>
                    <div class="grpc-meta"><strong>Nivel escolar:</strong> ${formulario.nivelEscolar || ""}</div>
                    <div class="grpc-meta"><strong>Latitud:</strong> ${formulario.latitud ?? ""}</div>
                    <div class="grpc-meta"><strong>Longitud:</strong> ${formulario.longitud ?? ""}</div>
                    <div class="grpc-meta"><strong>Fecha:</strong> ${formulario.fechaRegistro || ""}</div>

                    ${formulario.foto ? `<img src="${formulario.foto}" alt="Foto del formulario" class="foto-miniatura">` : ""}
                </div>
            `;

            contenedor.appendChild(item);
        });

        mostrarMensaje("success", "Consulta realizada correctamente.");

    } catch (error) {
        console.error("Error al cargar formularios gRPC:", error);

        contenedor.innerHTML = `
            <div class="col-12">
                <div class="estado-vacio text-center text-danger">
                    ${error.message}
                </div>
            </div>
        `;

        mostrarMensaje("danger", error.message);
    }
}

function mostrarMensaje(tipo, texto) {
    const mensaje = document.getElementById("mensaje");

    mensaje.classList.remove("d-none", "alert-success", "alert-danger");
    mensaje.classList.add(`alert-${tipo}`);
    mensaje.textContent = texto;
}