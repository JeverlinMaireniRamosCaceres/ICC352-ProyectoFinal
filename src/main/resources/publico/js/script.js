let webcam;
let fotoBase64 = "";

document.addEventListener("DOMContentLoaded", () => {
    iniciarGeolocalizacion();
    iniciarCamara();
    configurarFormulario();
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
        });
}

function tomarFoto() {
    try {
        if (!webcam) {
            alert("La cámara no está disponible.");
            return;
        }

        fotoBase64 = webcam.snap();

        const previewContainer = document.getElementById("preview-container");
        if (previewContainer) {
            previewContainer.style.display = "block";
        }

    } catch (error) {
        console.error("Error al tomar la foto:", error);
        alert("No se pudo capturar la foto.");
    }
}

function configurarFormulario() {
    const formulario = document.getElementById("encuestaForm");

    formulario.addEventListener("submit", async (e) => {
        e.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const apellidoInput = document.getElementById("apellido");
        const apellido = apellidoInput ? apellidoInput.value.trim() : "";
        const sector = document.getElementById("sector").value.trim();
        const nivelEscolar = document.getElementById("nivelEscolar").value;
        const latitud = document.getElementById("latitud").value;
        const longitud = document.getElementById("longitud").value;

        if (!nombre || !sector || !nivelEscolar) {
            alert("Por favor, complete los campos obligatorios.");
            return;
        }

        const formularioData = {
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
            const response = await fetch("/api/formularios", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(formularioData)
            });

            const data = await response.json();

            if (!response.ok) {
                alert(data.error || "Error al guardar el formulario.");
                return;
            }

            alert("Formulario guardado correctamente.");
            formulario.reset();

            document.getElementById("latitud").value = "";
            document.getElementById("longitud").value = "";
            document.getElementById("geo-status").textContent = "Detectando GPS...";
            document.getElementById("preview-container").style.display = "none";
            fotoBase64 = "";

            iniciarGeolocalizacion();

        } catch (error) {
            console.error("Error al enviar formulario:", error);
            alert("No se pudo conectar con el servidor.");
        }
    });
}