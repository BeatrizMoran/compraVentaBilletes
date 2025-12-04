package models;

import java.io.Serializable;

public class Billete implements Serializable {
    private String origen;
    private String destino;
    private double precio;
    private int plazasDisponibles;

    public Billete(String origen, String destino, double precio, int plazasDisponibles) {
        this.origen = origen;
        this.destino = destino;
        this.precio = precio;
        this.plazasDisponibles = plazasDisponibles;
    }

    // Getters y Setters
    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public double getPrecio() {
        return precio;
    }

    public int getPlazasDisponibles() {
        return plazasDisponibles;
    }

    public void setPlazasDisponibles(int plazasDisponibles) {
        this.plazasDisponibles = plazasDisponibles;
    }

    @Override
    public String toString() {
        return "models.Billete{" +
                "origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", precio=" + precio +
                ", plazasDisponibles=" + plazasDisponibles +
                '}';
    }
}
