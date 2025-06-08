// ProductoAPI.java
package restaurante.api;

public class ProductoAPI {
    public int id;
    public String nombre;
    public String descripcion;
    public String categoria;
    public double precio;
    public boolean tiene_iva;

    public ProductoAPI() {}

    public ProductoAPI(restaurante.models.Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.categoria = producto.getCategoria();
        this.precio = producto.getPrecio();
        this.tiene_iva = producto.isTieneIva();
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isTieneIva() {
        return tiene_iva;
    }

    public void setTieneIva(boolean tieneIva) {
        this.tiene_iva = tieneIva;
    }


    public restaurante.models.Producto toModel() {
        return new restaurante.models.Producto(id, nombre, descripcion, precio, categoria, tiene_iva);
    }
}
