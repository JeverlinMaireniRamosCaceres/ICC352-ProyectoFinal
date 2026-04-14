package servicios;

import com.mongodb.client.MongoCollection;
import dev.morphia.Datastore;
import entidades.Usuario;
import org.bson.Document;
import util.MongoConexion;
import dev.morphia.query.filters.Filters;
import org.bson.types.ObjectId;
import java.util.List;

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
        if (email == null || email.isBlank()) {
            return null;
        }

        return datastore.find(Usuario.class)
                .filter(dev.morphia.query.filters.Filters.eq("email", email.trim()))
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

    public Usuario buscarPorId(String id) {
        try {
            return datastore.find(Usuario.class)
                    .filter(Filters.eq("_id", new ObjectId(id)))
                    .first();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Usuario> listarTodos() {
        return datastore.find(Usuario.class).iterator().toList();
    }

    public void actualizar(String id, Usuario datosNuevos) {
        Usuario usuario = buscarPorId(id);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuario.setNombre(datosNuevos.getNombre());
        usuario.setEmail(datosNuevos.getEmail());
        usuario.setRol(datosNuevos.getRol());
        if (datosNuevos.getContrasena() != null && !datosNuevos.getContrasena().isEmpty()) {
            usuario.setContrasena(datosNuevos.getContrasena());
        }
        datastore.save(usuario);
    }

    public void eliminar(String id) {
        Usuario usuario = buscarPorId(id);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        datastore.delete(usuario);
    }

}