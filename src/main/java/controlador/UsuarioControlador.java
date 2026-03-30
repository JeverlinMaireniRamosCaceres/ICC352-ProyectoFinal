package controlador;

import entidades.Usuario;
import io.javalin.http.Context;
import servicios.UsuarioServicio;

import java.util.List;
import java.util.Map;

public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador() {
        this.usuarioServicio = UsuarioServicio.getInstancia();
    }

    // Listar todos los usuarios
    public void listar(Context ctx) {
        List<Usuario> usuarios = usuarioServicio.listarTodos();
        ctx.json(usuarios);
    }

    // Buscar por id
    public void buscarPorId(Context ctx) {
        String id = ctx.pathParam("id");
        Usuario usuario = usuarioServicio.buscarPorId(id);
        if (usuario == null) {
            ctx.status(404).json(Map.of("error", "Usuario no encontrado"));
        } else {
            ctx.json(usuario);
        }
    }

    // Crear usuario
    public void crear(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            if (usuarioServicio.existeEmail(body.get("email"))) {
                ctx.status(400).json(Map.of("error", "El email ya está registrado"));
                return;
            }
            Usuario usuario = new Usuario(
                    body.get("nombre"),
                    body.get("email"),
                    body.get("contrasena"),
                    body.get("rol")
            );
            usuarioServicio.guardar(usuario);
            ctx.status(201).json(Map.of("mensaje", "Usuario creado correctamente"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    // Actualizar usuario
    public void actualizar(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            Usuario datosNuevos = new Usuario(
                    body.get("nombre"),
                    body.get("email"),
                    body.get("contrasena"),
                    body.get("rol")
            );
            usuarioServicio.actualizar(id, datosNuevos);
            ctx.json(Map.of("mensaje", "Usuario actualizado correctamente"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar usuario
    public void eliminar(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            usuarioServicio.eliminar(id);
            ctx.json(Map.of("mensaje", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
}