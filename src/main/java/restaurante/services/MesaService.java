package restaurante.services;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import restaurante.models.Mesa;
import restaurante.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que maneja operaciones sobre las mesas en la base de datos.
 * Permite CRUD, actualización de posiciones y validaciones.
 */
public class MesaService {

    /**
     * Obtiene todas las mesas existentes desde la base de datos.
     */
    public List<Mesa> obtenerTodas() {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT * FROM mesas";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Mesa m = new Mesa(
                        rs.getInt("id_mesa"),
                        rs.getInt("numero_mesa"),
                        rs.getDouble("posicionX"),
                        rs.getDouble("posicionY"),
                        rs.getBoolean("ocupada"),
                        rs.getBoolean("bloqueada")
                );
                lista.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Inserta una nueva mesa en la base de datos.
     */
    public void insertarMesa(Mesa mesa) {
        String sql = "INSERT INTO mesas (numero_mesa, posicionX, posicionY, ocupada, bloqueada) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mesa.getNumeroMesa());
            stmt.setDouble(2, mesa.getPosicionX());
            stmt.setDouble(3, mesa.getPosicionY());
            stmt.setBoolean(4, mesa.isOcupada());
            stmt.setBoolean(5, mesa.isBloqueada());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el siguiente número de mesa disponible (MAX + 1).
     */
    public int obtenerSiguienteNumeroMesa() {
        String sql = "SELECT MAX(numero_mesa) AS max FROM mesas";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("max") + 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1; // Si no hay mesas, empezar en 1
    }

    /**
     * Elimina la última mesa (mayor número) y actualiza el número del resto si es necesario.
     */
    public void eliminarUltimaMesa() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Obtener última mesa
            String obtenerUltima = "SELECT id_mesa, numero_mesa FROM mesas ORDER BY numero_mesa DESC LIMIT 1";
            try (PreparedStatement stmt1 = conn.prepareStatement(obtenerUltima);
                 ResultSet rs = stmt1.executeQuery()) {

                if (rs.next()) {
                    int idUltimaMesa = rs.getInt("id_mesa");
                    int numeroUltimaMesa = rs.getInt("numero_mesa");

                    // 2. Eliminar esa mesa
                    String eliminar = "DELETE FROM mesas WHERE id_mesa = ?";
                    try (PreparedStatement stmt2 = conn.prepareStatement(eliminar)) {
                        stmt2.setInt(1, idUltimaMesa);
                        stmt2.executeUpdate();
                    }

                    // 3. Reducir número de las siguientes mesas
                    String actualizar = "UPDATE mesas SET numero_mesa = numero_mesa - 1 WHERE numero_mesa > ?";
                    try (PreparedStatement stmt3 = conn.prepareStatement(actualizar)) {
                        stmt3.setInt(1, numeroUltimaMesa);
                        stmt3.executeUpdate();
                    }

                    conn.commit(); // Confirmar cambios
                }
            } catch (SQLException e) {
                conn.rollback(); // Si falla algo, deshacer
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Guarda las posiciones de las mesas representadas como botones dentro de un Pane.
     * Utiliza el texto del botón como número de mesa.
     */
    public void guardarPosicionesDesdePane(Pane pane) {
        String sql = "UPDATE mesas SET posicionX = ?, posicionY = ? WHERE numero_mesa = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Node node : pane.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    String text = btn.getText().replace("Mesa ", ""); // Extrae el número de mesa
                    int numero = Integer.parseInt(text);
                    double x = btn.getLayoutX();
                    double y = btn.getLayoutY();

                    stmt.setDouble(1, x);
                    stmt.setDouble(2, y);
                    stmt.setInt(3, numero);
                    stmt.addBatch(); // Añadir a lote
                }
            }
            stmt.executeBatch(); // Ejecutar todas las actualizaciones juntas

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica si una mesa tiene un pedido activo (no cerrado).
     */
    public boolean tienePedidoActivo(int mesaId) {
        String sql = "SELECT COUNT(*) FROM pedido WHERE id_mesa = ? AND cerrado = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mesaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Actualiza la posición específica de una mesa mediante su ID.
     */
    public void actualizarPosicionMesa(int idMesa, double x, double y) {
        String sql = "UPDATE mesas SET posicionX = ?, posicionY = ? WHERE id_mesa = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, x);
            stmt.setDouble(2, y);
            stmt.setInt(3, idMesa);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar posición de mesa: " + e.getMessage());
        }
    }
}
