package controlador;

import entidades.Formulario;
import entidades.Posicion;
import entidades.Usuario;
import io.javalin.http.Context;
import servicios.FormularioServicio;
import servicios.UsuarioServicio;

import java.util.List;
import java.util.Map;

public class FormularioControlador {

    private final FormularioServicio formularioServicio;

    public FormularioControlador() {
        this.formularioServicio = FormularioServicio.getInstancia();
    }

    public void crear(Context ctx) {
        try {
            Usuario usuario = ctx.sessionAttribute("usuario");

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
                    usuario.getId().toString(),
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

    public void listarTodos(Context ctx) {
        try {
            List<Formulario> formularios = formularioServicio.listarTodos();
            UsuarioServicio usuarioServicio = UsuarioServicio.getInstancia();

            List<Map<String, Object>> respuesta = formularios.stream()
                    .map(formulario -> {
                        Map<String, Object> item = new java.util.LinkedHashMap<>();

                        Usuario usuario = null;
                        String usuarioNombre = "No disponible";

                        if (formulario.getUsuarioId() != null &&  !formulario.getUsuarioId().isEmpty()) {
                            usuario = usuarioServicio.buscarPorId(formulario.getUsuarioId());
                            if (usuario != null) {
                                usuarioNombre = usuario.getNombre();
                            }
                        }

                        item.put("id", formulario.getId() != null ? formulario.getId().toString() : "");
                        item.put("nombre", formulario.getNombre() != null ? formulario.getNombre() : "");
                        item.put("apellido", formulario.getApellido() != null ? formulario.getApellido() : "");
                        item.put("sector", formulario.getSector() != null ? formulario.getSector() : "");
                        item.put("nivelEscolar", formulario.getNivelEscolar() != null ? formulario.getNivelEscolar() : "");
                        item.put("usuarioId", formulario.getUsuarioId() != null ? formulario.getUsuarioId() : "");
                        item.put("usuarioNombre", usuarioNombre);
                        item.put("foto", formulario.getFoto()  != null ? formulario.getFoto() : "");
                        item.put("sincronizado", formulario.isSincronizado());
                        item.put("fechaRegistro", formulario.getFechaRegistro() != null ? formulario.getFechaRegistro().toString() : "");
                        item.put("latitud", formulario.getPosicion() != null ? formulario.getPosicion().getLatitud() : 0);
                        item.put("longitud", formulario.getPosicion() != null ? formulario.getPosicion().getLongitud() : 0);

                        return item;
                    })
                    .toList();

            ctx.json(respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Error al listar formularios: " + e.getMessage()));
        }
    }

    public void buscarPorId(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Formulario formulario = formularioServicio.buscarPorId(id);

            if (formulario == null) {
                ctx.status(404).json(Map.of("error", "Formulario no encontrado"));
                return;
            }

            Map<String, Object> respuesta = new java.util.LinkedHashMap<>();
            respuesta.put("id", formulario.getId() != null ? formulario.getId().toString() : "");
            respuesta.put("nombre", formulario.getNombre() != null ? formulario.getNombre() : "");
            respuesta.put("apellido", formulario.getApellido() != null ? formulario.getApellido() : "");
            respuesta.put("sector", formulario.getSector() != null ? formulario.getSector() : "");
            respuesta.put("nivelEscolar", formulario.getNivelEscolar() != null ? formulario.getNivelEscolar() : "");
            respuesta.put("usuarioId", formulario.getUsuarioId() != null ? formulario.getUsuarioId() : "");
            respuesta.put("foto", formulario.getFoto() != null ? formulario.getFoto() : "");
            respuesta.put("sincronizado", formulario.isSincronizado());
            respuesta.put("fechaRegistro", formulario.getFechaRegistro() != null ? formulario.getFechaRegistro().toString() : "");
            respuesta.put("latitud", formulario.getPosicion() != null ? formulario.getPosicion().getLatitud() : 0);
            respuesta.put("longitud", formulario.getPosicion() != null ? formulario.getPosicion().getLongitud() : 0);

            ctx.json(respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Error al buscar formulario: " + e.getMessage()));
        }
    }

    public void listarPorUsuario(Context ctx) {
        try {
            String usuarioId = ctx.pathParam("usuarioId");
            List<Formulario> formularios = formularioServicio.listarPorUsuarioId(usuarioId);

            List<Map<String, Object>> respuesta = formularios.stream()
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

                        return item;
                    })
                    .toList();

            ctx.json(respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Error al listar formularios por usuario: " + e.getMessage()));
        }
    }

    public void actualizar(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Formulario formularioExistente = formularioServicio.buscarPorId(id);

            if (formularioExistente == null) {
                ctx.status(404).json(Map.of("error", "Formulario no encontrado"));
                return;
            }

            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String nombre = (String) body.get("nombre");
            String apellido = (String) body.get("apellido");
            String sector = (String) body.get("sector");
            String nivelEscolar = (String) body.get("nivelEscolar");

            formularioExistente.setNombre(nombre);
            formularioExistente.setApellido(apellido);
            formularioExistente.setSector(sector);
            formularioExistente.setNivelEscolar(nivelEscolar);

            formularioServicio.guardar(formularioExistente);

            ctx.json(Map.of("mensaje", "Formulario actualizado correctamente"));

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Error al actualizar formulario: " + e.getMessage()));
        }
    }

    public void eliminar(Context ctx) {
        String id = ctx.pathParam("id");
        boolean eliminado = formularioServicio.eliminar(id);

        if (!eliminado) {
            ctx.status(404).json(Map.of("error", "Formulario no encontrado"));
            return;
        }

        ctx.json(Map.of("mensaje", "Formulario eliminado correctamente"));
    }
}