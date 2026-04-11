package controlador;

import entidades.Usuario;
import io.javalin.http.Context;
import servicios.UsuarioServicio;
import util.JWTUtil;

import java.util.Map;

public class AuthControlador {

    private final UsuarioServicio usuarioServicio;

    public AuthControlador() {
        this.usuarioServicio = UsuarioServicio.getInstancia();
    }

    public void login(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            Usuario usuario = usuarioServicio.autenticar(body.get("email"), body.get("contrasena"));

            String token = JWTUtil.generarToken(usuario.getEmail(), usuario.getRol());

            ctx.sessionAttribute("usuario", usuario);
            ctx.json(Map.of(
                    "mensaje", "Login exitoso",
                    "nombre", usuario.getNombre(),
                    "rol", usuario.getRol(),
                    "token", token
            ));
        } catch (RuntimeException e) {
            ctx.status(401).json(Map.of("error", e.getMessage()));
        }
    }

    public void registro(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            Usuario usuario = new Usuario(
                    body.get("nombre"),
                    body.get("email"),
                    body.get("contrasena"),
                    body.get("rol")
            );
            usuarioServicio.guardar(usuario);
            ctx.status(201).json(Map.of("mensaje", "Usuario creado correctamente"));
        } catch (RuntimeException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }
}