package restaurante.util;

import javafx.scene.control.Alert;

/**
 * Utilidad para mostrar mensajes de alerta en la interfaz gráfica usando JavaFX.
 * Proporciona métodos estáticos para mostrar mensajes de error o información.
 */
public class AlertUtils {

    /**
     * Muestra una alerta de tipo ERROR con el mensaje proporcionado.
     * Se utiliza para notificar al usuario sobre errores o problemas en la ejecución.
     *
     * @param mensaje Texto que se mostrará en la ventana de alerta.
     */
    public static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // Tipo de alerta: Error
        alert.setTitle("Error");                         // Título de la ventana
        alert.setHeaderText(null);                       // Encabezado desactivado
        alert.setContentText(mensaje);                   // Mensaje principal
        alert.showAndWait();                             // Mostrar alerta y esperar confirmación
    }

    /**
     * Muestra una alerta de tipo INFORMATION con el mensaje proporcionado.
     * Útil para mostrar mensajes informativos o confirmaciones.
     *
     * @param mensaje Texto que se mostrará en la ventana de alerta.
     */
    public static void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Tipo de alerta: Información
        alert.setTitle("Información");                        // Título de la ventana
        alert.setHeaderText(null);                            // Encabezado desactivado
        alert.setContentText(mensaje);                        // Mensaje principal
        alert.showAndWait();                                  // Mostrar alerta y esperar confirmación
    }
}
