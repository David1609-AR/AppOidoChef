package restaurante.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import restaurante.models.Producto;
import restaurante.services.ProductoService;

/**
 * Controlador encargado de gestionar la creación, edición, eliminación y visualización
 * de productos en el sistema. Usa JavaFX para manipular una interfaz gráfica.
 */
public class ProductoController {

    // Elementos de la interfaz (definidos en el archivo FXML)
    @FXML private TableView<Producto> tablaProductos;     // Tabla para mostrar los productos
    @FXML private TextField txtNombre;                    // Campo de texto para el nombre del producto
    @FXML private TextField txtDescripcion;               // Campo de texto para la descripción
    @FXML private TextField txtPrecio;                    // Campo de texto para el precio
    @FXML private ComboBox<String> cbCategoria;           // ComboBox para seleccionar la categoría
    @FXML private CheckBox chkTieneIva;                   // Checkbox para indicar si tiene IVA

    // Botones de acción
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    // Servicio que maneja la lógica relacionada con productos
    private final ProductoService productoService = new ProductoService();

    // Lista observable para actualizar automáticamente la tabla
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    // Producto actualmente seleccionado para edición
    private Producto productoSeleccionado = null;

    /**
     * Método que se llama automáticamente al iniciar el controlador.
     * Inicializa la tabla, carga categorías y desactiva el formulario.
     */
    @FXML
    public void initialize() {
        tablaProductos.setItems(listaProductos);

        // Cargar las categorías predefinidas al ComboBox
        cbCategoria.setItems(FXCollections.observableArrayList(
                "Entrantes", "Ensaladas", "Platos principales", "Carne", "Pesca",
                "Postres", "Bebidas", "Refrescos", "Vinos", "Botella vino"
        ));

        // Cargar productos desde el servicio
        cargarProductos();

        // Deshabilitar el formulario de entrada inicialmente
        deshabilitarFormulario();
    }

    /**
     * Carga los productos desde la base de datos a la tabla.
     */
    private void cargarProductos() {
        listaProductos.setAll(productoService.obtenerTodos());
    }

    /**
     * Prepara el formulario para crear un nuevo producto.
     */
    @FXML
    private void nuevoProducto() {
        productoSeleccionado = null;
        habilitarFormulario();
        limpiarFormulario();
    }

    /**
     * Permite editar el producto actualmente seleccionado en la tabla.
     */
    @FXML
    private void editarProducto() {
        productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            habilitarFormulario();

            // Cargar datos del producto al formulario
            txtNombre.setText(productoSeleccionado.getNombre());
            txtDescripcion.setText(productoSeleccionado.getDescripcion());
            txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecio()));
            cbCategoria.setValue(productoSeleccionado.getCategoria());
            chkTieneIva.setSelected(productoSeleccionado.isTieneIva());
        } else {
            mostrarAlerta("Selecciona un producto para editar.");
        }
    }

    /**
     * Elimina el producto seleccionado de la tabla y la base de datos.
     */
    @FXML
    private void eliminarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            if (productoService.eliminarPorId(seleccionado.getId())) {
                mostrarAlerta("Producto eliminado correctamente.");
                cargarProductos();
            } else {
                mostrarAlerta("Error al eliminar el producto.");
            }
        } else {
            mostrarAlerta("Selecciona un producto para eliminar.");
        }
    }

    /**
     * Guarda un nuevo producto o actualiza uno existente, según el contexto.
     */
    @FXML
    private void guardarProducto() {
        // Obtener valores del formulario
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String categoria = cbCategoria.getValue();
        boolean tieneIva = chkTieneIva.isSelected();

        // Validar campos obligatorios
        if (nombre.isEmpty() || precioStr.isEmpty() || categoria == null) {
            mostrarAlerta("Por favor, completa los campos obligatorios.");
            return;
        }

        // Validar que el precio sea un número válido
        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            mostrarAlerta("El precio debe ser un número válido.");
            return;
        }

        // Crear nuevo o actualizar producto
        if (productoSeleccionado == null) {
            Producto nuevo = new Producto(0, nombre, descripcion, precio, categoria, tieneIva);
            if (productoService.crearProducto(nuevo)) {
                mostrarAlerta("Producto creado correctamente.");
            } else {
                mostrarAlerta("Error al crear el producto.");
            }
        } else {
            productoSeleccionado.setNombre(nombre);
            productoSeleccionado.setDescripcion(descripcion);
            productoSeleccionado.setPrecio(precio);
            productoSeleccionado.setCategoria(categoria);
            productoSeleccionado.setTieneIva(tieneIva);

            if (productoService.actualizarProducto(productoSeleccionado)) {
                mostrarAlerta("Producto actualizado correctamente.");
            } else {
                mostrarAlerta("Error al actualizar el producto.");
            }
        }

        deshabilitarFormulario();
        cargarProductos();
    }

    /**
     * Cancela la edición o creación de un producto.
     */
    @FXML
    private void cancelarEdicion() {
        deshabilitarFormulario();
        limpiarFormulario();
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        cbCategoria.setValue(null);
        chkTieneIva.setSelected(false);
    }

    /**
     * Habilita el formulario para permitir edición o creación.
     */
    private void habilitarFormulario() {
        txtNombre.setDisable(false);
        txtDescripcion.setDisable(false);
        txtPrecio.setDisable(false);
        cbCategoria.setDisable(false);
        chkTieneIva.setDisable(false);
        btnGuardar.setDisable(false);
        btnCancelar.setDisable(false);
    }

    /**
     * Desactiva los campos del formulario.
     */
    private void deshabilitarFormulario() {
        txtNombre.setDisable(true);
        txtDescripcion.setDisable(true);
        txtPrecio.setDisable(true);
        cbCategoria.setDisable(true);
        chkTieneIva.setDisable(true);
        btnGuardar.setDisable(true);
        btnCancelar.setDisable(true);
    }

    /**
     * Muestra una alerta simple con el mensaje indicado.
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
