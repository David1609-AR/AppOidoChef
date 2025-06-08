package restaurante.controllers;

import com.google.gson.Gson;
import restaurante.api.PedidoAPI;
import restaurante.models.Pedido;
import restaurante.services.PedidoService;
import static spark.Spark.*;

/**
 * Controlador que gestiona las rutas REST relacionadas con pedidos.
 * Este controlador reemplaza al antiguo PedidoController.
 */
public class DetallePedidoController {

    // Instancia del servicio que gestiona la lógica de negocio de pedidos
    private static final PedidoService pedidoService = new PedidoService();

    // Instancia de Gson para convertir entre objetos Java y JSON
    private static final Gson gson = new Gson();

    /**
     * Registra todas las rutas REST disponibles en esta API para gestionar pedidos.
     * Las rutas permiten consultar y guardar pedidos.
     */
    public static void registrarRutas() {

        // Ruta GET: Obtener el pedido activo de una mesa específica
        get("/api/pedidos/:mesaId", (req, res) -> {
            int mesaId = Integer.parseInt(req.params("mesaId"));  // Obtener ID de mesa desde la URL
            PedidoAPI pedido = pedidoService.obtenerPedidoActivoPorMesaAPI(mesaId);  // Buscar pedido

            res.type("application/json");
            if (pedido != null) {
                res.status(200);
                return gson.toJson(pedido);  // Devolver el pedido si existe
            } else {
                res.status(404);
                return gson.toJson("No hay pedido activo para esta mesa");
            }
        });

        // Ruta POST: Crear o actualizar un pedido desde un cuerpo JSON recibido
        post("/api/pedidos", (req, res) -> {
            PedidoAPI pedidoAPI = gson.fromJson(req.body(), PedidoAPI.class);  // Deserializar JSON a objeto
            Pedido guardado = pedidoService.guardarPedidoDesdeAPI(pedidoAPI);  // Guardar en BD

            res.type("application/json");
            if (guardado != null) {
                res.status(200);
                return gson.toJson(new PedidoAPI(guardado));  // Devolver el pedido guardado como respuesta
            } else {
                res.status(500);
                return gson.toJson("Error al guardar pedido");
            }
        });
    }
}
