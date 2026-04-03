package entidades;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;

@Entity("formularios")
public class Formulario {

    @Id
    ObjectId id;
    String idLocal;
    String nombre;
    String apellido;
    String sector;
    String nivelEscolar;
    String usuarioId;
    Posicion posicion;
    String foto;
    LocalDateTime fechaRegistro;
    boolean sincronizado;

    public Formulario() {
    }

    public Formulario(String nombre, String apellido, String sector, String nivelEscolar, String usuarioId, Posicion posicion, String foto) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.sector = sector;
        this.nivelEscolar = nivelEscolar;
        this.usuarioId = usuarioId;
        this.posicion = posicion;
        this.foto = foto;
        this.fechaRegistro = LocalDateTime.now();
        this.sincronizado = false;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getNivelEscolar() {
        return nivelEscolar;
    }

    public void setNivelEscolar(String nivelEscolar) {
        this.nivelEscolar = nivelEscolar;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }

    public String getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(String idLocal) {
        this.idLocal = idLocal;
    }
}
