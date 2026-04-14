package util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class MongoConexion {

    private static Datastore datastore;

    public static Datastore getDatastore() {
        if (datastore == null) {
            String uri = System.getenv("MONGO_URI");
            if (uri == null || uri.isBlank()) {
                throw new RuntimeException("La variable MONGO_URI no está definida");
            }
            MongoClient mongoClient = MongoClients.create(uri);
            datastore = Morphia.createDatastore(mongoClient, "encuestas");
        }
        return datastore;
    }
}