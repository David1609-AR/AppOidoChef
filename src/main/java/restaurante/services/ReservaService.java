package restaurante.services;

import restaurante.models.Reserva;
import restaurante.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static restaurante.util.DatabaseConnection.getConnection;

/**
 * Servicio encargado de gestionar las operaciones relacionadas con las reservas en la base de datos.
 */
public class ReservaService {

    /**
     * Inserta una nueva reserva en la base de datos.
     * @param reserva Objeto Reserva con la información a guardar.
     * @return true si la operación fue exitosa.
     */
    public boolean crearReserva(Reserva reserva) {
        String query = "INSERT INTO reservas (nombre, telefono, email, num_personas, mesa_id, fecha_reserva, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Establecer los valores del objeto en el statement
            statement.setString(1, reserva.getNombre());
            statement.setString(2, reserva.getTelefono());
            statement.setString(3, reserva.getEmail());
            statement.setInt(4, reserva.getNumPersonas());
            statement.setInt(5, reserva.getIdMesa());
            statement.setTimestamp(6, Timestamp.valueOf(reserva.getFechaReserva()));
            statement.setString(7, reserva.getEstado());

            int result = statement.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todas las reservas almacenadas.
     * @return Lista de objetos Reserva.
     */
    public List<Reserva> obtenerTodasReservas() {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reservas";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reserva reserva = new Reserva(
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getInt("num_personas"),
                        rs.getInt("mesa_id"),
                        rs.getTimestamp("fecha_reserva").toLocalDateTime(),
                        rs.getString("estado")
                );
                reserva.setIdReserva(rs.getInt("id"));
                reservas.add(reserva);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservas;
    }

    /**
     * Cambia el estado de una reserva a "CANCELADA".
     * @param idReserva ID de la reserva a cancelar.
     * @return true si se actualizó correctamente.
     */
    public boolean cancelarReserva(int idReserva) {
        String sql = "UPDATE reservas SET estado = 'CANCELADA' WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idReserva);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todas las reservas correspondientes a una fecha específica.
     * @param fecha Fecha (con hora) que se desea consultar.
     * @return Lista de reservas para la fecha dada.
     */
    public List<Reserva> obtenerReservasPorFecha(LocalDateTime fecha) {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE DATE(fecha_reserva) = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(fecha.toLocalDate()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reserva reserva = new Reserva();
                    reserva.setIdReserva(rs.getInt("id"));
                    reserva.setNombre(rs.getString("nombre"));
                    reserva.setTelefono(rs.getString("telefono"));
                    reserva.setEmail(rs.getString("email"));
                    reserva.setNumPersonas(rs.getInt("num_personas"));
                    reserva.setIdMesa(rs.getInt("mesa_id"));
                    reserva.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
                    reserva.setEstado(rs.getString("estado"));
                    reservas.add(reserva);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservas;
    }

    /**
     * Obtiene una lista de IDs de mesas que tienen reservas activas en una fecha específica.
     * @param fecha Fecha que se desea verificar.
     * @return Lista de IDs de mesas reservadas.
     */
    public List<Integer> obtenerMesasReservadas(LocalDate fecha) {
        List<Integer> mesasReservadas = new ArrayList<>();
        String sql = "SELECT mesa_id FROM reservas WHERE estado = 'ACTIVA' AND DATE(fecha_reserva) = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mesasReservadas.add(rs.getInt("mesa_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mesasReservadas;
    }

    /**
     * Obtiene una reserva específica por ID de mesa y fecha.
     * @param idMesa ID de la mesa.
     * @param fecha Fecha a consultar.
     * @return Reserva correspondiente o null si no existe.
     */
    public Reserva obtenerReservaPorMesaYFecha(int idMesa, LocalDate fecha) {
        String sql = "SELECT * FROM reservas WHERE mesa_id = ? AND DATE(fecha_reserva) = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMesa);
            stmt.setDate(2, java.sql.Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reserva reserva = new Reserva();
                    reserva.setIdReserva(rs.getInt("id"));
                    reserva.setNombre(rs.getString("nombre"));
                    reserva.setTelefono(rs.getString("telefono"));
                    reserva.setEmail(rs.getString("email"));
                    reserva.setNumPersonas(rs.getInt("num_personas"));
                    reserva.setIdMesa(rs.getInt("mesa_id"));
                    reserva.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
                    reserva.setEstado(rs.getString("estado"));
                    return reserva;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
