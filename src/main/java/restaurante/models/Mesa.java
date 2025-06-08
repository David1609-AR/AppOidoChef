package restaurante.models;

/**
 * Representa una mesa del restaurante.
 */
public class Mesa{
    private int id_mesa;
    private int numeroMesa;
    private double posicionX;
    private double posicionY;
    private boolean ocupada;
    private boolean bloqueada;

    public Mesa() {}

    public Mesa(int id_mesa, int numeroMesa, double posicionX, double posicionY, boolean ocupada, boolean bloqueada) {
        this.id_mesa = id_mesa;
        this.numeroMesa = numeroMesa;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.ocupada = ocupada;
        this.bloqueada = bloqueada;
    }

    // Getters y Setters
    public int getIdMesa() {
        return id_mesa;
    }

    public void setIdMesa(int idMesa) {
        this.id_mesa = idMesa;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public double getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(double posicionX) {
        this.posicionX = posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(double posicionY) {
        this.posicionY = posicionY;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }
    @Override
    public String toString() {
        return "Mesa " + numeroMesa;
    }

}