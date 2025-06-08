package restaurante.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import restaurante.models.Producto;
import restaurante.services.ProductoService;

/**
 * Controlador encargado de agregar, editar y eliminar productos desde una tabla.
 */
public class AgregarController {

    // Servicio que gestiona operaciones sobre productos
    private final ProductoService productoService = new ProductoService();

    // Lista observable para mostrar productos en la tabla
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    // Producto actualmente seleccionado para edición
    private Producto productoSeleccionado;

    // Columnas de la tabla
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> id;
    @FXML private TableColumn<Producto, String> nombre;
    @FXML private TableColumn<Producto, String> descripcion;
    @FXML private TableColumn<Producto, Double> precio;
    @FXML private TableColumn<Producto, String> categoria;
    @FXML private TableColumn<Producto, Boolean> tieneIva;

    // Campos del formulario de producto
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private CheckBox chkTieneIva;
    @FXML private Label productoStatus;

    // Botones del formulario
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    /**
     * Inicializa el controlador al cargar la vista.
     * Configura columnas, categorías y productos iniciales.
     */
    @FXML
    public void initialize() {
        // Configurar columnas de la tabla con propiedades del modelo Producto
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        descripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        precio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        categoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        tieneIva.setCellValueFactory(new PropertyValueFactory<>("tieneIva"));

        // Vincular lista observable a la tabla
        tablaProductos.setItems(productos);

        // Cargar productos existentes
        cargarProductos();

        // Cargar categorías disponibles al ComboBox
        cbCategoria.getItems().addAll(
                "Entrantes", "Ensaladas", "Platos principales", "Carnes", "Pescados",
                "Sugerencias", "Postres", "Bebidas", "Refrescos", "Vinos", "Botella vino"
        );
    }

    /**
     * Carga los productos desde la base de datos y actualiza la tabla.
     */
    private void cargarProductos() {
        productos.setAll(productoService.obtenerTodos());
    }

    /**
     * Guarda un nuevo producto o actualiza uno existente.
     * Valida nombre, precio y categoría.
     */
    @FXML
    public void guardarProducto() {
        try {
            String nombre = txtNombre.getText();
            String descripcion = txtDescripcion.getText();
            double precio = Double.parseDouble(txtPrecio.getText());
            String categoria = cbCategoria.getValue();
            boolean tieneIva = chkTieneIva.isSelected();

            if (nombre.isEmpty() || categoria == null) {
                productoStatus.setText("❌ Nombre y categoría obligatorios");
                return;
            }

            Producto nuevo = new Producto(
                    productoSeleccionado != null ? productoSeleccionado.getId() : 0,
                    nombre, descripcion, precio, categoria, tieneIva
            );

            // Crear o actualizar según contexto
            boolean exito = (productoSeleccionado == null)
                    ? productoService.crearProducto(nuevo)
                    : productoService.actualizarProducto(nuevo);

            productoStatus.setText(exito
                    ? (productoSeleccionado == null ? "✅ Producto creado" : "✅ Producto actualizado")
                    : "❌ Error al guardar");

            limpiarFormulario();
            cargarProductos();

        } catch (NumberFormatException e) {
            productoStatus.setText("❌ Precio inválido");
        }
    }

    /**
     * Cancela la edición actual y limpia el formulario.
     */
    @FXML
    public void cancelarEdicion() {
        limpiarFormulario();
        productoStatus.setText("Edición cancelada");
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        cbCategoria.getSelectionModel().clearSelection();
        chkTieneIva.setSelected(false);
        productoSeleccionado = null;
    }

    /**
     * Prepara el formulario para ingresar un nuevo producto.
     */
    @FXML
    public void nuevoProducto() {
        limpiarFormulario();
        productoStatus.setText("Nuevo producto");
    }

    /**
     * Carga los datos del producto seleccionado para editar.
     */
    @FXML
    public void editarProducto() {
        productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            productoStatus.setText("⚠️ Selecciona un producto");
            return;
        }

        txtNombre.setText(productoSeleccionado.getNombre());
        txtDescripcion.setText(productoSeleccionado.getDescripcion());
        txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecio()));
        cbCategoria.setValue(productoSeleccionado.getCategoria());
        chkTieneIva.setSelected(productoSeleccionado.isTieneIva());
        productoStatus.setText("Editando producto");
    }

    /**
     * Elimina el producto actualmente seleccionado de la tabla y la base de datos.
     */
    @FXML
    public void eliminarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            productoStatus.setText("⚠️ Selecciona un producto");
            return;
        }

        boolean eliminado = productoService.eliminarPorId(seleccionado.getId());
        productoStatus.setText(eliminado ? "✅ Producto eliminado" : "❌ Error al eliminar");
        cargarProductos();
    }
}
