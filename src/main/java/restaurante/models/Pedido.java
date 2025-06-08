package restaurante.models;

import restaurante.api.ItemPedidoAPI;
import restaurante.api.PedidoAPI;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un pedido realizado en el restaurante.
 * Contiene todos los elementos de un pedido y su estado actual.
 *
 * Manual para estudiantes:
 * 1. Atributos: Datos básicos del pedido (ID, mesa, items, etc.)
 * 2. Constructores: Para crear nuevos pedidos
 * 3. Métodos principales: Gestión de items y cálculos
 * 4. Estado: Control del flujo del pedido (enviado, cerrado)
 * 5. Patrones: Ejemplo de modelo en arquitectura MVC
 */
public class Pedido {

    // ============ ATRIBUTOS ============

    private int id;  // ID único del pedido
    private int Id_Mesa;  // ID de la mesa asignada
    private List<ItemPedido> items;  // Lista de productos pedidos
    private transient LocalDateTime fechaHora;  // Fecha de creación (no persistente)
    private boolean enviadoACocina;  // ¿Fue enviado ya a cocina?
    private boolean cerrado;  // ¿Ya se cobró y cerró?
    private transient LocalDateTime horaEnvio;  // Hora en que se envió a cocina
    private double total;  // Total calculado

    private Mesa mesa;  // Referencia opcional a la mesa (puede ser nula)
    private int numPersonas = 1;  // Por defecto 1 persona

    // ============ CONSTRUCTORES ============

    /**
     * Constructor para nuevo pedido sin mesa.
     */
    public Pedido() {
    }



    /**
     * Constructor que recibe el ID de la mesa asociada.
     * @param mesaId ID de la mesa
     */
    public Pedido(int mesaId) {
        this(); // llama al constructor anterior
        this.Id_Mesa = mesaId;
    }

    // ============ MÉTODOS PRINCIPALES ============
    public void agregarItem(Producto producto, int cantidad) {
        if (producto == null || cantidad <= 0) return;

        // Si ya existe el producto, actualiza cantidad
        for (ItemPedido item : items) {
            if (item.getProducto().getId() == producto.getId()) {
                item.setCantidad(item.getCantidad() + cantidad);
                return;
            }
        }

        // Si no existía, lo agrega
        items.add(new ItemPedido(producto, cantidad));
    }

    /**
     * Elimina un item del pedido por ID.
     * @param itemId ID del item
     * @return true si se eliminó, false si no
     */
    public boolean eliminarItem(int itemId) {
        boolean eliminado = items.removeIf(item -> item.getId() == itemId);
        if (eliminado) calcularTotal();
        return eliminado;
    }

    /**
     * Suma todos los subtotales de los productos para calcular el total.
     */
    public void calcularTotal() {
        this.total = items.stream()
                .mapToDouble(ItemPedido::getSubtotal)
                .sum();
    }

    /**
     * Agrega un item ya creado (usado por la API).
     * @param item ItemPedido ya construido
     */
    public void addItem(ItemPedido item) {
        if (item != null) {
            this.items.add(item);
            calcularTotal();
        }
    }
    public void agregarOActualizarItem(ItemPedido nuevoItem) {
        for (ItemPedido item : this.items) {
            if (item.getProducto().getId() == nuevoItem.getProducto().getId()) {
                item.setCantidad(item.getCantidad() + nuevoItem.getCantidad());
                return;
            }
        }
        this.items.add(nuevoItem);
    }


    // ============ GETTERS Y SETTERS ============

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMesaId() {
        return Id_Mesa;
    }

    public void setMesaId(int mesaId) {
        this.Id_Mesa = mesaId;
    }

    public List<ItemPedido> getItems() {
        return new ArrayList<>(items); // Copia defensiva
    }

    public void setItems(List<ItemPedido> items) {
        this.items = new ArrayList<>(items); // Copia defensiva

    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public boolean isEnviadoACocina() {
        return enviadoACocina;
    }

    public void setEnviadoACocina(boolean enviadoACocina) {
        this.enviadoACocina = enviadoACocina;
        if (enviadoACocina) {
            this.horaEnvio = LocalDateTime.now();
        }
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public void setCerrado(boolean cerrado) {
        this.cerrado = cerrado;
    }

    public LocalDateTime getHoraEnvio() {
        return horaEnvio;
    }

    public double getTotal() {
        return total;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        if (numPersonas > 0) {
            this.numPersonas = numPersonas;
        }
    }
    // ============ TO STRING ============

    @Override
    public String toString() {
        return "Pedido #" + id + " - Mesa " + Id_Mesa + " - Total: " + total + "€";
    }
    public static Pedido fromAPI(PedidoAPI apiPedido) {
        Pedido pedido = new Pedido();
        pedido.setMesaId(apiPedido.getMesaId());
        pedido.setNumPersonas(apiPedido.getNumPersonas());

        List<ItemPedido> itemsConvertidos = new ArrayList<>();
        for (ItemPedidoAPI apiItem : apiPedido.getItems()) {
            itemsConvertidos.add(ItemPedido.fromAPI(apiItem));
        }

        pedido.setItems(itemsConvertidos);
        pedido.calcularTotal();

        return pedido;
    }
    /**
     * Agrega un producto al pedido con cantidad 1, o incrementa si ya existe.
     */
    public void agregarProducto(Producto producto) {
        for (ItemPedido item : items) {
            if (item.getProducto().getId() == producto.getId()) {
                item.setCantidad(item.getCantidad() + 1);
                return;
            }
        }
        items.add(new ItemPedido(producto, 1)); // o usa el constructor adecuado con cantidad
    }


}
