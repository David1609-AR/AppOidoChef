package restaurante.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reserva {
    private int idReserva;
    private String nombre;
    private String telefono;
    private String email;
    private int numPersonas;
    private int idMesa;
    private LocalDateTime fechaReserva;
    private String estado;
    // Constructores
    public Reserva() {}

    public Reserva(String nombre, String telefono, String email, int numPersonas, int idMesa, LocalDateTime fechaReserva,String estado) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.numPersonas = numPersonas;
        this.idMesa = idMesa;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaReservaFormateada() {
        if (fechaReserva != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaReserva.format(formatter);
        }
        return "";
    }

}
