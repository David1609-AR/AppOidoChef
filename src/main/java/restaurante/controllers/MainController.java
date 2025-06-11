package restaurante.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import restaurante.models.Mesa;
import restaurante.services.MesaService;
import restaurante.websocket.PedidoWebSocketClientFX;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController {
    private Mesa mesa;
    private static MainController instancia;

    // Constructor que asigna la instancia actual para acceso global
    public MainController() {
        instancia = this;
    }

    public static MainController getInstancia() {
        return instancia;
    }

    // Elementos de la interfaz definidos en el archivo FXML
    @FXML private BorderPane mainPane;
    @FXML private StackPane contentPane;
    @FXML private VBox welcomeView;
    @FXML private ScrollPane scrollMesas;
    @FXML private Pane mesaPane;
    @FXML private Button btnAddMesa;
    @FXML private Button btnEliminarMesa;
    @FXML private Button btnGuardarPosiciones;
    @FXML private Label estadoConexionLabel;

    private final MesaService mesaService = new MesaService();

    // Método de inicialización que se ejecuta al cargar la vista
    @FXML
    public void initialize() {
        PedidoWebSocketClientFX socket = PedidoWebSocketClientFX.getInstance();

        socket.setOnConectadoCallback(() -> {
            estadoConexionLabel.setText("🟢 Conectado al servidor");
            estadoConexionLabel.setStyle("-fx-text-fill: green;");
        });

        if (!socket.estaConectado()) {
            socket.conectar(null);
            estadoConexionLabel.setText("🔴 No conectado al servidor");
            estadoConexionLabel.setStyle("-fx-text-fill: red;");
        } else {
            estadoConexionLabel.setText("🟢 Conectado al servidor");
            estadoConexionLabel.setStyle("-fx-text-fill: green;");
        }

        configurarBotonesMesa();
        mostrarVistaMesas();
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    // Asignación de funcionalidad a botones para gestionar mesas
    private void configurarBotonesMesa() {
        btnAddMesa.setOnAction(e -> {
            Mesa nueva = new Mesa(0, mesaService.obtenerSiguienteNumeroMesa(), 50, 50, false, false);
            mesaService.insertarMesa(nueva);
            mostrarVistaMesas();
        });

        btnEliminarMesa.setOnAction(e -> {
            if (!mesaPane.getChildren().isEmpty()) {
                Node ultima = mesaPane.getChildren().get(mesaPane.getChildren().size() - 1);
                mesaPane.getChildren().remove(ultima);
                mesaService.eliminarUltimaMesa();
            }
        });

        btnGuardarPosiciones.setOnAction(e -> {
            mesaService.guardarPosicionesDesdePane(mesaPane);
        });
    }

    // Visualización de todas las mesas con su estado y eventos
    private void mostrarVistaMesas() {
        welcomeView.setVisible(false);
        scrollMesas.setVisible(true);
        mesaPane.getChildren().clear();

        List<Mesa> mesas = mesaService.obtenerTodas();

        for (Mesa mesa : mesas) {
            Button button = new Button("Mesa " + mesa.getNumeroMesa());
            button.setPrefSize(80, 80);
            button.setLayoutX(mesa.getPosicionX());
            button.setLayoutY(mesa.getPosicionY());

            boolean tienePedido = mesaService.tienePedidoActivo(mesa.getIdMesa());
            if (tienePedido) {
                button.setStyle("-fx-background-color: #f1c40f;");
            } else if (mesa.isOcupada()) {
                button.setStyle("-fx-background-color: red;");
            } else {
                button.setStyle("-fx-background-color: green;");
            }

            button.setOnMousePressed(e -> {
                button.setUserData(new double[]{e.getSceneX(), e.getSceneY(), button.getLayoutX(), button.getLayoutY()});
            });

            button.setOnMouseDragged(e -> {
                double[] datos = (double[]) button.getUserData();
                double offsetX = e.getSceneX() - datos[0];
                double offsetY = e.getSceneY() - datos[1];
                double nuevaX = datos[2] + offsetX;
                double nuevaY = datos[3] + offsetY;

                button.setLayoutX(nuevaX);
                button.setLayoutY(nuevaY);

                mesaService.actualizarPosicionMesa(mesa.getIdMesa(), nuevaX, nuevaY);
            });

            button.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    abrirVentanaPedido(mesa);
                }
            });

            mesaPane.getChildren().add(button);
        }
    }

    // Método para actualizar el color de una mesa según su estado
    public void actualizarColorMesa(Mesa mesaActualizada) {
        for (Node node : mesaPane.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                if (button.getText().equals("Mesa " + mesaActualizada.getNumeroMesa())) {
                    boolean tienePedido = mesaService.tienePedidoActivo(mesaActualizada.getIdMesa());
                    if (tienePedido) {
                        button.setStyle("-fx-background-color: #f1c40f;");
                    } else if (mesaActualizada.isOcupada()) {
                        button.setStyle("-fx-background-color: red;");
                    } else {
                        button.setStyle("-fx-background-color: green;");
                    }
                    break;
                }
            }
        }
    }

    // Métodos para navegación desde el menú lateral
    @FXML
    public void loadVolverInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            Node mainView = loader.load();
            Stage stage = (Stage) mainPane.getScene().getWindow();
            Scene nuevaScene = new Scene((Parent) mainView, 900, 700);
            stage.setScene(nuevaScene);
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Error al volver al inicio:");
            e.printStackTrace();
        }
    }

    @FXML public void loadAgregarProducto() {
        cargarVistaCentral("/views/productos.fxml");
    }

    @FXML private void loadGestionUsuarios() {
        cargarVistaCentral("/views/usuarios.fxml");
    }

    @FXML private void loadNuevaReserva() {
        cargarVistaCentral("/views/reservasView.fxml");
    }

    @FXML private void loadVerReservas() {
        cargarVistaCentral("/views/ver_reservas.fxml");
    }

    // Método auxiliar para cargar contenido en el centro del panel principal
    private void cargarVistaCentral(String rutaFXML) {
        try {
            Node vista = FXMLLoader.load(getClass().getResource(rutaFXML));
            mainPane.setCenter(vista);
        } catch (Exception e) {
            System.err.println("❌ Error al cargar vista: " + rutaFXML);
            e.printStackTrace();
        }
    }

    // Abre la ventana de gestión de pedido para una mesa
    private void abrirVentanaPedido(Mesa mesa) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pedido.fxml"));
            Parent root = loader.load();

            PedidoController pedidoController = loader.getController();
            if (pedidoController != null) {
                pedidoController.setMesa(mesa);
            } else {
                System.err.println("❌ No se pudo obtener el controlador de pedido.fxml");
            }

            root.getStylesheets().add(getClass().getResource("/views/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Pedido - Mesa " + mesa.getNumeroMesa());
            Scene scene = new Scene(root);
            scene.getRoot().setStyle("-fx-font-size: 13px;");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setWidth(900);
            stage.setHeight(700);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Error al abrir ventana pedido:");
            e.printStackTrace();
        }
    }
    @FXML
    private void abrirReadme() {
        try {
            File readme = new File("readme.md");
            if (readme.exists()) {
                Desktop.getDesktop().open(readme);
            } else {
                System.err.println("❌ readme.md no encontrado en: " + readme.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Cierra la aplicación
    @FXML
    private void salir() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.close();
    }
}
