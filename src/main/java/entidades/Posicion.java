package entidades;

import dev.morphia.annotations.Embedded;

@Embedded
public class Posicion {

    private double latitud;
    private double longitud;

    public Posicion() {
    }

    public Posicion(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}