let webcam;
let fotoBase64 = "";
let tokenJwt = "";
let rolUsuario = "";
let emailUsuario = "";

document.addEventListener("DOMContentLoaded", () => {
    iniciarGeolocalizacion();
    iniciarCamara();
    inicializarToken();
    configurarLoginRest();
    configurarFormularioRest();
    configurarListadoRest();
    actualizarVistaPorRol();
});

function inicializarToken() {
    const tokenArea = document.getElementById("tokenJwt");
    const estadoToken = document.getElementById("estado-token");

    tokenArea.value = "";
    estadoToken.textContent = "Sin token";
    estadoToken.classList.remove("bg-success");
    estadoToken.classList.add("bg-secondary");
}

function actualizarVistaPorRol() {
    const bloqueEmailAdmin = document.getElementById("bloque-email-admin");

    if (!rolUsuario) {
        bloqueEmailAdmin.style.display = "none";
        return;
    }

    if (rolUsuario === "ADMIN") {
        bloqueEmailAdmin.style.display = "block";
    } else {
        bloqueEmailAdmin.style.display = "none";
    }
}

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

    webcam.start().catch((error) => {
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
        previewContainer.style.display = "block";

        mostrarMensaje("success", "Foto capturada correctamente.");
    } catch (error) {
        console.error("Error al tomar la foto:", error);
        mostrarMensaje("danger", "No se pudo capturar la foto.");
    }
}

function configurarLoginRest() {
    const form = document.getElementById("loginRestForm");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const email = document.getElementById("emailRest").value.trim();
        const contrasena = document.getElementById("passwordRest").value.trim();

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, contrasena })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || "No se pudo iniciar sesión.");
            }

            tokenJwt = data.token;
            rolUsuario = data.rol || "";
            emailUsuario = email;

            document.getElementById("tokenJwt").value = formatearToken(tokenJwt);

            const estadoToken = document.getElementById("estado-token");
            estadoToken.textContent = "Token activo";
            estadoToken.classList.remove("bg-secondary");
            estadoToken.classList.add("bg-success");

            actualizarVistaPorRol();

            mostrarMensaje("success", `Login REST exitoso. Sesión iniciada como ${rolUsuario}.`);
        } catch (error) {
            console.error("Error en login REST:", error);
            mostrarMensaje("danger", error.message);
        }
    });
}

function configurarFormularioRest() {
    const form = document.getElementById("restForm");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!tokenJwt) {
            mostrarMensaje("danger", "Debe iniciar sesión primero para obtener el token JWT.");
            return;
        }

        const nombre = document.getElementById("nombre").value.trim();
        const apellido = document.getElementById("apellido").value.trim();
        const sector = document.getElementById("sector").value.trim();
        const nivelEscolar = document.getElementById("nivelEscolar").value;
        const latitud = document.getElementById("latitud").value;
        const longitud = document.getElementById("longitud").value;

        if (!nombre || !sector || !nivelEscolar) {
            mostrarMensaje("danger", "Complete los campos obligatorios.");
            return;
        }

        const payload = {
            nombre,
            apellido,
            sector,
            nivelEscolar,
            foto: fotoBase64,
            posicion: {
                latitud: latitud ? parseFloat(latitud) : 0,
                longitud: longitud ? parseFloat(longitud) : 0
            }
        };

        try {
            const response = await fetch("/api/formularios", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + tokenJwt
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || "No se pudo crear el formulario.");
            }

            mostrarMensaje("success", data.mensaje || "Formulario creado correctamente por REST.");

            form.reset();
            document.getElementById("latitud").value = "";
            document.getElementById("longitud").value = "";
            document.getElementById("geo-status").textContent = "Detectando GPS...";
            document.getElementById("preview-container").style.display = "none";

            fotoBase64 = "";

            iniciarGeolocalizacion();
        } catch (error) {
            console.error("Error al crear formulario REST:", error);
            mostrarMensaje("danger", error.message);
        }
    });
}

function configurarListadoRest() {
    const boton = document.getElementById("btn-cargar-formularios");
    boton.addEventListener("click", cargarFormulariosRest);
}

async function cargarFormulariosRest() {
    const contenedor = document.getElementById("rest-listado");

    if (!tokenJwt) {
        mostrarMensaje("danger", "Debe iniciar sesión primero para consultar formularios.");
        return;
    }

    let url = "/api/formularios/losDelUsuario";

    if (rolUsuario === "ADMIN") {
        const emailBusqueda = document.getElementById("emailBusqueda").value.trim();

        if (emailBusqueda) {
            url = `/api/formularios/email/${encodeURIComponent(emailBusqueda)}`;
        }
    }

    try {
        const response = await fetch(url, {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + tokenJwt
            }
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "No se pudieron cargar los formularios.");
        }

        if (!data.length) {
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

        data.forEach(formulario => {
            const item = document.createElement("div");
            item.className = "col-12 col-md-6";

            item.innerHTML = `
                <div class="rest-card">
                    <div class="d-flex justify-content-between align-items-start gap-2">
                        <h6>${formulario.nombre || ""} ${formulario.apellido || ""}</h6>
                    </div>

                    <div class="rest-meta"><strong>Sector:</strong> ${formulario.sector || ""}</div>
                    <div class="rest-meta"><strong>Nivel escolar:</strong> ${formulario.nivelEscolar || ""}</div>
                    <div class="rest-meta"><strong>Latitud:</strong> ${formulario.latitud ?? ""}</div>
                    <div class="rest-meta"><strong>Longitud:</strong> ${formulario.longitud ?? ""}</div>
                    <div class="rest-meta"><strong>Fecha:</strong> ${formulario.fechaRegistro || ""}</div>

                    ${formulario.foto ? `<img src="${formulario.foto}" alt="Foto del formulario" class="foto-miniatura">` : ""}
                </div>
            `;

            contenedor.appendChild(item);
        });

        mostrarMensaje("success", "Formularios cargados correctamente.");
    } catch (error) {
        console.error("Error al listar formularios REST:", error);

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

    mensaje.className = "alert";
    mensaje.classList.add(`alert-${tipo}`);
    mensaje.classList.remove("d-none");
    mensaje.textContent = texto;
}

function formatearToken(token) {
    if (!token) return "";

    const inicio = token.substring(0, 20);
    const final = token.substring(token.length - 10);

    return `${inicio}...${final}`;
}