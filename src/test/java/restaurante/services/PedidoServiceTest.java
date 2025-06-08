package restaurante.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restaurante.api.ItemPedidoAPI;
import restaurante.api.PedidoAPI;
import restaurante.models.Pedido;
import restaurante.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class PedidoServiceTest {

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService();

        // Prepara una mesa y un producto con ID conocidos para las pruebas
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Insertar mesa con id 100 si no existe
            String sqlMesa = "INSERT IGNORE INTO mesas (id_mesa, numero_mesa, posicionX, posicionY, ocupada, bloqueada) " +
                    "VALUES (100, 10, 50, 50, false, false)";
            conn.prepareStatement(sqlMesa).executeUpdate();

            // Insertar producto con id 200 si no existe
            String sqlProducto = "INSERT IGNORE INTO productos (id, nombre, descripcion, precio, categoria, tiene_iva) " +
                    "VALUES (200, 'Test Café', 'Café para test', 1.2, 'Bebidas', true)";
            conn.prepareStatement(sqlProducto).executeUpdate();

        } catch (SQLException e) {
            fail("Error preparando datos de prueba: " + e.getMessage());
        }
    }

    @Test
    void guardarPedidoDesdeAPI_deberiaInsertarPedidoCorrectamente() {
        PedidoAPI pedidoAPI = new PedidoAPI(
                100, // ID de mesa preparado
                2,
                Collections.singletonList(new ItemPedidoAPI(200, "Test Café", 1.2, 1))
        );

        Pedido resultado = pedidoService.guardarPedidoDesdeAPI(pedidoAPI);
        assertNotNull(resultado);
        assertEquals(100, resultado.getMesaId());
        assertEquals(1, resultado.getItems().size());
        assertEquals("Test Café", resultado.getItems().get(0).getProducto().getNombre());
    }

    @Test
    void guardarPedidoDesdeAPI_conPedidoInvalido_deberiaRetornarNull() {
        PedidoAPI pedidoAPI = new PedidoAPI(); // pedido vacío
        Pedido resultado = pedidoService.guardarPedidoDesdeAPI(pedidoAPI);
        assertNull(resultado);
    }

    @Test
    void eliminarPorMesaId_deberiaNoLanzarExcepcion() {
        assertDoesNotThrow(() -> pedidoService.eliminarPorMesaId(100));
    }
}
