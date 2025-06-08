package restaurante.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import restaurante.models.Usuario;
import restaurante.services.ProductoService;

/**
 * Controlador para la gestión de usuarios desde la interfaz JavaFX.
 * Permite listar, crear y eliminar usuarios (usado para login del sistema).
 */
public class UsuarioController {

    // Servicio reutilizado que incluye métodos de gestión de usuarios
    private final ProductoService productoService = new ProductoService();

    // Lista observable para reflejar automáticamente cambios en la tabla
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    // Elementos visuales definidos en el archivo FXML
    @FXML private TableView<Usuario> tablaUsuarios;          // Tabla para mostrar usuarios
    @FXML private TableColumn<Usuario, Integer> colId;       // Columna ID del usuario
    @FXML private TableColumn<Usuario, String> colUsername;  // Columna nombre de usuario

    @FXML private TextField usernameField;                   // Campo para introducir nombre de usuario
    @FXML private PasswordField passwordField;               // Campo para introducir contraseña
    @FXML private Label statusLabel;                         // Etiqueta para mostrar estado de la acción

    /**
     * Método que se ejecuta al iniciar el controlador.
     * Configura las columnas de la tabla y carga los usuarios existentes.
     */
    @FXML
    public void initialize() {
        // Configurar columnas de la tabla con propiedades del modelo Usuario
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colUsername.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));

        // Enlazar la tabla con la lista observable
        tablaUsuarios.setItems(usuarios);

        // Cargar usuarios desde la base de datos
        cargarUsuarios();
    }

    /**
     * Carga todos los usuarios disponibles desde el servicio y los muestra en la tabla.
     */
    private void cargarUsuarios() {
        usuarios.setAll(productoService.obtenerUsuarios());
    }

    /**
     * Guarda un nuevo usuario en la base de datos si el nombre no existe aún.
     */
    @FXML
    public void guardarUsuario() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validación básica: no permitir campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("❌ Campos vacíos");
            return;
        }

        // Validación: evitar duplicados por nombre de usuario
        if (productoService.existeUsuario(username)) {
            statusLabel.setText("⚠️ Ya existe");
            return;
        }

        // Intentar insertar el nuevo usuario
        boolean creado = productoService.insertarUsuario(username, password);
        statusLabel.setText(creado ? "✅ Usuario creado" : "❌ Error");

        // Refrescar tabla y limpiar formulario
        cargarUsuarios();
        usernameField.clear();
        passwordField.clear();
    }

    /**
     * Elimina el usuario actualmente seleccionado en la tabla.
     */
    @FXML
    public void eliminarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        // Validación: asegurarse de que hay un usuario seleccionado
        if (seleccionado == null) {
            statusLabel.setText("⚠️ Selecciona uno");
            return;
        }

        // Eliminar usuario y actualizar tabla
        boolean eliminado = productoService.eliminarUsuario(seleccionado.getId());
        statusLabel.setText(eliminado ? "✅ Eliminado" : "❌ Error");
        cargarUsuarios();
    }
}
