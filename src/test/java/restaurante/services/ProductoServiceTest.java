package restaurante.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restaurante.models.Producto;
import restaurante.services.ProductoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoServiceTest {

    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService();
    }

    @Test
    void obtenerTodos_deberiaRetornarProductos() {
        List<Producto> productos = productoService.obtenerTodos();
        assertNotNull(productos);
    }

    @Test
    void buscarPorId_inexistente_deberiaRetornarNull() {
        Producto producto = productoService.buscarPorId(-1);
        assertNull(producto);
    }

    @Test
    void crearYEliminarProducto_deberiaCrearYEliminarCorrectamente() {
        Producto nuevo = new Producto(0, "TestProducto", "Prueba", 9.99, "Test", false);
        boolean creado = productoService.crearProducto(nuevo);
        assertTrue(creado);

        // Obtener todos para buscar el recién creado
        List<Producto> productos = productoService.obtenerTodos();
        Producto creadoProducto = productos.stream()
                .filter(p -> "TestProducto".equals(p.getNombre()))
                .findFirst()
                .orElse(null);

        assertNotNull(creadoProducto);
        assertTrue(productoService.eliminarPorId(creadoProducto.getId()));
    }

    @Test
    void obtenerAgrupadosPorCategoria_deberiaDevolverMapa() {
        var mapa = productoService.obtenerAgrupadosPorCategoria();
        assertNotNull(mapa);
        assertFalse(mapa.isEmpty());
    }
}
