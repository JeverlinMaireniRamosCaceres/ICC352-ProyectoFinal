import entidades.Formulario;
import entidades.Posicion;
import entidades.Usuario;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import controlador.*;
import servicios.FormularioServicio;
import servicios.UsuarioServicio;
import util.JWTUtil;

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

            // Obtener usuario
            config.routes.get("/usuarios", ctx -> {
                Usuario usuario = ctx.sessionAttribute("usuario");

                if (usuario == null || !usuario.getRol().equals("ADMIN")) {
                    ctx.redirect("/");
                    return;
                }

                var listaUsuarios = UsuarioServicio.getInstancia().listarTodos();

                ctx.attribute("usuarios", listaUsuarios);
                ctx.render("templates/usuarios.html");
            });

            // Crear usuario
            config.routes.post("/usuarios/crear", ctx -> {
                Usuario admin = ctx.sessionAttribute("usuario");

                if (admin == null || !admin.getRol().equals("ADMIN")) {
                    ctx.redirect("/");
                    return;
                }

                Usuario usuario = new Usuario(
                        ctx.formParam("nombre"),
                        ctx.formParam("email"),
                        ctx.formParam("contrasena"),
                        ctx.formParam("rol")
                );

                UsuarioServicio.getInstancia().guardar(usuario);

                ctx.redirect("/usuarios");
            });

            // Redirigir a editar usuario
            config.routes.get("/usuarios/editar/{id}", ctx -> {
                Usuario admin = ctx.sessionAttribute("usuario");

                if (admin == null || !admin.getRol().equals("ADMIN")) {
                    ctx.redirect("/");
                    return;
                }

                String id = ctx.pathParam("id");

                Usuario usuarioEditar = UsuarioServicio.getInstancia().buscarPorId(id);
                var listaUsuarios = UsuarioServicio.getInstancia().listarTodos();

                ctx.attribute("usuarios", listaUsuarios);
                ctx.attribute("usuarioEditar", usuarioEditar);

                ctx.render("templates/usuarios.html");
            });

            // Editar usuario
            config.routes.post("/usuarios/editar/{id}", ctx -> {
                Usuario admin = ctx.sessionAttribute("usuario");

                if (admin == null || !admin.getRol().equals("ADMIN")) {
                    ctx.redirect("/");
                    return;
                }

                String id = ctx.pathParam("id");

                Usuario datosNuevos = new Usuario(
                        ctx.formParam("nombre"),
                        ctx.formParam("email"),
                        ctx.formParam("contrasena"),
                        ctx.formParam("rol")
                );

                UsuarioServicio.getInstancia().actualizar(id, datosNuevos);

                ctx.redirect("/usuarios");
            });

            // Eliminar usuario
            config.routes.post("/usuarios/eliminar/{id}", ctx -> {
                Usuario admin = ctx.sessionAttribute("usuario");

                if (admin == null || !admin.getRol().equals("ADMIN")) {
                    ctx.redirect("/");
                    return;
                }

                String id = ctx.pathParam("id");

                UsuarioServicio.getInstancia().eliminar(id);

                ctx.redirect("/usuarios");
            });

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

            // ---------------------- JWT ---------------------

            // Filtro JWT para proteger los endpoints
            config.routes.before("/api/*", ctx -> {
                if (ctx.method().toString().equals("OPTIONS")) return;

                String header = ctx.header("Authorization");
                if (header == null || !header.startsWith("Bearer ")) {
                    throw new io.javalin.http.UnauthorizedResponse("Se requiere token");
                }

                String token = header.replace("Bearer ", "").trim();
                if (!JWTUtil.esValido(token)) {
                    throw new io.javalin.http.ForbiddenResponse("El token no es valido o expiro");
                }
            });


        }).start();

    }
}
