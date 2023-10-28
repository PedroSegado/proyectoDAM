package com.pasegados.labo.calibraciones;

import java.util.Locale;
import com.pasegados.labo.App;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.modelos.Filtros;
import com.pasegados.labo.modelos.Patron;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;


/**
 * Esta clase representa al controlador del Editor de Patrones, al que se accede cada vez que se desea crear un
 * nuevo Patrón o modificar uno existente
 * 
 * @author Pedro Antonio Segado Solano
 */
public class EditorPatronControlador {

    // Label
    @FXML
    private Label lbTitulo;

    // TextFields
    @FXML
    private TextField tfNombre;
    @FXML
    private TextField tfConcentracion;
    
    // DatePicker
    @FXML
    private DatePicker dpFecha;
    
    // Botones
    @FXML
    private Button btCancelar;
    @FXML
    private Button btAceptar;
   
    // Otras variables necesarias
    private Stage stage;
    private Patron patronDuplicado;
    private Patron patronOriginal;
    private boolean aceptarPulsado = false;    
    
    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {
        // Filtro de textfield concentracion para que admita entre 0 y 100 con decimales
        tfConcentracion.setTextFormatter(new TextFormatter<>(Filtros.getDecimalFilter(100)));
    }

    // Pulsamos el botón cancelar para rechazar cualquier tipo de cambios o trabajo realizado
    @FXML
    private void cancelar(ActionEvent event) {
        stage.close();
    }

    // Pulsamos el botón aceptar y verificamos que tenemos la información necesaria para crear/modificar un Patrón
    @FXML
    private void aceptar(ActionEvent event) {
        if (isInputValid()) { // Si todos los campos necesarios y validaciones son correctas
            // Ajustamos los atributos del objeto original con lo indicados en pantalla sobre el duplicado
            patronOriginal.setNombre(patronDuplicado.getNombre());
            patronOriginal.setFecha(patronDuplicado.getFecha());
            patronOriginal.setConcentracion(patronDuplicado.getConcentracion());

            aceptarPulsado = true; // Esta variable sirve para ver desde otras clases que hemos pulsado "Aceptar"
            //Cerramos la ventana del editor de patron
            stage.close();
        }
    }

    //OTROS METODOS NO GENERADOS EN EL FXML
    
    // Validación de los campos del formulario, si no están completos se avisa con una Alerta al usuario
    private boolean isInputValid() {
        String mensajeMostrar = ""; // Preparamos un String para mostrar en la alerta al usuario

        if (tfNombre.getText() == null || tfNombre.getText().length() == 0) { // No hay nombre
            mensajeMostrar += "Debes seleccionar un nombre para el patrón\n";
        }

        for (Patron p : App.getControladorPrincipal().getListaPatrones()) { // Comprobamos que no exista el nombre por ser PK
            if (p.getNombre().equals(tfNombre.getText()) & !patronOriginal.getNombre().equals(patronDuplicado.getNombre())) { // Si estamos modificando si se permite                                                                                                
                mensajeMostrar += "Ya existe un patrón con ese nombre";                                                       // el mismo nombre
            }
        }

        if (tfConcentracion.getText() == null || tfConcentracion.getText().length() == 0) { // No hay concentracion de azufre
            mensajeMostrar += "Debes seleccionar una concentración en % para el patrón\n";
        }

        // El resto de campos se podrían dejar a null si se desconocen
        if (mensajeMostrar.length() == 0) {
            return true;

        } else {
            Alertas.alertaErrorFormulario(mensajeMostrar);
            return false;
        }
    }
    
    /**
     * Recibe un Patrón y lo usa como Patrón Original, y lo clona sobre un Patrón Duplicado sobre el que realizaremos
     * las modificaciones oportunas, preservando intacto el original.       * 
     * @param patron Patron con el que trabajar en el formulario
     */
    public void setPatron(Patron patron) {
        this.patronOriginal = patron;
        this.patronDuplicado = (Patron) patron.clone();

        // Binding de los campos del editor con los atributos del Patrón Duplicado
        tfNombre.textProperty().bindBidirectional(this.patronDuplicado.nombreProperty());
        dpFecha.valueProperty().bindBidirectional(this.patronDuplicado.fechaProperty());
        tfConcentracion.textProperty().bindBidirectional(this.patronDuplicado.concentracionProperty(), new NumberStringConverter(Locale.US, "0.0000")); // Forzamos usar puntos en vez de comas en decimales
        if (tfConcentracion.textProperty().getValue().equals("0.0000")) {
            tfConcentracion.setText("");
        }
    }

    /**
     * Este método establece el escenario sobre el que trabajar
     * @param stage
     */
    public void setStage(Stage stage) {        
        this.stage = stage;
    }

    /**
     * Este método devuelve si los cambios sobre el formulario del Patrón son aceptados o no     * 
     * @return boolean true si el usuario pulsa aceptar, o false si pulsa cancelar o cierra la ventana
     */
    public boolean sePulsaAceptar() {
        
        return aceptarPulsado;
    }

    /**
     * Este método da título al editor según sea un Patrón nuevo o modificar uno existente     * 
     * @param titulo String con el título de la ventana del formulario
     */
    public void setTitulo(String titulo) {        
        lbTitulo.setText(titulo);
    }
}
