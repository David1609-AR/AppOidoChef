package restaurante.services;

import restaurante.models.Producto;
import restaurante.models.Usuario;
import restaurante.util.DatabaseConnection;

import java.sql.*;
import java.util.*;

/**
 * Servicio para gestionar operaciones con productos y usuarios en la base de datos.
 * Contiene métodos CRUD para ambas entidades.
 */
public class ProductoService {

    // === MÉTODOS DE PRODUCTOS ===

    /**
     * Obtiene todos los productos de la base de datos.
     * @return Lista de productos existentes.
     */
    public List<Producto> obtenerTodos() {
        List<Producto> productos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM productos");
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

    /**
     * Busca un producto por su ID.
     * @param id identificador del producto
     * @return Producto encontrado o null si no existe
     */
    public Producto buscarPorId(int id) {
        String sql = "SELECT * FROM productos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Inserta un nuevo producto en la base de datos.
     * @param producto objeto producto a insertar
     * @return true si fue insertado correctamente
     */
    public boolean crearProducto(Producto producto) {
        String sql = "INSERT INTO productos (nombre, descripcion, precio, categoria, tiene_iva) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getNombre());
            stmt.setString(2, producto.getDescripcion());
            stmt.setDouble(3, producto.getPrecio());
            stmt.setString(4, producto.getCategoria());
            stmt.setBoolean(5, producto.isTieneIva());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza un producto existente.
     * @param producto objeto con los nuevos valores
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarProducto(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, categoria = ?, tiene_iva = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getNombre());
            stmt.setString(2, producto.getDescripcion());
            stmt.setDouble(3, producto.getPrecio());
            stmt.setString(4, producto.getCategoria());
            stmt.setBoolean(5, producto.isTieneIva());
            stmt.setInt(6, producto.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un producto por su ID.
     * @param id identificador del producto
     * @return true si se eliminó correctamente
     */
    public boolean eliminarPorId(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Agrupa los productos por categoría.
     * @return Mapa con la categoría como clave y la lista de productos como valor
     */
    public Map<String, List<Producto>> obtenerAgrupadosPorCategoria() {
        Map<String, List<Producto>> agrupados = new LinkedHashMap<>();
        for (Producto producto : obtenerTodos()) {
            String categoria = producto.getCategoria();
            if (categoria == null || categoria.isBlank()) {
                categoria = "Sin categoría";
            }
            agrupados.computeIfAbsent(categoria, k -> new ArrayList<>()).add(producto);
        }
        return agrupados;
    }

    /**
     * Mapea un ResultSet a un objeto Producto.
     * @param rs ResultSet que contiene los datos de un producto
     * @return Objeto Producto mapeado
     * @throws SQLException en caso de error al acceder a los datos
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getString("categoria"),
                rs.getBoolean("tiene_iva")
        );
    }

    // === MÉTODOS DE USUARIOS ===

    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param username nombre de usuario
     * @param password contraseña del usuario
     * @return true si se insertó correctamente
     */
    public boolean insertarUsuario(String username, String password) {
        String sql = "INSERT INTO usuarios (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Valida que existan credenciales correctas para un usuario.
     * @param username nombre de usuario
     * @param password contraseña
     * @return true si el usuario existe con esa contraseña
     */
    public boolean validarCredenciales(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un usuario por nombre de usuario.
     * @param username nombre de usuario
     * @return true si fue eliminado correctamente
     */
    public boolean eliminarUsuario(String username) {
        String sql = "DELETE FROM usuarios WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si existe un usuario con el nombre dado.
     * @param username nombre de usuario
     * @return true si existe
     */
    public boolean existeUsuario(String username) {
        String sql = "SELECT id FROM usuarios WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los usuarios registrados.
     * @return Lista de objetos Usuario
     */
    public List<Usuario> obtenerUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(rs.getInt("id"), rs.getString("username"), rs.getString("password")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Elimina un usuario por su ID.
     * @param id identificador del usuario
     * @return true si fue eliminado correctamente
     */
    public boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
