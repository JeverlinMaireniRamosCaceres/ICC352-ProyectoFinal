package servicios;

import com.mongodb.client.MongoCollection;
import dev.morphia.Datastore;
import entidades.Usuario;
import org.bson.Document;
import util.MongoConexion;

public class UsuarioServicio {

    private static UsuarioServicio instancia;
    private final Datastore datastore;

    private UsuarioServicio() {
        datastore = MongoConexion.getDatastore();
    }

    public static UsuarioServicio getInstancia() {
        if (instancia == null) {
            instancia = new UsuarioServicio();
        }
        return instancia;
    }

    public Usuario buscarPorEmail(String email) {
        return datastore.find(Usuario.class)
                .filter(dev.morphia.query.filters.Filters.eq("email", email))
                .first();
    }

    public boolean existeEmail(String email) {
        return buscarPorEmail(email) != null;
    }

    public void guardar(Usuario usuario) {
        datastore.save(usuario);
    }

    public Usuario autenticar(String email, String contrasena) {
        Usuario usuario = buscarPorEmail(email);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        if (!usuario.getContrasena().equals(contrasena)) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return usuario;
    }
}