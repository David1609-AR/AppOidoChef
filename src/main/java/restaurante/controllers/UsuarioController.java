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

    private final ProductoService productoService = new ProductoService();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colPassword;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colUsername.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        colPassword.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPassword()));

        tablaUsuarios.setItems(usuarios);
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        usuarios.setAll(productoService.obtenerUsuarios());
    }

    @FXML
    public void guardarUsuario() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("❌ Campos vacíos");
            return;
        }

        if (productoService.existeUsuario(username)) {
            statusLabel.setText("⚠️ Ya existe");
            return;
        }

        boolean creado = productoService.insertarUsuario(username, password);
        statusLabel.setText(creado ? "✅ Usuario creado" : "❌ Error");

        cargarUsuarios();
        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    public void eliminarUsuario() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            statusLabel.setText("⚠️ Selecciona uno");
            return;
        }

        boolean eliminado = productoService.eliminarUsuario(seleccionado.getId());
        statusLabel.setText(eliminado ? "✅ Eliminado" : "❌ Error");
        cargarUsuarios();
    }
}
