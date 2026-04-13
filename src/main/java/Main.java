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

import grpc.GrpcServer;

public class Main {

    public static void main(String[] args) {

        try {
            GrpcServer grpcServer = new GrpcServer();
            grpcServer.iniciar();
        } catch (Exception e) {
            System.out.println("Error al iniciar servidor gRPC: " + e.getMessage());
        }

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

            // Obtener la conexion (si esta online, offline)
            config.routes.get("/estadoConexion", ctx -> ctx.json(Map.of("estado", "ok")));

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
                Usuario usuario = ctx.sessionAttribute("usuario");
                if (usuario == null) {
                    ctx.redirect("/");
                    return;
                }
                ctx.render("templates/index.html", Map.of("usuarioLogueado", usuario));
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
            // FormularioControlador formularioControlador = new FormularioControlador();
            FormularioRestControlador formularioRestControlador = new FormularioRestControlador();
            ClienteGrpcControlador clienteGrpcControlador = new ClienteGrpcControlador();

            // LOGIN
            config.routes.post("/auth/login", authControlador::login);
            config.routes.post("/auth/registro", authControlador::registro);

            // FORMULARIO

            config.routes.get("/formularios", ctx -> {
                ctx.render("templates/formularios.html"); });


            // Dirigir a formularios sincronizados y mostrarlos
            config.routes.get("/formularios/sincronizados", ctx -> {

                Usuario usuario = ctx.sessionAttribute("usuario");

                if (usuario == null) {
                    ctx.status(401).json(Map.of("error", "No autenticado"));
                    return;
                }

                var formularios = FormularioServicio.getInstancia().listarTodos();

                UsuarioServicio usuarioServicio = UsuarioServicio.getInstancia();

                var respuesta = formularios.stream().map(formulario -> {
                    Map<String, Object> item = new java.util.LinkedHashMap<>();

                    String usuarioNombre = "No disponible";
                    if (formulario.getUsuarioId() != null) {
                        Usuario u = usuarioServicio.buscarPorId(formulario.getUsuarioId());
                        if (u != null) usuarioNombre = u.getNombre();
                    }

                    item.put("id", formulario.getId().toString());
                    item.put("nombre", formulario.getNombre());
                    item.put("apellido", formulario.getApellido());
                    item.put("sector", formulario.getSector());
                    item.put("nivelEscolar", formulario.getNivelEscolar());
                    item.put("usuarioNombre", usuarioNombre);
                    item.put("foto", formulario.getFoto());
                    item.put("fechaRegistro", formulario.getFechaRegistro().toString());
                    item.put("latitud", formulario.getPosicion().getLatitud());
                    item.put("longitud", formulario.getPosicion().getLongitud());

                    return item;
                }).toList();

                ctx.json(respuesta);
            });

            // Dirigir a sincronizar y mostrar los formularios pendientes de sincronizar
            config.routes.post("/formularios/sincronizar", ctx -> {
                try {
                    Map<String, Object> body = ctx.bodyAsClass(Map.class);

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

                    Usuario usuario = ctx.sessionAttribute("usuario");
                    String usuarioId = "";

                    if (usuario != null && usuario.getId() != null) {
                        usuarioId = usuario.getId().toString();
                    }

                    Formulario formulario = new Formulario(
                            nombre,
                            apellido,
                            sector,
                            nivelEscolar,
                            usuarioId,
                            posicion,
                            foto
                    );

                    formulario.setIdLocal(idLocal);

                    FormularioServicio.getInstancia().guardar(formulario);

                    ctx.status(201).json(Map.of(
                            "mensaje", "Formulario sincronizado correctamente",
                            "idLocal", idLocal
                    ));

                } catch (Exception e) {
                    ctx.status(400).json(Map.of(
                            "error", "Error al sincronizar formulario: " + e.getMessage()
                    ));
                }
            });

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

            // MAPA
            // Obtener datos para el mapa
            config.routes.get("/mapa/datos", ctx -> {
                var formularios = FormularioServicio.getInstancia().listarTodos();
                UsuarioServicio usuarioServicio = UsuarioServicio.getInstancia();

                var respuesta = formularios.stream().map(formulario -> {
                    Map<String, Object> item = new java.util.LinkedHashMap<>();

                    String usuarioNombre = "No disponible";
                    if (formulario.getUsuarioId() != null && !formulario.getUsuarioId().isEmpty()) {
                        Usuario usuario = usuarioServicio.buscarPorId(formulario.getUsuarioId());
                        if (usuario != null) {
                            usuarioNombre = usuario.getNombre();
                        }
                    }

                    item.put("id", formulario.getId() != null ? formulario.getId().toString() : "");
                    item.put("nombre", formulario.getNombre() != null ? formulario.getNombre() : "");
                    item.put("apellido", formulario.getApellido() != null ? formulario.getApellido() : "");
                    item.put("sector", formulario.getSector() != null ? formulario.getSector() : "");
                    item.put("nivelEscolar", formulario.getNivelEscolar() != null ? formulario.getNivelEscolar() : "");
                    item.put("usuarioNombre", usuarioNombre);
                    item.put("foto", formulario.getFoto() != null ? formulario.getFoto() : "");
                    item.put("fechaRegistro", formulario.getFechaRegistro() != null ? formulario.getFechaRegistro().toString() : "");
                    item.put("latitud", formulario.getPosicion() != null ? formulario.getPosicion().getLatitud() : 0);
                    item.put("longitud", formulario.getPosicion() != null ? formulario.getPosicion().getLongitud() : 0);

                    return item;
                }).toList();

                ctx.json(respuesta);
            });

            // ----- WEBSOCKET -------
            config.routes.ws("/sync", ws -> {
                ws.onConnect(ctx -> ctx.session.setMaxTextMessageSize(10 * 1024 * 1024));
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

                String path = ctx.path();

                if (path.equals("/api/auth/login")) {
                    return;
                }

                String header = ctx.header("Authorization");
                if (header == null || !header.startsWith("Bearer ")) {
                    throw new io.javalin.http.UnauthorizedResponse("Se requiere token");
                }

                String token = header.replace("Bearer ", "").trim();

                try {
                    var claims = JWTUtil.validarToken(token);
                    ctx.attribute("jwt-claims", claims);
                } catch (Exception e) {
                    throw new io.javalin.http.ForbiddenResponse("El token no es valido o expiro");
                }
            });

            // API REST

            config.routes.post("/api/auth/login", authControlador::login);

            config.routes.get("/api/formularios/losDelUsuario", formularioRestControlador::listarMisFormularios);

            config.routes.get("/api/formularios/email/{email}", formularioRestControlador::listarPorEmail);

            config.routes.post("/api/formularios", formularioRestControlador::crear);

            // CLIENTE REST
            config.routes.get("/clienteRest", formularioRestControlador::vista);

            //GRPC
            config.routes.get("/clienteGrpc", clienteGrpcControlador::vista);
            config.routes.get("/grpc/formularios", clienteGrpcControlador::listarPorUsuario);
            config.routes.post("/grpc/formularios", clienteGrpcControlador::crear);


        }).start();

    }
}
