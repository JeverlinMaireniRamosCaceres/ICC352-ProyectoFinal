package util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class MongoConexion {

    private static Datastore datastore;

    public static Datastore getDatastore() {
        if (datastore == null) {
            String uri = "mongodb+srv://jeverlin:123@proyectofinal.o0qjost.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoFinal";
            MongoClient mongoClient = MongoClients.create(uri);
            datastore = Morphia.createDatastore(mongoClient, "encuestas");
        }
        return datastore;
    }
}