package restaurante.services;

import restaurante.api.ItemPedidoAPI;
import restaurante.api.MesaAPI;
import restaurante.api.PedidoAPI;
import restaurante.models.ItemPedido;
import restaurante.models.Mesa;
import restaurante.models.Pedido;
import restaurante.models.Producto;
import restaurante.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la lógica de negocio relacionada con pedidos.
 * Gestiona operaciones como guardar, actualizar, eliminar pedidos y obtener información relacionada.
 */
public class PedidoService {

    // Servicio para obtener datos de productos
    private final ProductoService productoService = new ProductoService();

    /**
     * Guarda o actualiza un pedido recibido desde Android o JavaFX.
     * Si ya existe un pedido activo para la mesa, actualiza los productos existentes.
     * @param pedidoAPI objeto PedidoAPI recibido del cliente
     * @return Pedido creado o actualizado, o null si hay error
     */
    public Pedido guardarPedidoDesdeAPI(PedidoAPI pedidoAPI) {
        if (pedidoAPI == null || !pedidoAPI.isValid()) {
            System.out.println("PedidoAPI inválido o sin items.");
            return null;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Conexión establecida para guardar pedido.");

            Pedido pedidoExistente = obtenerPedidoActivoPorMesa(pedidoAPI.getMesaId(), conn);
            if (pedidoExistente != null) {
                // Actualiza los items del pedido existente
                for (ItemPedidoAPI itemAPI : pedidoAPI.getItems()) {
                    actualizarOInsertarItem(itemAPI, pedidoExistente.getId(), conn);
                }
                pedidoExistente.setItems(convertirItems(itemListByPedidoId(pedidoExistente.getId(), conn)));
                return pedidoExistente;
            }

            // Si no hay pedido activo, se inserta uno nuevo
            int nuevoPedidoId = insertarPedidoNuevo(pedidoAPI, conn);

            if (nuevoPedidoId <= 0) {
                return null;
            }

            for (ItemPedidoAPI item : pedidoAPI.getItems()) {
                insertarItem(item, nuevoPedidoId, conn);
            }

            Pedido nuevo = new Pedido();
            nuevo.setId(nuevoPedidoId);
            nuevo.setMesaId(pedidoAPI.getMesaId());
            nuevo.setNumPersonas(pedidoAPI.getNumPersonas());
            nuevo.setItems(convertirItems(itemListByPedidoId(nuevoPedidoId, conn)));

            return nuevo;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Inserta un nuevo pedido en la base de datos y devuelve su ID generado
    private int insertarPedidoNuevo(PedidoAPI pedidoAPI, Connection conn) throws SQLException {
        String sql = "INSERT INTO pedido (id_mesa, num_personas, enviado_a_cocina, cerrado) VALUES (?, ?, false, false)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pedidoAPI.getMesaId());
            stmt.setInt(2, pedidoAPI.getNumPersonas());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    // Elimina los items asociados a un pedido específico
    private void eliminarItemsDePedido(int pedidoId, Connection conn) throws SQLException {
        String sql = "DELETE FROM items_pedido WHERE pedido_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            stmt.executeUpdate();
        }
    }

    // Inserta un item a un pedido
    private void insertarItem(ItemPedidoAPI item, int pedidoId, Connection conn) throws SQLException {
        String sql = "INSERT INTO items_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            stmt.setInt(2, item.getProductoId());
            stmt.setInt(3, item.getCantidad());
            stmt.executeUpdate();
        }
    }

    // Obtiene el pedido activo de una mesa (no cerrado)
    public Pedido obtenerPedidoActivoPorMesa(int mesaId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return obtenerPedidoActivoPorMesa(mesaId, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lógica para obtener un pedido activo usando una conexión existente
    private Pedido obtenerPedidoActivoPorMesa(int mesaId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM pedido WHERE id_mesa = ? AND cerrado = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mesaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("id"));
                pedido.setMesaId(rs.getInt("id_mesa"));
                pedido.setNumPersonas(rs.getInt("num_personas"));
                pedido.setCerrado(rs.getBoolean("cerrado"));
                pedido.setEnviadoACocina(rs.getBoolean("enviado_a_cocina"));
                pedido.setItems(convertirItems(itemListByPedidoId(pedido.getId(), conn)));
                pedido.calcularTotal();
                return pedido;
            }
        }
        return null;
    }

    // Convierte items desde ItemPedidoAPI a modelo interno ItemPedido
    private List<ItemPedido> convertirItems(List<ItemPedidoAPI> itemsAPI) {
        List<ItemPedido> items = new ArrayList<>();
        for (ItemPedidoAPI itemAPI : itemsAPI) {
            Producto producto = productoService.buscarPorId(itemAPI.getProductoId());
            if (producto != null) {
                items.add(new ItemPedido(producto, itemAPI.getCantidad()));
            }
        }
        return items;
    }

