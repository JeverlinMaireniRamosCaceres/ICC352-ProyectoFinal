package grpc;

import entidades.Formulario;
import entidades.Posicion;
import io.grpc.stub.StreamObserver;
import servicios.FormularioServicio;

import java.util.List;

public class FormularioServiciosGrpc extends FormularioGrpcServiceGrpc.FormularioGrpcServiceImplBase {

    private final FormularioServicio formularioServicio;

    public FormularioServiciosGrpc() {
        this.formularioServicio = FormularioServicio.getInstancia();
    }

    @Override
    public void crearFormulario(CrearFormularioRequest request,
                                StreamObserver<CrearFormularioResponse> responseObserver) {
        try {
            Posicion posicion = new Posicion(
                    request.getPosicion().getLatitud(),
                    request.getPosicion().getLongitud()
            );

            Formulario formulario = new Formulario(
                    request.getNombre(),
                    request.getApellido(),
                    request.getSector(),
                    request.getNivelEscolar(),
                    request.getUsuarioId(),
                    posicion,
                    request.getFoto()
            );
            formularioServicio.guardar(formulario);
            formulario.setSincronizado(true);

            CrearFormularioResponse response = CrearFormularioResponse.newBuilder()
                    .setMensaje("Formulario creado correctamente")
                    .setId(formulario.getId() != null ? formulario.getId().toString() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void listarFormulariosPorUsuario(ListarFormulariosPorUsuarioRequest request,
                                            StreamObserver<ListarFormulariosPorUsuarioResponse> responseObserver) {
        try {
            List<Formulario> formularios = formularioServicio.listarPorUsuarioId(request.getUsuarioId());

            ListarFormulariosPorUsuarioResponse.Builder responseBuilder =
                    ListarFormulariosPorUsuarioResponse.newBuilder();

            for (Formulario formulario : formularios) {
                PosicionMessage posicionMessage = PosicionMessage.newBuilder()
                        .setLatitud(formulario.getPosicion() != null ? formulario.getPosicion().getLatitud() : 0)
                        .setLongitud(formulario.getPosicion() != null ? formulario.getPosicion().getLongitud() : 0)
                        .build();

                FormularioMessage formularioMessage = FormularioMessage.newBuilder()
                        .setId(formulario.getId() != null ? formulario.getId().toString() : "")
                        .setNombre(formulario.getNombre() != null ? formulario.getNombre() : "")
                        .setApellido(formulario.getApellido() != null ? formulario.getApellido() : "")
                        .setSector(formulario.getSector() != null ? formulario.getSector() : "")
                        .setNivelEscolar(formulario.getNivelEscolar() != null ? formulario.getNivelEscolar() : "")
                        .setUsuarioId(formulario.getUsuarioId() != null ? formulario.getUsuarioId() : "")
                        .setPosicion(posicionMessage)
                        .setFoto(formulario.getFoto() != null ? formulario.getFoto() : "")
                        .setFechaRegistro(formulario.getFechaRegistro() != null ? formulario.getFechaRegistro().toString() : "")
                        .setSincronizado(formulario.isSincronizado())
                        .build();

                responseBuilder.addFormularios(formularioMessage);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void listarTodosFormularios(ListarTodosFormulariosRequest request,
                                       StreamObserver<ListarTodosFormulariosResponse> responseObserver) {
        try {
            List<Formulario> formularios = formularioServicio.listarTodos();

            ListarTodosFormulariosResponse.Builder responseBuilder =
                    ListarTodosFormulariosResponse.newBuilder();

            for (Formulario formulario : formularios) {
                PosicionMessage posicionMessage = PosicionMessage.newBuilder()
                        .setLatitud(formulario.getPosicion() != null ? formulario.getPosicion().getLatitud() : 0)
                        .setLongitud(formulario.getPosicion() != null ? formulario.getPosicion().getLongitud() : 0)
                        .build();

                FormularioMessage formularioMessage = FormularioMessage.newBuilder()
                        .setId(formulario.getId() != null ? formulario.getId().toString() : "")
                        .setNombre(formulario.getNombre() != null ? formulario.getNombre() : "")
                        .setApellido(formulario.getApellido() != null ? formulario.getApellido() : "")
                        .setSector(formulario.getSector() != null ? formulario.getSector() : "")
                        .setNivelEscolar(formulario.getNivelEscolar() != null ? formulario.getNivelEscolar() : "")
                        .setUsuarioId(formulario.getUsuarioId() != null ? formulario.getUsuarioId() : "")
                        .setPosicion(posicionMessage)
                        .setFoto(formulario.getFoto() != null ? formulario.getFoto() : "")
                        .setFechaRegistro(formulario.getFechaRegistro() != null ? formulario.getFechaRegistro().toString() : "")
                        .setSincronizado(formulario.isSincronizado())
                        .build();

                responseBuilder.addFormularios(formularioMessage);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}