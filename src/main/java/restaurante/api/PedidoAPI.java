package restaurante.api;

import restaurante.models.ItemPedido;
import restaurante.models.Pedido;
import restaurante.models.Producto;

import java.util.ArrayList;
import java.util.List;

public class PedidoAPI {

    private int mesaId;
    private int numPersonas;
    private int numeroMesa;
    private MesaAPI mesa;
    private List<ItemPedidoAPI> items = new ArrayList<>();

    public PedidoAPI() {}

    public PedidoAPI(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("El pedido no puede ser null");
        this.mesaId = pedido.getMesaId();
        this.numPersonas = pedido.getNumPersonas();
        if (pedido.getMesa() != null) {
            this.mesa = new MesaAPI(pedido.getMesa());
            this.numeroMesa = pedido.getMesa().getNumeroMesa();
        }
        this.items = new ArrayList<>();
        if (pedido.getItems() != null) {
            for (ItemPedido item : pedido.getItems()) {
                this.items.add(new ItemPedidoAPI(item));
            }
        }
    }

    public PedidoAPI(int mesaId, int numPersonas, List<ItemPedidoAPI> items) {
        this.mesaId = mesaId;
        this.numPersonas = numPersonas;
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public boolean isValid() {
        return items != null && !items.isEmpty();
    }

    // Getters and setters...

    public int getMesaId() { return mesaId; }
    public void setMesaId(int mesaId) { this.mesaId = mesaId; }

    public int getNumPersonas() { return numPersonas; }
    public void setNumPersonas(int numPersonas) { this.numPersonas = numPersonas; }

    public List<ItemPedidoAPI> getItems() {
        if (items == null) items = new ArrayList<>();
        return items;
    }
    public void setItems(List<ItemPedidoAPI> items) {
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public MesaAPI getMesa() { return mesa; }
    public void setMesa(MesaAPI mesa) { this.mesa = mesa; }

    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }

    public Pedido toModel() {
        Pedido pedido = new Pedido();
        pedido.setMesaId(this.mesaId);
        pedido.setNumPersonas(this.numPersonas);
        pedido.setCerrado(false);
        pedido.setEnviadoACocina(false);

        List<ItemPedido> itemPedidos = new ArrayList<>();
        if (items != null) {
            for (ItemPedidoAPI apiItem : items) {
                Producto producto = new Producto(
                        apiItem.getProductoId(),
                        apiItem.getNombreProducto(),
                        "", // descripción no enviada
                        apiItem.getPrecio(),
                        apiItem.getCategoria() != null ? apiItem.getCategoria() : "General",
                        true // tiene_iva por defecto
                );
                itemPedidos.add(new ItemPedido(producto, apiItem.getCantidad()));
            }
        }
        pedido.setItems(itemPedidos);
        return pedido;
    }
}
