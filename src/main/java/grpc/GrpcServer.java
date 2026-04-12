package grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    private Server server;

    public void iniciar() throws IOException {
        server = ServerBuilder.forPort(50051)
                .addService(new FormularioServiciosGrpc())
                .build()
                .start();

        System.out.println("Servidor gRPC iniciado en el puerto 50051");
    }

    public void detener() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination();
        }
    }
}