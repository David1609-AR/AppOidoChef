package restaurante.controllers;

import static spark.Spark.*;

import com.google.gson.Gson;
import restaurante.api.PedidoAPI;
import restaurante.api.ProductoAPI;
import restaurante.models.Pedido;
import restaurante.services.PedidoService;
import restaurante.services.ProductoService;
import restaurante.websocket.PedidoWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servidor REST y WebSocket para el sistema de restaurante.
 * Expone endpoints para login, productos, mesas y pedidos, y permite comunicación en tiempo real con WebSocket.
 */
public class RestServer {

    // Utilidad para convertir objetos Java a JSON y viceversa
    private static final Gson gson = new Gson();

    // Servicios para manejar lógica de pedidos y productos
    private static final PedidoService pedidoService = new PedidoService();
    private static final ProductoService productoService = new ProductoService();

    /**
     * Método principal que configura el servidor Spark, rutas REST, WebSocket y CORS.
     */
    public static void init() {
        // Establecer IP local y puerto de escucha
        ipAddress("192.168.1.16");
        port(4567);

        // WebSocket para actualizar pedidos en tiempo real desde clientes
        webSocket("/ws/pedidos", PedidoWebSocketHandler.class);
        spark.Spark.init(); // Inicializar Spark

        // Configurar CORS para permitir peticiones desde cualquier origen
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        // ✅ Ruta POST para login de usuario
        post("/login", (req, res) -> {
            res.type("application/json");

            try {
                // Convertir el JSON recibido en un mapa clave/valor
                var body = gson.fromJson(req.body(), Map.class);
                String username = (String) body.get("username");
                String password = (String) body.get("password");

                // Validar las credenciales contra la base de datos
                boolean valido = productoService.validarCredenciales(username, password);
                return gson.toJson(Map.of("success", valido));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
                return gson.toJson(Map.of("success", false, "error", "Error en el formato del login"));
            }
        });

        // ✅ Ruta GET para obtener todas las mesas
        get("/mesas", (req, res) -> {
            res.type("application/json");
            return gson.toJson(pedidoService.obtenerTodasLasMesas());
        });

        // ✅ Ruta GET para obtener todos los productos disponibles
        get("/productos", (req, res) -> {
            res.type("application/json");

            // Convertir cada producto a su versión API (DTO)
            List<ProductoAPI> productos = productoService.obtenerTodos().stream()
                    .map(ProductoAPI::new)
                    .collect(Collectors.toList());

            return gson.toJson(productos);
        });

        // ✅ Ruta GET para obtener un pedido activo por ID de mesa
        get("/pedido/mesa/:mesaId", (req, res) -> {
            int mesaId = Integer.parseInt(req.params("mesaId"));
            PedidoAPI pedidoAPI = pedidoService.obtenerPedidoActivoPorMesaAPI(mesaId);
            res.type("application/json");

            if (pedidoAPI != null) {
                res.status(200);
                return gson.toJson(pedidoAPI);
            } else {
                res.status(404);
                return gson.toJson("No hay pedido activo para esta mesa");
            }
        });

        // ✅ Ruta POST para guardar un nuevo pedido desde el cliente
        post("/pedido", (req, res) -> {
            PedidoAPI pedido = gson.fromJson(req.body(), PedidoAPI.class);
            Pedido guardado = pedidoService.guardarPedidoDesdeAPI(pedido);
            res.type("application/json");

            if (guardado != null) {
                PedidoAPI guardadoAPI = new PedidoAPI(guardado);
                res.status(200);
                return gson.toJson(guardadoAPI);
            } else {
                res.status(500);
                return gson.toJson("Error al guardar pedido");
            }
        });

        // ✅ Ruta DELETE para eliminar un pedido activo por mesa
        delete("/pedido/mesa/:mesaId", (req, res) -> {
            int mesaId = Integer.parseInt(req.params("mesaId"));
            boolean eliminado = pedidoService.eliminarPorMesaId(mesaId);
            res.type("application/json");

            if (eliminado) {
                res.status(200);
                return gson.toJson("Pedido eliminado correctamente");
            } else {
                res.status(404);
                return gson.toJson("No se encontró pedido para esta mesa");
            }
        });
    }
}
