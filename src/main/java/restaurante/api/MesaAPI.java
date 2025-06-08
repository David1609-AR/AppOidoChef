package restaurante.api;

import restaurante.models.Mesa;

public class MesaAPI {
    private int id;
    private int numeroMesa;
    private boolean bloqueada;
    private boolean ocupada;

    public MesaAPI() {}

    public MesaAPI(Mesa mesa) {
        this.id = mesa.getIdMesa();
        this.numeroMesa = mesa.getNumeroMesa();
        this.bloqueada = mesa.isBloqueada();
        this.ocupada = mesa.isOcupada();
    }

    public int getId() {
        return id;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public Mesa toModel() {
        return new Mesa(id, numeroMesa, 0, 0, ocupada, bloqueada);
    }
}
