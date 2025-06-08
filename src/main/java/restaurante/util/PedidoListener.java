package restaurante.util;

import restaurante.models.Pedido;

/**
 * Interfaz que define el contrato para recibir notificaciones de pedidos.
 */
public interface PedidoListener {

    /**
     * Método que se llama cuando llega un nuevo pedido.
     * @param pedido El pedido recibido.
     */
    void onPedidoRecibido(Pedido pedido);
}
