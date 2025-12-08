package models;

import java.io.Serializable;
import java.util.Objects;

public class Billete implements Serializable {
    private int id;
    private String origen;
    private String destino;
    private double precio;
    private int plazasDisponibles;

    // para avisar cuando otro cliente esté comprando
    private boolean enCompra = false;

    public Billete(int id, String origen, String destino, double precio, int plazasDisponibles) {
        this.id = id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEnCompra() {
        return enCompra;
    }

    public void setEnCompra(boolean enCompra) {
        this.enCompra = enCompra;
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
