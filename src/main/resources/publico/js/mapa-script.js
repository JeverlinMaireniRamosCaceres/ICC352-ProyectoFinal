    let map;
    let markersLayer;
    const marcadores = {};

    document.addEventListener("DOMContentLoaded", () => {
        inicializarMapa();
        cargarRegistrosEnMapa();
    });

    function inicializarMapa() {
        map = L.map("map").setView([19.4517, -70.6970], 8);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        markersLayer = L.layerGroup().addTo(map);
    }

    async function cargarRegistrosEnMapa() {
        const lista = document.getElementById("lista-puntos");
        const contador = document.getElementById("contador-registros");
        const mensaje = document.getElementById("mensaje-mapa");

        try {
            const response = await fetch("/mapa/datos");
            const formularios = await response.json();

            if (!response.ok) {
                throw new Error(formularios.error || "No se pudieron cargar los registros.");
            }

            const registrosValidos = formularios.filter(f =>
                f.latitud !== null &&
                f.longitud !== null &&
                !isNaN(f.latitud) &&
                !isNaN(f.longitud) &&
                Number(f.latitud) !== 0 &&
                Number(f.longitud) !== 0
            );

            contador.textContent = registrosValidos.length;

            if (!registrosValidos.length) {
                lista.innerHTML = `
                    <div class="text-center mt-4 text-muted small">
                        <i class="bi bi-info-circle"></i> No hay registros con ubicación disponible.
                    </div>
                `;
                return;
            }

            lista.innerHTML = "";
            markersLayer.clearLayers();

            const bounds = [];

            registrosValidos.forEach((registro, index) => {
                const lat = Number(registro.latitud);
                const lng = Number(registro.longitud);
                const nombreCompleto = `${registro.nombre || ""} ${registro.apellido || ""}`.trim();
                const nombreMostrar = nombreCompleto || "Sin nombre";

               const popupHtml = `
                   <div style="min-width: 200px;">
                       ${registro.foto ? `
                           <img
                               src="${registro.foto}"
                               alt="Foto del registro"
                               style="width: 100%; height: 120px; object-fit: cover; border-radius: 8px; margin-bottom: 8px;"
                           >
                       ` : ""}

                       <h6 class="mb-2">${nombreMostrar}</h6>
                       <p class="mb-1"><strong>Sector:</strong> ${registro.sector || "No disponible"}</p>
                       <p class="mb-1"><strong>Nivel Escolar:</strong> ${registro.nivelEscolar || "No disponible"}</p>
                       <p class="mb-1"><strong>Registrado por:</strong> ${registro.usuarioNombre || "No disponible"}</p>
                       <p class="mb-0"><strong>Fecha:</strong> ${formatearFecha(registro.fechaRegistro)}</p>
                   </div>
               `;

                const marker = L.marker([lat, lng]).addTo(markersLayer).bindPopup(popupHtml);

                const markerId = registro.id || `registro-${index}`;
                marcadores[markerId] = marker;

                marker.on("click", () => {
                    resaltarItemLista(markerId);
                });

                bounds.push([lat, lng]);

                const item = document.createElement("button");
                item.type = "button";
                item.className = "list-group-item list-group-item-action py-3 item-registro";
                item.dataset.id = markerId;

             item.innerHTML = `
                 <div class="d-flex w-100 justify-content-between">
                     <h6 class="mb-1 fw-bold text-primary">${nombreMostrar}</h6>
                     <small class="text-muted">${formatearHora(registro.fechaRegistro)}</small>
                 </div>
                 <p class="mb-1 small">Sector: ${registro.sector || "No disponible"}</p>
                 <p class="mb-1 small">Registrado por: ${registro.usuarioNombre || "No disponible"}</p>
                 <small class="text-secondary">
                     <i class="bi bi-geo-alt"></i> ${lat.toFixed(5)}, ${lng.toFixed(5)}
                 </small>
             `;

                item.addEventListener("click", () => {
                    map.setView([lat, lng], 15, { animate: true });
                    marker.openPopup();
                    resaltarItemLista(markerId);
                });

                lista.appendChild(item);
            });

            if (bounds.length === 1) {
                map.setView(bounds[0], 15);
            } else {
                map.fitBounds(bounds, { padding: [30, 30] });
            }

        } catch (error) {
            console.error("Error al cargar registros en el mapa:", error);

            lista.innerHTML = `
                <div class="text-center mt-4 text-danger small">
                    <i class="bi bi-exclamation-triangle"></i> Error al cargar registros.
                </div>
            `;

            mensaje.classList.remove("d-none");
            mensaje.classList.remove("alert-success");
            mensaje.classList.add("alert-danger");
            mensaje.textContent = error.message;
        }
    }

    function resaltarItemLista(id) {
        document.querySelectorAll(".item-registro").forEach(item => {
            item.classList.remove("active");
        });

        const itemActivo = document.querySelector(`.item-registro[data-id="${id}"]`);
        if (itemActivo) {
            itemActivo.classList.add("active");
        }
    }

    function formatearFecha(fecha) {
        if (!fecha) return "No disponible";

        const date = new Date(fecha);
        if (isNaN(date.getTime())) return fecha;

        return date.toLocaleString("es-DO", {
            dateStyle: "short",
            timeStyle: "short"
        });
    }

    function formatearHora(fecha) {
        if (!fecha) return "";

        const date = new Date(fecha);
        if (isNaN(date.getTime())) return "";

        return date.toLocaleTimeString("es-DO", {
            hour: "2-digit",
            minute: "2-digit"
        });
    }