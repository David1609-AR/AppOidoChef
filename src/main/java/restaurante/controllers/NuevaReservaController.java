package restaurante.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import restaurante.models.Mesa;
import restaurante.models.Reserva;
import restaurante.services.PedidoService;
import restaurante.services.ReservaService;
import restaurante.util.AlertUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controlador encargado de gestionar la creación de nuevas reservas y mostrar las existentes.
 */
public class NuevaReservaController {

    // Campos del formulario para introducir los datos de la reserva
    @FXML private TextField nombreField;               // Campo para el nombre del cliente
    @FXML private TextField telefonoField;             // Campo para el teléfono del cliente
    @FXML private TextField emailField;                // Campo para el correo electrónico del cliente
    @FXML private Spinner<Integer> personasSpinner;    // Spinner para seleccionar el número de personas
    @FXML private ComboBox<Mesa> mesaCombo;            // ComboBox para seleccionar una mesa
    @FXML private DatePicker fechaPicker;              // Selector de fecha para la reserva
    @FXML private ComboBox<String> horaCombo;          // ComboBox para seleccionar la hora

    // Tabla para mostrar las reservas existentes y sus columnas
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colNombre;
    @FXML private TableColumn<Reserva, String> colFecha;
    @FXML private TableColumn<Reserva, String> colHora;
    @FXML private TableColumn<Reserva, Integer> colPersonas;
    @FXML private TableColumn<Reserva, Integer> colMesa;

    // Servicios para acceder a la base de datos
    private final PedidoService pedidoService = new PedidoService();
    private final ReservaService reservaService = new ReservaService();
    private List<Mesa> mesas = new ArrayList<>();

    /**
     * Método que se ejecuta al iniciar el controlador.
     * Configura la tabla, carga las mesas y las reservas existentes.
     */
    @FXML
    public void initialize() {
        // Configurar columnas de la tabla de reservas
        colNombre.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getNombre()));
        colFecha.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFechaReservaFormateada()));
        colHora.setCellValueFactory(cell -> {
            LocalDateTime fecha = cell.getValue().getFechaReserva();
            return new ReadOnlyStringWrapper(fecha != null ? fecha.toLocalTime().toString() : "");
        });
        colPersonas.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getNumPersonas()).asObject());
        colMesa.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getIdMesa()).asObject());

        // Inicializar el Spinner para el número de personas (de 1 a 20, valor inicial 2)
        personasSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));

        // Obtener la lista de mesas desde el servicio
        mesas = pedidoService.obtenerMesasModelo();
        mesaCombo.setItems(FXCollections.observableArrayList(mesas));

        // Configurar cómo se muestra cada mesa en el ComboBox
        mesaCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Mesa item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "Mesa " + item.getNumeroMesa());
            }
        });
        mesaCombo.setButtonCell(mesaCombo.getCellFactory().call(null));

        // Agregar las horas disponibles (de 13:00 a 23:00 en intervalos de 30 minutos) al ComboBox de horas
        horaCombo.setItems(FXCollections.observableArrayList(
                IntStream.range(13, 23).boxed()
                        .flatMap(h -> List.of(String.format("%02d:00", h), String.format("%02d:30", h)).stream())
                        .collect(Collectors.toList())
        ));

        // Cargar reservas existentes en la tabla
        cargarReservas();
    }

    /**
     * Método llamado al pulsar el botón de crear reserva.
     * Toma los valores del formulario, los valida y crea una nueva reserva si todo está correcto.
     */
    @FXML
    private void handleCrearReserva() {
        try {
            // Obtener valores del formulario
            String nombre = nombreField.getText().trim();
            String telefono = telefonoField.getText().trim();
            String email = emailField.getText().trim();
            int personas = personasSpinner.getValue();
            Mesa mesaSeleccionada = mesaCombo.getValue();
            LocalDate fecha = fechaPicker.getValue();
            String hora = horaCombo.getValue();

            // Validar que todos los campos estén completos
            if (nombre.isEmpty() || telefono.isEmpty() || email.isEmpty() ||
                    mesaSeleccionada == null || fecha == null || hora == null) {
                AlertUtils.mostrarError("Todos los campos deben estar completos.");
                return;
            }

            // Crear la fecha y hora completa de la reserva
            LocalDateTime fechaReserva = LocalDateTime.of(fecha, LocalTime.parse(hora));
            String estado = "ACTIVA";

            // Crear un objeto Reserva con los datos del formulario
            Reserva nueva = new Reserva(nombre, telefono, email, personas,
                    mesaSeleccionada.getIdMesa(), fechaReserva, estado);

            // Guardar la reserva en la base de datos y actualizar la interfaz
            if (reservaService.crearReserva(nueva)) {
                AlertUtils.mostrarInfo("Reserva creada correctamente.");
                cargarReservas();     // Refrescar tabla
                limpiarFormulario();  // Vaciar los campos del formulario
            } else {
                AlertUtils.mostrarError("No se pudo crear la reserva.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.mostrarError("Error: " + e.getMessage());
        }
    }

    /**
     * Carga todas las reservas desde la base de datos y las muestra en la tabla.
     */
    private void cargarReservas() {
        tablaReservas.setItems(FXCollections.observableArrayList(reservaService.obtenerTodasReservas()));
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        nombreField.clear();
        telefonoField.clear();
        emailField.clear();
        personasSpinner.getValueFactory().setValue(2);
        mesaCombo.setValue(null);
        fechaPicker.setValue(null);
        horaCombo.setValue(null);
    }
}
