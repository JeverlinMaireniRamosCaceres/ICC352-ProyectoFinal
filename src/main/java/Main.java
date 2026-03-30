import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import controlador.*;

public class Main {

    public static void main(String[] args) {

        var app = Javalin.create(config -> {

            // HTTP configuration
            config.http.asyncTimeout = 10_000L;
            config.http.generateEtags = true;

            // Router configuration
            config.router.ignoreTrailingSlashes = true;
            config.router.caseInsensitiveRoutes = true;

            // Static files
            config.staticFiles.add("/publico", Location.CLASSPATH);

            // renderer thymeleaf
            config.fileRenderer(new JavalinThymeleaf());

            // Jetty configuration
            config.jetty.port = 7000;

            // ---------- ENDPOINTS ---------

            // Redireccion registro
            config.routes.get("/formulario", ctx -> {
                ctx.render("templates/formulario.html");
            });

            // Redireccion login
            config.routes.get("/", ctx -> {
                ctx.render("templates/login.html");
            });

            // Redireccion usuarios
            config.routes.get("/usuarios", ctx -> {
                ctx.render("templates/usuarios.html");
            });

            // Redireccion index
            config.routes.get("/index", ctx -> {
                ctx.render("templates/index.html");
            });

            // Redireccion mapa
            config.routes.get("/mapa", ctx -> {
                ctx.render("templates/mapa.html");
            });

            // Cerrar sesion
            config.routes.get("/logout", ctx -> {
                ctx.req().getSession().invalidate();
                ctx.redirect("/");
            });

            // ------

            // CONTROLADORES
            AuthControlador authControlador = new AuthControlador();
            FormularioControlador formularioControlador = new FormularioControlador();
            UsuarioControlador usuarioControlador = new UsuarioControlador();

            // LOGIN
            config.routes.post("/auth/login", authControlador::login);
            config.routes.post("/auth/registro", authControlador::registro);

            // FORMULARIO

            config.routes.get("/formularios", ctx -> {
                ctx.render("templates/formularios.html"); });

            config.routes.post("/api/formularios", formularioControlador::crear);

            config.routes.get("/api/formularios", formularioControlador::listarTodos);
            config.routes.get("/api/formularios/{id}", formularioControlador::buscarPorId);
            config.routes.get("/api/formularios/usuario/{usuarioId}", formularioControlador::listarPorUsuario);
            config.routes.put("/api/formularios/{id}", formularioControlador::actualizar);
            config.routes.delete("/api/formularios/{id}", formularioControlador::eliminar);


            // CRUD USUARIO

            config.routes.get("/api/usuarios", usuarioControlador::listar);
            config.routes.get("/api/usuarios/{id}", usuarioControlador::buscarPorId);
            config.routes.post("/api/usuarios", usuarioControlador::crear);
            config.routes.put("/api/usuarios/{id}", usuarioControlador::actualizar);
            config.routes.delete("/api/usuarios/{id}", usuarioControlador::eliminar);

        }).start();

    }
}
