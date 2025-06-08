package restaurante.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import restaurante.models.*;
import restaurante.api.*;
import restaurante.services.*;
import restaurante.websocket.PedidoWebSocketClientFX;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de pedidos desde la vista JavaFX.
 */
public class PedidoController {

    // Contenedor principal donde se añaden dinámicamente las categorías y productos
    @FXML private VBox contenedorCategorias;

    // Lista que muestra los productos añadidos al pedido
    @FXML private ListView<String> listViewPedido;

    // Etiqueta que muestra el total acumulado del pedido
    @FXML private Label labelTotal;

    // Información de la mesa actual y el pedido asociado
    private Mesa mesa;
    private Pedido pedidoActivo;

    // Servicios para obtener productos y manejar pedidos
    private final ProductoService productoService = new ProductoService();
    private final PedidoService pedidoService = new PedidoService();

    // Mapa que almacena productos seleccionados con su cantidad
    private final Map<Producto, Integer> productosSeleccionados = new LinkedHashMap<>();

    // Referencia estática del controlador y la ventana para permitir acceso desde otras clases
    private static PedidoController instancia;
    private static Stage stage;

    // Constructor: guarda instancia del controlador
    public PedidoController() {
        instancia = this;
    }

    public static PedidoController getInstancia() {
        return instancia;
    }

    public static void setStage(Stage s) {
        stage = s;
    }

    public static Stage getStage() {
        return stage;
    }

    // Asigna la mesa actual y carga el pedido activo (si existe) y los productos
    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
        cargarPedidoActivo();
        cargarProductosPorCategoria();
    }

    // Carga el pedido activo desde la base de datos y lo refleja en la vista
    private void cargarPedidoActivo() {
        pedidoActivo = pedidoService.obtenerPedidoActivoPorMesa(mesa.getIdMesa());
        productosSeleccionados.clear();

        if (pedidoActivo != null && pedidoActivo.getItems() != null) {
            for (ItemPedido item : pedidoActivo.getItems()) {
                productosSeleccionados.put(item.getProducto(), item.getCantidad());
            }
        }
        actualizarLista();
    }

    // Carga todos los productos agrupados por categoría y los muestra como botones
    private void cargarProductosPorCategoria() {
        contenedorCategorias.getChildren().clear();
        Map<String, List<Producto>> productosPorCategoria = productoService.obtenerAgrupadosPorCategoria();

        for (Map.Entry<String, List<Producto>> entry : productosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            List<Producto> productos = entry.getValue();

            Label titulo = new Label(categoria);
            titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            contenedorCategorias.getChildren().add(titulo);

            for (Producto producto : productos) {
                Button btn = new Button(producto.getNombre() + " (" + producto.getPrecio() + "€)");
                btn.setMaxWidth(Double.MAX_VALUE);

                // Al pulsar el botón se incrementa la cantidad del producto seleccionado
                btn.setOnAction(e -> {
                    productosSeleccionados.merge(producto, 1, Integer::sum);
                    actualizarLista();
                });

                contenedorCategorias.getChildren().add(btn);
            }

            contenedorCategorias.getChildren().add(new Separator());
        }
    }

    // Envía el pedido al backend (evita duplicar productos ya existentes)
    @FXML
    private void enviarPedido() {
        if (productosSeleccionados.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "El pedido está vacío.");
            return;
        }

        pedidoActivo = pedidoService.obtenerPedidoActivoPorMesa(mesa.getIdMesa());
        List<ItemPedidoAPI> itemsAPI = new ArrayList<>();

        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            boolean yaExiste = false;

            if (pedidoActivo != null && pedidoActivo.getItems() != null) {
                for (ItemPedido existente : pedidoActivo.getItems()) {
                    if (existente.getProducto().getId() == entry.getKey().getId()) {
                        yaExiste = true;
                        break;
                    }
                }
            }

            if (!yaExiste) {
                itemsAPI.add(new ItemPedidoAPI(
                        entry.getKey().getId(),
                        entry.getKey().getNombre(),
                        entry.getKey().getPrecio(),
                        entry.getValue()
                ));
            }
        }

        if (itemsAPI.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Todos los productos ya estaban en el pedido.");
            return;
        }

        PedidoAPI pedidoAPI = new PedidoAPI(mesa.getIdMesa(), 1, itemsAPI);
        Pedido guardado = pedidoService.guardarPedidoDesdeAPI(pedidoAPI);

        if (guardado != null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Pedido enviado correctamente.");

            productosSeleccionados.clear();
            cargarPedidoActivo();

            // Notificar a cocina vía WebSocket
            for (ItemPedido item : guardado.getItems()) {
                PedidoWebSocketClientFX.getInstance().enviarProductoHecho(item);
            }

            // Abrir vista de cocina con los nuevos productos
            try {
                CocinaController cocinaController = CocinaController.abrirVentana();
                cocinaController.cargarProductosParaCocina(guardado.getItems(), guardado.getMesaId());
            } catch (IOException e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Cocina.");
            }

            // Actualizar color de mesa en la vista principal
            MainController.getInstancia().actualizarColorMesa(mesa);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al enviar pedido.");
        }
    }

    // Marca la mesa como cobrada y limpia el pedido
    @FXML
    private void cobrarMesa() {
        boolean exito = pedidoService.cobrarPedido(mesa.getIdMesa());
        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Mesa cobrada con éxito.");
            productosSeleccionados.clear();
            actualizarLista();
            MainController.getInstancia().actualizarColorMesa(mesa);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo cobrar la mesa.");
        }
    }

    // Cierra la ventana actual del pedido
    @FXML
    private void cerrarVentana() {
        if (stage != null) {
            stage.close();
            instancia = null;
            stage = null;
        } else {
            Stage s = (Stage) listViewPedido.getScene().getWindow();
            s.close();
            instancia = null;
        }
    }

    // Actualiza la lista visual del pedido y el total
    private void actualizarLista() {
        listViewPedido.getItems().clear();
        double total = 0;

        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            double subtotal = entry.getKey().getPrecio() * entry.getValue();
            total += subtotal;
            listViewPedido.getItems().add(entry.getKey().getNombre() + " x" + entry.getValue() + " - " + String.format("%.2f", subtotal) + "€");
        }

        labelTotal.setText(String.format("Total: %.2f€", total));
    }

    // Abre la vista de cocina manualmente
    @FXML
    private void abrirVistaCocina() {
        try {
            CocinaController nuevaCocina = CocinaController.abrirVentana();
            if (pedidoActivo != null && pedidoActivo.getItems() != null && !pedidoActivo.getItems().isEmpty()) {
                nuevaCocina.cargarProductosParaCocina(pedidoActivo.getItems(), pedidoActivo.getMesaId());
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo abrir la ventana de Cocina.");
        }
    }

    // Elimina el producto seleccionado actualmente de la lista de pedido
    @FXML
    private void eliminarProductoSeleccionado() {
        int index = listViewPedido.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Producto productoAEliminar = new ArrayList<>(productosSeleccionados.keySet()).get(index);
            productosSeleccionados.remove(productoAEliminar);  // Elimina completamente
            actualizarLista();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un producto para eliminar.");
        }
    }

    // Muestra una alerta simple con el tipo y el mensaje
    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje);
        alert.showAndWait();
    }
}
