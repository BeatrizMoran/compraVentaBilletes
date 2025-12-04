package models;

import java.io.Serializable;
import java.util.UUID;

public class Transaccion implements Serializable {
    private String id;
    private Billete billete;
    private String estado; // "EXITOSA", "PENDIENTE", "RECHAZADA"

    public Transaccion(Billete billete) {
        this.id = UUID.randomUUID().toString();
        this.billete = billete;
        this.estado = "PENDIENTE";
    }

    // Getters y setters
    public String getId() { return id; }
    public Billete getBillete() { return billete; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return "Transaccion{" +
                "id='" + id + '\'' +
                ", billete=" + billete +
                ", estado='" + estado + '\'' +
                '}';
    }
}
