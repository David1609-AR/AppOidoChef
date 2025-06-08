package restaurante.api;

import restaurante.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioAPI {

    public static boolean validarCredenciales(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Recomendado: usar hash
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // True si encuentra usuario
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
