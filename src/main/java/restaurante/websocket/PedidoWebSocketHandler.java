package restaurante.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manejador WebSocket del lado del servidor para la recepción y envío de mensajes de pedidos.
 * Utiliza Jetty como implementación del servidor WebSocket.
 * Se encarga de:
 * - Gestionar conexiones de clientes (JavaFX, Android, etc.).
 * - Escuchar mensajes entrantes.
 * - Reenviar mensajes a todos los clientes conectados (broadcast).
 */
public class PedidoWebSocketHandler implements WebSocketListener {

    // Conjunto de sesiones activas (clientes conectados)
    // Se usa ConcurrentHashMap para acceso concurrente seguro
    private static final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Referencia a la sesión individual del cliente conectado actualmente
    private Session session;

    /**
     * Se ejecuta automáticamente cuando un cliente establece una conexión WebSocket.
     * @param session Sesión establecida entre servidor y cliente.
     */
    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        sessions.add(session); // Se registra la nueva sesión
        System.out.println("Cliente conectado: " + session.getRemoteAddress().getAddress());
    }

    /**
     * Se ejecuta automáticamente cuando un cliente cierra la conexión WebSocket.
     * @param statusCode Código de cierre.
     * @param reason Motivo del cierre.
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        sessions.remove(session); // Se elimina la sesión cerrada
        System.out.println("Cliente desconectado: " + reason);
    }

    /**
     * Se ejecuta si ocurre un error en la conexión WebSocket.
     * @param cause Excepción o causa del error.
     */
    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
    }

    /**
     * Se ejecuta al recibir un mensaje de texto desde un cliente.
     * En este caso, se reenvía el mensaje a todos los clientes conectados.
     * @param message Mensaje recibido del cliente.
     */
    @Override
    public void onWebSocketText(String message) {
        System.out.println("Mensaje recibido: " + message);
        broadcast(message); // Reenvía a todos los demás clientes
    }

    /**
     * Método no implementado: no se usa en esta aplicación ya que no se reciben mensajes binarios.
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // No usado en esta aplicación
    }

    /**
     * Envia un mensaje a todos los clientes conectados al WebSocket.
     * @param message El mensaje a enviar.
     */
    private void broadcast(String message) {
        for (Session sess : sessions) {
            if (sess.isOpen()) {
                try {
                    sess.getRemote().sendString(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