    // Consulta todos los items de un pedido
    private List<ItemPedidoAPI> itemListByPedidoId(int pedidoId, Connection conn) throws SQLException {
        List<ItemPedidoAPI> items = new ArrayList<>();
        String sql = "SELECT p.id, p.nombre, p.precio, i.cantidad " +
                "FROM items_pedido i JOIN productos p ON i.producto_id = p.id " +
                "WHERE i.pedido_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new ItemPedidoAPI(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("cantidad")
                ));
            }
        }
        return items;
    }

    // Obtiene un PedidoAPI desde el modelo interno Pedido
    public PedidoAPI obtenerPedidoActivoPorMesaAPI(int mesaId) {
        Pedido pedido = obtenerPedidoActivoPorMesa(mesaId);
        if (pedido == null) return null;

        return new PedidoAPI(pedido.getMesaId(), pedido.getNumPersonas(), convertirItemsAPI(pedido.getItems()));
    }

    // Convierte una lista de ItemPedido a ItemPedidoAPI
    private List<ItemPedidoAPI> convertirItemsAPI(List<ItemPedido> items) {
        List<ItemPedidoAPI> result = new ArrayList<>();
        for (ItemPedido item : items) {
            result.add(new ItemPedidoAPI(
                    item.getProducto().getId(),
                    item.getProducto().getNombre(),
                    item.getProducto().getPrecio(),
                    item.getCantidad()
            ));
        }
        return result;
    }

    // Consulta todos los productos
    public List<Producto> obtenerTodosProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto producto = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getString("categoria"),
                        rs.getBoolean("tiene_iva")
                );
                productos.add(producto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productos;
    }

    // Marca un pedido como enviado a cocina
    public void enviarPedidoACocina(Pedido pedido) {
        if (pedido == null || pedido.getId() <= 0) {
            return;
        }

        String sql = "UPDATE pedido SET enviado_a_cocina = true WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Elimina un pedido activo (y sus items) por mesa
    public boolean eliminarPorMesaId(int mesaId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String eliminarItems = "DELETE FROM items_pedido WHERE pedido_id IN (SELECT id FROM pedido WHERE id_mesa = ? AND cerrado = false)";
            try (PreparedStatement stmtItems = conn.prepareStatement(eliminarItems)) {
                stmtItems.setInt(1, mesaId);
                stmtItems.executeUpdate();
            }

            String eliminarPedido = "DELETE FROM pedido WHERE id_mesa = ? AND cerrado = false";
            try (PreparedStatement stmtPedido = conn.prepareStatement(eliminarPedido)) {
                stmtPedido.setInt(1, mesaId);
                stmtPedido.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Devuelve una lista de mesas como DTO para API
    public List<MesaAPI> obtenerTodasLasMesas() {
        List<MesaAPI> mesas = new ArrayList<>();
        String sql = "SELECT id_mesa, numero_mesa, bloqueada, ocupada FROM mesas";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MesaAPI mesa = new MesaAPI();
                mesa.setId(rs.getInt("id_mesa"));
                mesa.setNumeroMesa(rs.getInt("numero_mesa"));
                mesa.setBloqueada(rs.getBoolean("bloqueada"));
                mesa.setOcupada(rs.getBoolean("ocupada"));
                mesas.add(mesa);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mesas;
    }

    // Guarda un pedido desde JavaFX (modelo interno)
    public Pedido guardar(Pedido pedido) {
        String sqlInsertPedido = "INSERT INTO pedido (id_mesa, enviado_a_cocina, cerrado) VALUES (?, ?, ?)";
        String sqlInsertItem = "INSERT INTO items_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertPedido, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, pedido.getMesaId());
                stmt.setBoolean(2, pedido.isEnviadoACocina());
                stmt.setBoolean(3, false);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    pedido.setId(rs.getInt(1));
                } else {
                    conn.rollback();
                    return null;
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertItem)) {
                for (ItemPedido item : pedido.getItems()) {
                    stmt.setInt(1, pedido.getId());
                    stmt.setInt(2, item.getProducto().getId());
                    stmt.setInt(3, item.getCantidad());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return pedido;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Marca el pedido como cobrado (cerrado)
    public boolean cobrarPedido(int mesaId) {
        String sql = "UPDATE pedido SET cerrado = true WHERE id_mesa = ? AND cerrado = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mesaId);
            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Actualiza cantidad si el producto ya existe en el pedido, si no lo inserta
    private void actualizarOInsertarItem(ItemPedidoAPI item, int pedidoId, Connection conn) throws SQLException {
        String sqlUpdate = "UPDATE items_pedido SET cantidad = cantidad + ? WHERE pedido_id = ? AND producto_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setInt(1, item.getCantidad());
            stmt.setInt(2, pedidoId);
            stmt.setInt(3, item.getProductoId());
            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                String sqlInsert = "INSERT INTO items_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                    insertStmt.setInt(1, pedidoId);
                    insertStmt.setInt(2, item.getProductoId());
                    insertStmt.setInt(3, item.getCantidad());
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    // Devuelve las mesas en forma de modelo interno Mesa
    public List<Mesa> obtenerMesasModelo() {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM mesas";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Mesa mesa = new Mesa(
                        rs.getInt("id_mesa"),
                        rs.getInt("numero_mesa"),
                        rs.getDouble("posicionX"),
                        rs.getDouble("posicionY"),
                        rs.getBoolean("ocupada"),
                        rs.getBoolean("bloqueada")
                );
                mesas.add(mesa);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mesas;
    }
}
