package restaurante.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import restaurante.models.Reserva;
import restaurante.services.ReservaService;
import restaurante.util.AlertUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para visualizar, filtrar y cancelar reservas existentes.
 */
public class VerReservasController {

    // Elementos de la interfaz gráfica (FXML)
    @FXML private TableView<Reserva> reservasTable;               // Tabla principal
    @FXML private TableColumn<Reserva, Integer> idColumn;         // Columna ID de la reserva
    @FXML private TableColumn<Reserva, String> nombreColumn;      // Columna nombre del cliente
    @FXML private TableColumn<Reserva, Integer> mesaColumn;       // Columna número de mesa
    @FXML private TableColumn<Reserva, String> fechaColumn;       // Columna fecha formateada
    @FXML private TableColumn<Reserva, String> estadoColumn;      // Columna estado de la reserva
    @FXML private DatePicker fechaFiltro;                         // Filtro por fecha
    @FXML private TextField busquedaField;                        // Filtro por nombre o mesa

    // Servicio para interactuar con la base de datos
    private ReservaService reservaService = new ReservaService();

    /**
     * Método llamado automáticamente al cargar el controlador.
     * Configura las columnas de la tabla, filtros y carga los datos iniciales.
     */
    @FXML
    public void initialize() {
        // Configuración de columnas para reflejar las propiedades del objeto Reserva
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        mesaColumn.setCellValueFactory(new PropertyValueFactory<>("idMesa"));

        // Columna con fecha formateada usando binding personalizado
        fechaColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cellData.getValue().getFechaReservaFormateada()));

        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Establecer la fecha actual como valor inicial del filtro de fecha
        fechaFiltro.setValue(LocalDate.now());

        // Cargar reservas en la tabla
        cargarReservas();

        // Agregar listeners a los filtros (se ejecutan cuando el usuario cambia fecha o texto)
        fechaFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        busquedaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    /**
     * Carga todas las reservas desde el servicio y las muestra en la tabla.
     */
    private void cargarReservas() {
        List<Reserva> reservas = reservaService.obtenerTodasReservas();
        reservasTable.setItems(FXCollections.observableArrayList(reservas));
    }

    /**
     * Aplica los filtros definidos por fecha y texto sobre la lista de reservas.
     */
    private void aplicarFiltros() {
        List<Reserva> reservasFiltradas = reservaService.obtenerTodasReservas()
                .stream()
                .filter(this::cumpleFiltroFecha)      // Filtro por fecha
                .filter(this::cumpleFiltroBusqueda)   // Filtro por nombre o mesa
                .collect(Collectors.toList());

        reservasTable.setItems(FXCollections.observableArrayList(reservasFiltradas));
    }

    // Verifica si la reserva coincide con la fecha seleccionada (o todas si no hay fecha)
    private boolean cumpleFiltroFecha(Reserva reserva) {
        LocalDate fechaFiltro = this.fechaFiltro.getValue();
        return fechaFiltro == null ||
                reserva.getFechaReserva().toLocalDate().equals(fechaFiltro);
    }

    // Verifica si el nombre del cliente o el número de mesa coinciden con el texto introducido
    private boolean cumpleFiltroBusqueda(Reserva reserva) {
        String busqueda = busquedaField.getText().toLowerCase();
        return busqueda.isEmpty() ||
                reserva.getNombre().toLowerCase().contains(busqueda) ||
                String.valueOf(reserva.getIdMesa()).contains(busqueda);
    }

    /**
     * Permite cancelar la reserva actualmente seleccionada en la tabla.
     * Actualiza la interfaz y muestra mensaje de éxito o error.
     */
    @FXML
    private void handleCancelarReserva() {
        Reserva seleccionada = reservasTable.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.mostrarError("Por favor seleccione una reserva");
            return;
        }

        if (reservaService.cancelarReserva(seleccionada.getIdReserva())) {
            AlertUtils.mostrarInfo("Reserva cancelada exitosamente");
            cargarReservas(); // Refrescar la tabla tras cancelar
        } else {
            AlertUtils.mostrarError("Error al cancelar la reserva");
        }
    }
}
