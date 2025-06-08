package restaurante.api;

import restaurante.models.ItemPedido;

public class ItemPedidoAPI {

    private int productoId;
    private String nombreProducto;
    private double precio;
    private int cantidad;
    private String categoria;

    public ItemPedidoAPI() {}

    public ItemPedidoAPI(ItemPedido item) {
        if (item == null || item.getProducto() == null) throw new IllegalArgumentException("ItemPedido o Producto es null");
        this.productoId = item.getProducto().getId();
        this.nombreProducto = item.getProducto().getNombre();
        this.precio = item.getProducto().getPrecio();
        this.cantidad = item.getCantidad();
        this.categoria = item.getProducto().getCategoria();
    }
    public ItemPedidoAPI(int productoId, String nombreProducto, double precio, int cantidad) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    // Getters y setters...

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
