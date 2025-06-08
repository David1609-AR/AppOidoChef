package restaurante.models;

import javafx.beans.property.*;
import restaurante.api.ItemPedidoAPI;

/**
 * Clase que representa un ítem dentro de un pedido.
 */
public class ItemPedido {

    // ============ PROPIEDADES ============

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty pedidoId = new SimpleIntegerProperty(); // NUEVO
    private final ObjectProperty<Producto> producto = new SimpleObjectProperty<>();
    private final IntegerProperty cantidad = new SimpleIntegerProperty(1);
    private final DoubleProperty subtotal = new SimpleDoubleProperty();

    // ============ CONSTRUCTORES ============

    public ItemPedido() {
        this.cantidad.addListener((obs, oldVal, newVal) -> calcularSubtotal());

        this.producto.addListener((obs, oldProd, newProd) -> {
            if (oldProd != null) {
                oldProd.precioProperty().removeListener((o, ov, nv) -> calcularSubtotal());
            }
            if (newProd != null) {
                newProd.precioProperty().addListener((o, ov, nv) -> calcularSubtotal());
            }
            calcularSubtotal();
        });
    }

    public ItemPedido(Producto producto, int cantidad) {
        this();
        this.producto.set(producto);
        this.cantidad.set(Math.max(cantidad, 1));
        calcularSubtotal();
    }

    // ============ LÓGICA ============

    private void calcularSubtotal() {
        if (producto.get() != null) {
            subtotal.set(cantidad.get() * producto.get().getPrecio());
        } else {
            subtotal.set(0);
        }
    }

    // ============ GETTERS & SETTERS ============

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getPedidoId() { return pedidoId.get(); }                 // NUEVO
    public void setPedidoId(int pedidoId) { this.pedidoId.set(pedidoId); } // NUEVO
    public IntegerProperty pedidoIdProperty() { return pedidoId; }      // NUEVO

    public Producto getProducto() { return producto.get(); }
    public void setProducto(Producto producto) { this.producto.set(producto); }
    public ObjectProperty<Producto> productoProperty() { return producto; }

    public int getCantidad() { return cantidad.get(); }
    public void setCantidad(int cantidad) { this.cantidad.set(Math.max(cantidad, 1)); }
    public IntegerProperty cantidadProperty() { return cantidad; }

    public double getSubtotal() { return subtotal.get(); }
    public DoubleProperty subtotalProperty() { return subtotal; }

    public String getCategoria() {
        return producto.get() != null ? producto.get().getCategoria() : "";
    }

    @Override
    public String toString() {
        String nombre = (producto.get() != null) ? producto.get().getNombre() : "Producto desconocido";
        return String.format("%d x %s (%.2f€)", cantidad.get(), nombre, subtotal.get());
    }
    public static ItemPedido fromAPI(ItemPedidoAPI apiItem) {
        Producto producto = new Producto();
        producto.setId(apiItem.getProductoId());
        producto.setNombre(apiItem.getNombreProducto());
        producto.setPrecio(apiItem.getPrecio());
        // Si tienes categoría en API, también podrías poner:
        // producto.setCategoria(apiItem.getCategoria());

        return new ItemPedido(producto, apiItem.getCantidad());
    }


}
