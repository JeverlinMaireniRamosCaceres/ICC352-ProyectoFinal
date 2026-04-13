package controlador;

import entidades.Formulario;
import entidades.Posicion;
import entidades.Usuario;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import servicios.FormularioServicio;
import servicios.UsuarioServicio;

import java.util.List;
import java.util.Map;

public class FormularioRestControlador {

    private final FormularioServicio formularioServicio;
    private final UsuarioServicio usuarioServicio;

    public FormularioRestControlador() {
        this.formularioServicio = FormularioServicio.getInstancia();
        this.usuarioServicio = UsuarioServicio.getInstancia();
    }

    public void vista(Context ctx) {
        ctx.render("templates/clienteRest.html");
    }

    public void crear(Context ctx) {
        try {
            Claims claims = ctx.attribute("jwt-claims");

            if (claims == null) {
                ctx.status(401).json(Map.of("error", "Token no disponible"));
                return;
            }

            String email = claims.getSubject();
            Usuario usuario = usuarioServicio.buscarPorEmail(email);

            if (usuario == null) {
                ctx.status(401).json(Map.of("error", "Usuario no autenticado"));
                return;
            }

            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String nombre = (String) body.get("nombre");
            String apellido = (String) body.get("apellido");
            String sector = (String) body.get("sector");
            String nivelEscolar = (String) body.get("nivelEscolar");
            String foto = (String) body.get("foto");

            Map<String, Object> posicionMap = (Map<String, Object>) body.get("posicion");

            double latitud = 0;
            double longitud = 0;

            if (posicionMap != null) {
                latitud = Double.parseDouble(posicionMap.get("latitud").toString());
                longitud = Double.parseDouble(posicionMap.get("longitud").toString());
            }

            Posicion posicion = new Posicion(latitud, longitud);

            Formulario formulario = new Formulario(
                    nombre,
                    apellido,
                    sector,
                    nivelEscolar,
                    usuario.getId(),
                    posicion,
                    foto
            );

            formularioServicio.guardar(formulario);

            ctx.status(201).json(Map.of(
                    "mensaje", "Formulario guardado correctamente",
                    "id", formulario.getId().toString()
            ));

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Error al guardar formulario: " + e.getMessage()));
        }
    }

    public void listarMisFormularios(Context ctx) {
        try {
            Claims claims = ctx.attribute("jwt-claims");

            if (claims == null) {
                ctx.status(401).json(Map.of("error", "Token no disponible"));
                return;
            }

            String email = claims.getSubject();
            List<Formulario> formularios = formularioServicio.listarPorUsuarioEmail(email);

            ctx.json(mapearFormularios(formularios));

        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Error al listar mis formularios: " + e.getMessage()));
        }
    }

    public void listarPorEmail(Context ctx) {
        try {
            Claims claims = ctx.attribute("jwt-claims");

            if (claims == null) {
                ctx.status(401).json(Map.of("error", "Token no disponible"));
                return;
            }

            String rol = claims.get("rol", String.class);

            if (!"ADMIN".equalsIgnoreCase(rol)) {
                ctx.status(403).json(Map.of("error", "No tiene permisos para consultar por correo"));
                return;
            }

            String email = ctx.pathParam("email");
            List<Formulario> formularios = formularioServicio.listarPorUsuarioEmail(email);

            ctx.json(mapearFormularios(formularios));

        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Error al listar formularios por correo: " + e.getMessage()));
        }
    }

    private List<Map<String, Object>> mapearFormularios(List<Formulario> formularios) {
        return formularios.stream()
                .map(formulario -> {
                    Map<String, Object> item = new java.util.LinkedHashMap<>();

                    item.put("id", formulario.getId() != null ? formulario.getId().toString() : "");
                    item.put("nombre", formulario.getNombre() != null ? formulario.getNombre() : "");
                    item.put("apellido", formulario.getApellido() != null ? formulario.getApellido() : "");
                    item.put("sector", formulario.getSector() != null ? formulario.getSector() : "");
                    item.put("nivelEscolar", formulario.getNivelEscolar() != null ? formulario.getNivelEscolar() : "");
                    item.put("usuarioId", formulario.getUsuarioId() != null ? formulario.getUsuarioId() : "");
                    item.put("sincronizado", formulario.isSincronizado());
                    item.put("fechaRegistro", formulario.getFechaRegistro() != null ? formulario.getFechaRegistro().toString() : "");
                    item.put("latitud", formulario.getPosicion() != null ? formulario.getPosicion().getLatitud() : 0);
                    item.put("longitud", formulario.getPosicion() != null ? formulario.getPosicion().getLongitud() : 0);
                    item.put("foto", formulario.getFoto() != null ? formulario.getFoto() : "");

                    return item;
                })
                .toList();
    }
}