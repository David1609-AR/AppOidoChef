// Paquete principal de la aplicación JavaFX
package restaurante;

// Importación de la clase base para aplicaciones JavaFX
import javafx.application.Application;

// Importación para cargar archivos FXML (interfaz visual definida en XML)
import javafx.fxml.FXMLLoader;

// Importaciones para representar elementos visuales y escenas
import javafx.scene.Parent;
import javafx.scene.Scene;

// Importación para manipular la ventana principal de la app
import javafx.stage.Stage;

// Clase que inicializa un servidor REST embebido para la aplicación
import restaurante.controllers.RestServer;

// Cliente WebSocket personalizado para recibir pedidos en tiempo real
import restaurante.websocket.PedidoWebSocketClientFX;

/**
 * Clase principal de la aplicación JavaFX.
 * Se encarga de iniciar la ventana principal, cargar la interfaz desde FXML,
 * aplicar los estilos CSS, iniciar el servidor REST y conectar al WebSocket.
 */
public class Main extends Application {

    /**
     * Método que se ejecuta automáticamente cuando se lanza la app.
     * Aquí se configura y muestra la interfaz principal.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicia el servidor REST local para servir datos o recibir peticiones
        RestServer.init();

        // Carga la interfaz gráfica desde el archivo FXML principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
        Parent root = loader.load(); // Se carga todo el contenido visual

        // Crea la escena principal con tamaño fijo
        Scene scene = new Scene(root, 900, 700);

        // Aplica la hoja de estilos CSS para personalizar la apariencia
        scene.getStylesheets().add(getClass().getResource("/views/styles.css").toExternalForm());

        // Configura la ventana principal (Stage)
        primaryStage.setTitle("AppOidoChef - Restaurante"); // Título de la ventana
        primaryStage.setScene(scene);                       // Se asigna la escena
        primaryStage.setResizable(false);                   // Se bloquea el redimensionamiento
        primaryStage.show();                                // Se muestra la ventana

        // Se conecta al WebSocket para recibir notificaciones de nuevos pedidos
        PedidoWebSocketClientFX.getInstance().conectar(pedido -> {
            // Esta función se ejecuta cuando se recibe un pedido por WebSocket
            System.out.println("Pedido recibido por WebSocket: mesa " + pedido.getMesaId());
            // Aquí puedes actualizar la interfaz o mostrar notificaciones visuales
        });
    }

    /**
     * Punto de entrada de la aplicación. Llama al sistema JavaFX.
     */
    public static void main(String[] args) {
        launch(args); // Lanza la aplicación y llama al método start()
    }
}
