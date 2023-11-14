package com.pasegados.labo.configuracion;

import com.pasegados.labo.App;
import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.modelos.Filtros;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;


/**
 * Esta clase representa al controlador del Editor de Ajustes, al que se accede cada vez que se
 * desea crear un nuevo Ajuste o modificar uno existente
 *
 * @author Pedro Antonio Segado Solano
 */
public class EditorAjusteControlador {

    // Label
    @FXML
    private Label lbTitulo;
    
    // Textfields
    @FXML
    private TextField tfNombre;
    @FXML
    private TextField tfDuracion;
    
    // Spinners
    @FXML
    private Spinner<Integer> spAnalisisPagina;
    @FXML
    private Spinner<Integer> spAnalisisMenu;
    @FXML
    private Spinner<Integer> spCalibracionPagina;
    @FXML
    private Spinner<Integer> spCalibracionMenu;

    // Botones    
    @FXML
    private Button btCancelar;
    @FXML
    private Button btAceptar;

    //Otras variables necesarias
    private Stage stage;
    private Ajuste ajusteDuplicado;
    private Ajuste ajusteOriginal;
    private boolean aceptarPulsado = false;
    

    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {
        
       
        // Control de entrada solo numérico y máximo valor en textfield de duración (300s max)
        tfDuracion.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(300)));
        
        SpinnerValueFactory<Integer> valorPaginaAnalisis = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,9,1);
        SpinnerValueFactory<Integer> valorPaginaCalibracion = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,9,1);
        SpinnerValueFactory<Integer> valorMenuAnalisis = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,2,1);
        SpinnerValueFactory<Integer> valorMenuCalibracion = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,2,1);
        
        spAnalisisPagina.setValueFactory(valorPaginaAnalisis);
        spAnalisisMenu.setValueFactory(valorMenuAnalisis);
        spCalibracionPagina.setValueFactory(valorPaginaCalibracion);
        spCalibracionMenu.setValueFactory(valorMenuCalibracion);
    }

    // Pulsamos el botón cancelar para rechazar cualquier tipo de cambios o trabajo realizado
    @FXML
    private void cancelar(ActionEvent event) {
        stage.close();
    }

    // Pulsamos el botón aceptar y verificamos que tenemos la información necesaria para crear/modificar un Ajuste
    @FXML
    private void aceptar(ActionEvent event) {
        if (isInputValid()) { // Si tenemos los campos necesarios y son correctos
            // Ajustamos los atributos del objeto original con lo indicados en pantalla sobre el duplicado
            ajusteOriginal.setNombre(ajusteDuplicado.getNombre());
            
            ajusteOriginal.setAnalisisMenu(ajusteDuplicado.getAnalisisMenu());
            ajusteOriginal.setAnalisisPagina(ajusteDuplicado.getAnalisisPagina());
            ajusteOriginal.setCalibracionMenu(ajusteDuplicado.getCalibracionMenu());
            ajusteOriginal.setCalibracionPagina(ajusteDuplicado.getCalibracionPagina());
                        
            ajusteOriginal.setDuracion(ajusteDuplicado.getDuracion());

            aceptarPulsado = true;
            //Cerramos la ventana del editor de ajustes
            stage.close();
        }
    }

    // OTROS METODOS NO FXML
    
    // Validación de los campos del formulario, si no están completos se avisa con una Alerta al usuario
    private boolean isInputValid() {
        String mensajeMostrar = ""; // Preparamos un String para mostrar en la alerta al usuario

        if (tfNombre.getText() == null || tfNombre.getText().length() == 0) { // No hay nombre
            mensajeMostrar += "Debes seleccionar un nombre para el ajuste\n";
        }
        for (Ajuste a : App.getControladorPrincipal().getListaAjustes()) {
            if (a.getNombre().equals(tfNombre.getText()) & !ajusteOriginal.getNombre().equals(ajusteDuplicado.getNombre())) { // Si estamos modificando si se permite                                       
                mensajeMostrar += "Ya existe un ajuste con ese nombre";                                                       // el mismo nombre
            }
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
     * Este método establece el Ajuste original, y lo clona sobre un Ajuste Duplicado sobre el que  realizaremos las
     * modificaciones oportunas, preservando intacto el original
     * @param ajuste Ajuste con el que trabajar
     */
    public void setAjuste(Ajuste ajuste) {
        this.ajusteOriginal = ajuste;
        this.ajusteDuplicado = (Ajuste) ajuste.clone();

        tfNombre.textProperty().bindBidirectional(this.ajusteDuplicado.nombreProperty());
        spAnalisisMenu.getValueFactory().valueProperty().bindBidirectional(this.ajusteDuplicado.analisisMenuProperty().asObject());
        spAnalisisPagina.getValueFactory().valueProperty().bindBidirectional(this.ajusteDuplicado.analisisPaginaProperty().asObject());
        spCalibracionMenu.getValueFactory().valueProperty().bindBidirectional(this.ajusteDuplicado.calilbracionMenuProperty().asObject());
        spCalibracionPagina.getValueFactory().valueProperty().bindBidirectional(this.ajusteDuplicado.calibracionPaginaProperty().asObject());
               
        tfDuracion.textProperty().bindBidirectional(this.ajusteDuplicado.tiempoProperty(), new NumberStringConverter("0"));
        if (tfDuracion.textProperty().getValue().equals("0,0000")) {
            tfDuracion.setText("");
        }
    }

    /**
     * Este método establece el escenario con el que trabajar
     * @param stage Stage con la que trabajar
     */
    public void setStage(Stage stage) {        
        this.stage = stage;
    }

    /**
     * Este método permite saber si se ha pulsado Aceptar en el formulario o no
     * @return Booblean true si se ha pulsado aceptar o false si se cancela o cierra el formulario
     */
    public boolean sePulsaAceptar() {        
        return aceptarPulsado;
    }

    // Da titulo al editor según sea un Ajuste nuevo o modificar uno existente

    /**
     * Este método permite establecer el título de la ventana del editor en función de si es un nuevo ajuste o estamos
     * modificando uno existente.
     * @param titulo String con el título de la ventana
     */
    public void setTitulo(String titulo) {        
        lbTitulo.setText(titulo);
    }
}
