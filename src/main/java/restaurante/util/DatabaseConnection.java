package restaurante.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para obtener conexiones a la base de datos MySQL.
 * Centraliza la configuración de conexión para facilitar su reutilización.
 */
public class DatabaseConnection {

    // URL de conexión JDBC a la base de datos MySQL.
    // Incluye dirección IP, puerto (3306 por defecto) y nombre de la base de datos.
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/restaurante";

    // Usuario con permisos de acceso a la base de datos.
    private static final String USER = "root";

    // Contraseña correspondiente al usuario de la base de datos.
    private static final String PASSWORD = "1234";

    /**
     * Método estático que devuelve una nueva conexión a la base de datos.
     *
     * @return una instancia de java.sql.Connection lista para ser usada.
     * @throws SQLException si ocurre un error al intentar establecer la conexión.
     */
    public static Connection getConnection() throws SQLException {
        // Se usa el DriverManager para crear y devolver una conexión con los parámetros definidos.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
