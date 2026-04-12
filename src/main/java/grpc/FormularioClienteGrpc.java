package grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class FormularioClienteGrpc {

    private final ManagedChannel channel;
    private final FormularioGrpcServiceGrpc.FormularioGrpcServiceBlockingStub stub;

    public FormularioClienteGrpc() {
        this.channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        this.stub = FormularioGrpcServiceGrpc.newBlockingStub(channel);
    }

    public ListarFormulariosPorUsuarioResponse listarPorUsuario(String usuarioId) {
        ListarFormulariosPorUsuarioRequest request =
                ListarFormulariosPorUsuarioRequest.newBuilder()
                        .setUsuarioId(usuarioId)
                        .build();

        return stub.listarFormulariosPorUsuario(request);
    }

    public CrearFormularioResponse crearFormulario(
            String nombre,
            String apellido,
            String sector,
            String nivelEscolar,
            String usuarioId,
            double latitud,
            double longitud,
            String foto
    ) {
        PosicionMessage posicion = PosicionMessage.newBuilder()
                .setLatitud(latitud)
                .setLongitud(longitud)
                .build();

        CrearFormularioRequest request = CrearFormularioRequest.newBuilder()
                .setNombre(nombre != null ? nombre : "")
                .setApellido(apellido != null ? apellido : "")
                .setSector(sector != null ? sector : "")
                .setNivelEscolar(nivelEscolar != null ? nivelEscolar : "")
                .setUsuarioId(usuarioId != null ? usuarioId : "")
                .setPosicion(posicion)
                .setFoto(foto != null ? foto : "")
                .build();

        return stub.crearFormulario(request);
    }

    public void cerrar() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}