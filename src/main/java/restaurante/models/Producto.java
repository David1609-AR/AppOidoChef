package restaurante.models;

import javafx.beans.property.*;

public class Producto {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty descripcion = new SimpleStringProperty();
    private final DoubleProperty precio = new SimpleDoubleProperty();
    private final StringProperty categoria = new SimpleStringProperty();
    private final BooleanProperty tiene_iva = new SimpleBooleanProperty();

    public Producto() {}

    public Producto(int id, String nombre, String descripcion, double precio, String categoria, boolean tiene_iva) {
        setId(id);
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrecio(precio);
        setCategoria(categoria);
        setTieneIva(tiene_iva);
    }

    // Getters y setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public StringProperty nombreProperty() { return nombre; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String descripcion) { this.descripcion.set(descripcion); }
    public StringProperty descripcionProperty() { return descripcion; }

    public double getPrecio() { return precio.get(); }
    public void setPrecio(double precio) { this.precio.set(precio); }
    public DoubleProperty precioProperty() { return precio; }

    public String getCategoria() { return categoria.get(); }
    public void setCategoria(String categoria) { this.categoria.set(capitalizar(categoria)); }
    public StringProperty categoriaProperty() { return categoria; }

    public boolean isTieneIva() { return tiene_iva.get(); }
    public void setTieneIva(boolean tieneIva) { this.tiene_iva.set(tieneIva); }
    public BooleanProperty tieneIvaProperty() { return tiene_iva; }

    @Override
    public String toString() {
        return getNombre() + " - " + getPrecio() + " €";
    }

    private String capitalizar(String texto) {
        if (texto == null) return "";
        texto = texto.trim().toLowerCase();
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
