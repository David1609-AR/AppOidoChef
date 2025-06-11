package restaurante.websocket;

import com.google.gson.Gson;
import javafx.application.Platform;
import restaurante.api.PedidoAPI;
import restaurante.models.ItemPedido;
import restaurante.util.PedidoListener;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Cliente WebSocket para aplicaciones JavaFX que recibe notificaciones de nuevos pedidos desde el servidor.
 * Esta clase es un singleton para asegurar una única conexión WebSocket activa.
 */
public class PedidoWebSocketClientFX {

    // Instancia única (singleton) del cliente WebSocket
    private static PedidoWebSocketClientFX instance;

    // Cliente WebSocket de la librería org.java_websocket
    private WebSocketClient client;

    // Listener externo que será notificado cuando llegue un nuevo pedido
    private PedidoListener listener;

    // Conversor JSON para serialización/deserialización de objetos PedidoAPI
    private final Gson gson = new Gson();

    // Constructor privado para restringir instanciación directa (patrón singleton)
    private PedidoWebSocketClientFX() {
    }

    /**
     * Método estático para obtener la instancia única del cliente WebSocket.
     *
     * @return Instancia única de PedidoWebSocketClientFX.
     */
    public static PedidoWebSocketClientFX getInstance() {
        if (instance == null) {
            instance = new PedidoWebSocketClientFX();
        }
        return instance;
    }

    // Callback que se ejecuta cuando la conexión WebSocket se establece correctamente
    private Runnable onConectadoCallback;

    /**
     * Permite definir una acción personalizada que se ejecutará cuando la conexión WebSocket esté activa.
     *
     * @param callback Acción a ejecutar (por ejemplo, mostrar notificación visual)
     */
    public void setOnConectadoCallback(Runnable callback) {
        this.onConectadoCallback = callback;
    }

    /**
     * Establece la conexión WebSocket y define los manejadores de eventos.
     *
     * @param listener Objeto que recibirá el pedido cuando llegue un mensaje nuevo.
     */
    public void conectar(PedidoListener listener) {
        this.listener = listener;

        try {
            // Dirección del servidor WebSocket
            client = new WebSocketClient(new URI("ws://192.168.1.16:4567/ws/pedidos")) {

                // Se ejecuta cuando la conexión WebSocket se abre exitosamente
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("🟢 WebSocket conectado: " + getURI());

                    // Ejecuta la acción en el hilo de JavaFX
                    if (onConectadoCallback != null) {
                        Platform.runLater(onConectadoCallback);
                    }
                }

                // Se ejecuta al recibir un mensaje desde el servidor
                @Override
                public void onMessage(String message) {
                    System.out.println("Mensaje recibido: " + message);

                    // Convierte el mensaje JSON a objeto PedidoAPI
                    PedidoAPI pedidoAPI = gson.fromJson(message, PedidoAPI.class);

                    // Llama al listener en el hilo de JavaFX
                    Platform.runLater(() -> {
                        if (listener != null) {
                            listener.onPedidoRecibido(pedidoAPI.toModel());
                        }
                    });
                }

                // Se ejecuta cuando la conexión se cierra
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("🔴 WebSocket cerrado: " + reason);
                }

                // Se ejecuta si ocurre un error durante la conexión o transmisión
                @Override
                public void onError(Exception ex) {
                    System.err.println("Error WebSocket: " + ex.getMessage());
                }
            };

            // Inicia la conexión con el servidor
            client.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica si el cliente WebSocket está conectado.
     *
     * @return true si la conexión está abierta; false en caso contrario.
     */
    public boolean estaConectado() {
        return client != null && client.isOpen();
    }

    /**
     * Envía un mensaje al servidor indicando que un producto está listo.
     *
     * @param item El producto que ha sido preparado (cocinado).
     */
    public void enviarProductoHecho(ItemPedido item, int numeroMesa) {
        if (client != null && client.isOpen() && item != null) {
            String mensaje = String.format(
                    "{\"tipo\":\"productoListo\", \"productoId\":%d, \"nombre\":\"%s\", \"cantidad\":%d, \"numeroMesa\":%d}",
                    item.getProducto().getId(),
                    item.getProducto().getNombre(),
                    item.getCantidad(),
                    numeroMesa
            );

            client.send(mensaje);
            System.out.println("Notificación producto listo enviada: " + mensaje);
        }
    }
    // Método viejo que mantiene compatibilidad
    public void enviarProductoHecho(ItemPedido item) {
        enviarProductoHecho(item, -1); // Usa -1 si no se conoce el número de mesa
    }

}
