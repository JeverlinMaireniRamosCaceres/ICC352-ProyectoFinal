package servicios;


import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import entidades.Formulario;
import util.MongoConexion;
import org.bson.types.ObjectId;

import java.util.List;

public class FormularioServicio {

    private static FormularioServicio instancia;
    private final Datastore datastore;

    private FormularioServicio() {
        datastore = MongoConexion.getDatastore();
    }

    public static FormularioServicio getInstancia() {
        if (instancia == null) {
            instancia = new FormularioServicio();
        }
        return instancia;
    }

    public void guardar(Formulario formulario) {
        datastore.save(formulario);
    }

    public List<Formulario> listarTodos() {
        return datastore.find(Formulario.class).iterator().toList();
    }

    public Formulario buscarPorId(String id) {
        return datastore.find(Formulario.class)
                .filter(Filters.eq("_id", new ObjectId(id)))
                .first();
    }

    public List<Formulario> listarPorUsuarioId(String usuarioId) {
        return datastore.find(Formulario.class)
                .filter(Filters.eq("usuarioId", usuarioId))
                .iterator()
                .toList();
    }

    public boolean actualizar(String id, Formulario datosActualizados) {
        Formulario formularioExistente = buscarPorId(id);

        if (formularioExistente == null) {
            return false;
        }

        formularioExistente.setNombre(datosActualizados.getNombre());
        formularioExistente.setApellido(datosActualizados.getApellido());
        formularioExistente.setSector(datosActualizados.getSector());
        formularioExistente.setNivelEscolar(datosActualizados.getNivelEscolar());
        formularioExistente.setUsuarioId(datosActualizados.getUsuarioId());
        formularioExistente.setPosicion(datosActualizados.getPosicion());
        formularioExistente.setFoto(datosActualizados.getFoto());
        formularioExistente.setSincronizado(datosActualizados.isSincronizado());

        datastore.save(formularioExistente);
        return true;
    }

    public boolean eliminar(String id) {
        Formulario formulario = buscarPorId(id);

        if (formulario == null) {
            return false;
        }

        datastore.find(Formulario.class)
                .filter(Filters.eq("_id", new ObjectId(id)))
                .delete();

        return true;
    }

    public List<Formulario> listarPorUsuarioEmail(String email) {
        UsuarioServicio usuarioServicio = UsuarioServicio.getInstancia();
        var usuario = usuarioServicio.buscarPorEmail(email);

        if (usuario == null || usuario.getId() == null) {
            return List.of();
        }

        return listarPorUsuarioId(usuario.getId());
    }

}