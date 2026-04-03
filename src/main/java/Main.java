import entidades.Formulario;
import entidades.Posicion;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import controlador.*;
import servicios.FormularioServicio;

import java.util.Map;

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
                ctx.render("templates/index.html", Map.of("usuarioLogueado", ctx.sessionAttribute("usuario")));
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

            // ----- WEBSOCKET -------
            config.routes.ws("/sync", ws -> {
                ws.onMessage(ctx -> {
                    try {
                        Map<String, Object> body = ctx.messageAsClass(Map.class);

                        String nombre = (String) body.get("nombre");
                        String apellido = (String) body.get("apellido");
                        String sector = (String) body.get("sector");
                        String nivelEscolar = (String) body.get("nivelEscolar");
                        String foto = (String) body.get("foto");
                        String idLocal = (String) body.get("idLocal");

                        Map<String, Object> posicionMap = (Map<String, Object>) body.get("posicion");
                        double latitud = 0;
                        double longitud = 0;
                        if (posicionMap != null) {
                            latitud = Double.parseDouble(posicionMap.get("latitud").toString());
                            longitud = Double.parseDouble(posicionMap.get("longitud").toString());
                        }

                        Posicion posicion = new Posicion(latitud, longitud);
                        Formulario formulario = new Formulario(nombre, apellido, sector, nivelEscolar, "", posicion, foto);
                        formulario.setIdLocal(idLocal);

                        FormularioServicio.getInstancia().guardar(formulario);

                        ctx.send("{\"estado\":\"OK\",\"idLocal\":\"" + idLocal + "\"}");

                    } catch (Exception e) {
                        ctx.send("{\"estado\":\"ERROR\",\"mensaje\":\"" + e.getMessage() + "\"}");
                    }
                });
            });

        }).start();

    }
}
