package restaurante.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import restaurante.util.DatabaseConnection;
import restaurante.util.DatabaseConnection;

public class UsuarioService {

    /**
     * Verifica si el usuario y la contraseña existen en la base de datos.
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return true si las credenciales son válidas, false si no.
     */
    public boolean autenticarUsuario(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // devuelve true si encontró una coincidencia
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al autenticar usuario:");
            e.printStackTrace();
            return false;
        }
    }
}

