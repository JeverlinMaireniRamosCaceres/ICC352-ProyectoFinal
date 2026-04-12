package controlador;

import entidades.Usuario;
import grpc.CrearFormularioResponse;
import grpc.FormularioClienteGrpc;
import grpc.FormularioMessage;
import grpc.ListarFormulariosPorUsuarioResponse;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClienteGrpcControlador {

    public void vista(Context ctx) {
        ctx.render("templates/clienteGrpc.html");
    }

    public void listarPorUsuario(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");

        if (usuario == null) {
            ctx.status(401).json(Map.of("error", "Usuario no autenticado"));
            return;
        }

        FormularioClienteGrpc cliente = new FormularioClienteGrpc();

        try {
            ListarFormulariosPorUsuarioResponse response =
                    cliente.listarPorUsuario(usuario.getId().toString());

            List<Map<String, Object>> resultado = new ArrayList<>();

            for (FormularioMessage formulario : response.getFormulariosList()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", formulario.getId());
                item.put("nombre", formulario.getNombre());
                item.put("apellido", formulario.getApellido());
                item.put("sector", formulario.getSector());
                item.put("nivelEscolar", formulario.getNivelEscolar());
                item.put("usuarioId", formulario.getUsuarioId());
                item.put("foto", formulario.getFoto());
                item.put("fechaRegistro", formulario.getFechaRegistro());
                item.put("sincronizado", formulario.getSincronizado());
                item.put("latitud", formulario.getPosicion().getLatitud());
                item.put("longitud", formulario.getPosicion().getLongitud());
                resultado.add(item);
            }

            ctx.json(resultado);

        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Error al listar por gRPC: " + e.getMessage()));
        } finally {
            cliente.cerrar();
        }
    }

    @SuppressWarnings("unchecked")
    public void crear(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuario");

        if (usuario == null) {
            ctx.status(401).json(Map.of("error", "Usuario no autenticado"));
            return;
        }

        FormularioClienteGrpc cliente = new FormularioClienteGrpc();

        try {
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

            CrearFormularioResponse response = cliente.crearFormulario(
                    nombre,
                    apellido,
                    sector,
                    nivelEscolar,
                    usuario.getId().toString(),
                    latitud,
                    longitud,
                    foto
            );

            ctx.status(201).json(Map.of(
                    "mensaje", response.getMensaje(),
                    "id", response.getId()
            ));

        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Error al crear por gRPC: " + e.getMessage()));
        } finally {
            cliente.cerrar();
        }
    }
}