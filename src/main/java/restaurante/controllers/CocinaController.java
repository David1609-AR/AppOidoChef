package restaurante.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import restaurante.models.ItemPedido;
import restaurante.websocket.PedidoWebSocketClientFX;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para la ventana de cocina.
 * Muestra pedidos agrupados por mesa y categoría, permitiendo marcar productos como "listos".
 */
public class CocinaController {

    @FXML private VBox contenedorPedidos;  // Contenedor principal donde se añaden los pedidos agrupados

    private Stage stage;

    // Mapa que agrupa los pedidos por mesa (clave = idMesa)
    private final Map<Integer, List<ItemPedido>> pedidosPorMesa = new HashMap<>();

    // Mapa auxiliar para convertir id_mesa a numero_mesa
    private final Map<Integer, Integer> idToNumeroMesa = new HashMap<>();

    // Conjunto de categorías visibles en cocina
    private static final Set<String> CATEGORIAS_SET = Set.of(
            "Entrantes", "Ensaladas", "Platos principales", "Sugerencias",
            "Carne", "Pescado", "Postres"
    );

    /**
     * Abre la ventana de cocina desde cualquier parte del programa.
     * @return una instancia de CocinaController ya inicializada.
     */
    public static CocinaController abrirVentana() throws IOException {
        FXMLLoader loader = new FXMLLoader(CocinaController.class.getResource("/views/vista_cocina.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Pedidos en Cocina");
        stage.setScene(new Scene(root, 400, 400));
        stage.setMinWidth(300);
        stage.setMinHeight(300);

        CocinaController controller = loader.getController();
        controller.stage = stage;
        stage.show();

        return controller;
    }

    /**
     * Carga los productos que deben mostrarse en cocina para una mesa específica.
     * Solo se muestran productos de categorías relevantes para cocina.
     */
    public void cargarProductosParaCocina(List<ItemPedido> items, int mesaId) {
        if (items == null || items.isEmpty()) return;

        // Asegura que el mapeo id -> número de mesa está disponible
        cargarNumerosDeMesaSiNecesario();

        // Filtra solo los productos relevantes para cocina
        List<ItemPedido> filtrados = items.stream()
                .filter(item -> item.getProducto().getCategoria() != null &&
                        CATEGORIAS_SET.contains(item.getProducto().getCategoria()))
                .collect(Collectors.toList());

        // Guarda los productos filtrados por mesa
        pedidosPorMesa.put(mesaId, new ArrayList<>(filtrados));

        actualizarVista();
    }

    /**
     * Carga una sola vez el mapeo de ID de mesa a número de mesa desde la base de datos.
     */
    private void cargarNumerosDeMesaSiNecesario() {
        if (!idToNumeroMesa.isEmpty()) return;

        try (Connection conn = restaurante.util.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_mesa, numero_mesa FROM mesas");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                idToNumeroMesa.put(rs.getInt("id_mesa"), rs.getInt("numero_mesa"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log de error si hay fallo de conexión o consulta
        }
    }

    /**
     * Refresca el contenido de la vista con todos los pedidos agrupados por mesa y categoría.
     */
    private void actualizarVista() {
        contenedorPedidos.getChildren().clear();

        for (Map.Entry<Integer, List<ItemPedido>> entrada : pedidosPorMesa.entrySet()) {
            int idMesa = entrada.getKey();
            List<ItemPedido> items = entrada.getValue();

            // Obtener el número de mesa desde el ID
            int numeroMesa = idToNumeroMesa.getOrDefault(idMesa, idMesa);

            // Crear etiqueta con número de mesa
            Label labelMesa = new Label("Mesa # " + numeroMesa);
            labelMesa.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 10 0;");
            contenedorPedidos.getChildren().add(labelMesa);

            // Agrupación visual con Accordion
            Accordion accordion = new Accordion();
            Map<String, VBox> contenidoPorCategoria = new HashMap<>();

            // Agrupar productos por categoría y nombre
            Map<String, ItemPedido> agrupados = new HashMap<>();
            for (ItemPedido item : items) {
                String categoria = item.getProducto().getCategoria();
                String clave = categoria + "-" + item.getProducto().getNombre();

                // Si el producto ya existe, sumar cantidades
                agrupados.merge(clave, item, (existente, nuevo) -> {
                    existente.setCantidad(existente.getCantidad() + nuevo.getCantidad());
                    return existente;
                });

                // Crear contenedor por categoría si no existe
                contenidoPorCategoria.computeIfAbsent(categoria, c -> new VBox(8));
            }

            // Añadir productos agrupados al contenido por categoría
            for (Map.Entry<String, VBox> entry : contenidoPorCategoria.entrySet()) {
                String categoria = entry.getKey();
                VBox contenido = entry.getValue();

                // Añadir productos por categoría al VBox correspondiente
                agrupados.entrySet().stream()
                        .filter(e -> e.getKey().startsWith(categoria + "-"))
                        .forEach(e -> {
                            ItemPedido item = e.getValue();

                            HBox fila = new HBox(10); // Línea visual para el producto
                            Label nombre = new Label(item.getProducto().getNombre() + " x" + item.getCantidad());

                            // Botón "Listo" para marcar producto como preparado
                            Button btnListo = new Button("Listo");
                            btnListo.setOnAction(ev -> {
                                btnListo.setDisable(true);
                                PedidoWebSocketClientFX.getInstance().enviarProductoHecho(item, numeroMesa);
                            });

                            // Botón "Eliminar" para quitarlo de la vista
                            Button btnEliminar = new Button("✖");
                            btnEliminar.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                            btnEliminar.setOnAction(ev -> contenido.getChildren().remove(fila));

                            fila.getChildren().addAll(nombre, btnListo, btnEliminar);
                            contenido.getChildren().add(fila);
                        });

                // Añadir cada sección de categoría como TitledPane al acordeón
                TitledPane pane = new TitledPane(categoria, contenido);
                accordion.getPanes().add(pane);
            }

            contenedorPedidos.getChildren().add(accordion);
        }
    }

    /**
     * Lleva la ventana de cocina al frente si ya está abierta.
     */
    public void traerAlFrente() {
        if (stage != null) stage.toFront();
    }
}
