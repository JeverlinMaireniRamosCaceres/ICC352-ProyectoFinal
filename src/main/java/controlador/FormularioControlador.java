package controlador;

import entidades.Formulario;
import entidades.Posicion;
import entidades.Usuario;
import io.javalin.http.Context;
import servicios.FormularioServicio;

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
        List<Formulario> formularios = formularioServicio.listarTodos();
        ctx.json(formularios);
    }

    public void buscarPorId(Context ctx) {
        String id = ctx.pathParam("id");
        Formulario formulario = formularioServicio.buscarPorId(id);

        if (formulario == null) {
            ctx.status(404).json(Map.of("error", "Formulario no encontrado"));
            return;
        }

        ctx.json(formulario);
    }

    public void listarPorUsuario(Context ctx) {
        String usuarioId = ctx.pathParam("usuarioId");
        List<Formulario> formularios = formularioServicio.listarPorUsuarioId(usuarioId);
        ctx.json(formularios);
    }

    public void actualizar(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String nombre = (String) body.get("nombre");
            String apellido = (String) body.get("apellido");
            String sector = (String) body.get("sector");
            String nivelEscolar = (String) body.get("nivelEscolar");
            String usuarioId = (String) body.get("usuarioId");
            String foto = (String) body.get("foto");

            Map<String, Object> posicionMap = (Map<String, Object>) body.get("posicion");

            double latitud = 0;
            double longitud = 0;

            if (posicionMap != null) {
                latitud = Double.parseDouble(posicionMap.get("latitud").toString());
                longitud = Double.parseDouble(posicionMap.get("longitud").toString());
            }

            Posicion posicion = new Posicion(latitud, longitud);

            Formulario formularioActualizado = new Formulario(
                    nombre,
                    apellido,
                    sector,
                    nivelEscolar,
                    usuarioId,
                    posicion,
                    foto
            );

            boolean actualizado = formularioServicio.actualizar(id, formularioActualizado);

            if (!actualizado) {
                ctx.status(404).json(Map.of("error", "Formulario no encontrado"));
                return;
            }

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