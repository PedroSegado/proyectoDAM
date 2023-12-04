package com.pasegados.labo.calibraciones;

import java.io.IOException;
import java.util.ArrayList;
import com.pasegados.labo.App;
import com.pasegados.labo.conexionesbbdd.ConexionesCalibrado;
import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.modelos.Calibrado;
import com.pasegados.labo.modelos.HiloAcondicionamiento;
import com.pasegados.labo.modelos.HiloEnvioDatos;
import com.pasegados.labo.modelos.HiloMedida;
import com.pasegados.labo.modelos.Patron;
import com.pasegados.labo.modelos.Puerto;
import java.sql.SQLException;
import java.util.Locale;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Esta clase representa al controlador del Editor de Calibrados, al que se accede cada vez que se desea crear un nuevo
 * Calibrado o modificar uno existente
 *
 * @author Pedro Antonio Segado Solano
 */
public class EditorCalibracionControlador {

    // TextField
    @FXML
    private TextField tfNombreCalibrado;
    @FXML
    private TextField tfCuentasPatron;

    // DatePicker
    @FXML
    private DatePicker dpFechaCalibrado;

    // Listas
    @FXML
    private ListView<Patron> lvPatronesAsignados;
    private ObservableList<Patron> listaPatronesAsignados;
    @FXML
    private ListView<Patron> lvPatrones;
    private ObservableList<Patron> listaPatrones;

    // Combobox
    @FXML
    private ComboBox<String> cbAjusteEquipo;
    private final ObservableList<String> LISTA_AJUSTES = FXCollections.observableArrayList();
    @FXML
    private ComboBox<String> cbEcuacion;
    private final ObservableList<String> LISTA_ECUACIONES = FXCollections.observableArrayList();

    // Labels
    @FXML
    private Label lbTitulo;
    @FXML
    private Label lbCoefCuad;
    @FXML
    private Label lbCoefLin;
    @FXML
    private Label lbTermiInd;
    @FXML
    private Label lbR2;
    @FXML
    private Label lbCoefR2;

    // Botones y Toggles
    @FXML
    private Button btAsignarPatron;
    @FXML
    private Button btQuitarPatron;
    @FXML
    private Button btAceptar;
    @FXML
    private Button btCancelar;
    @FXML
    private RadioButton rbActivoSi;
    @FXML
    private RadioButton rbActivoNo;
    @FXML
    private ToggleGroup tgActivo;
    @FXML
    private Button btAnalizarPatron;
    @FXML
    private Button btGrafica;

    //Otras variables necesarias
    private Stage stage;
    private Calibrado calibradoDuplicado;
    private Calibrado calibradoOriginal;
    private boolean aceptarPulsado = false;
    private Puerto puerto; //Puerto COM    
    private HiloAcondicionamiento cond;
    private HiloMedida medida;
    private HiloEnvioDatos envioDatos;
    private GraficaControlador controladorGrafica = new GraficaControlador();
    private static final Logger LOGGER = LogManager.getLogger(TabCalibracionesControlador.class); // logger del controlador que lo llama
    private static final ConexionesCalibrado CNC = ConexionesCalibrado.getINSTANCIA_CALIBRADO();
    private Patron patronActualizandose;
   

    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {
        //Combo regresion
        LISTA_ECUACIONES.addAll("Seleccionar...", "Lineal", "Cuadrática");
        cbEcuacion.setItems(LISTA_ECUACIONES);

        //Combo ajustes, con los existentes en la pestaña configuración
        for (Ajuste a : App.getControladorPrincipal().getListaAjustes()) {
            if (a.getAnalisisPagina()!=0 & a.getAnalisisMenu()!=0){ // Solo añadiremos ajustes activos en el menú analisis del equipo, es decir con atributo analisisPagina y analisisMenu distinto a 0
                LISTA_AJUSTES.add(a.getNombre());
            }            
        }
        cbAjusteEquipo.setItems(LISTA_AJUSTES);

        //Lista con todos los patrones
        listaPatrones = App.getControladorPrincipal().getListaPatrones();
        lvPatrones.setItems(listaPatrones);

        //Lista con todos los patrones asignados se asigna posteriormente al cargar el calibrado con el que trabajar en el método setCalibrado()      
        //Permitimos selección multiple en la listas
        lvPatrones.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvPatronesAsignados.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  
        
        lbCoefR2.setText("Coef. R\u00B2:"); // No se pueden poner superindices directamente en Scene Builder
    }

    // Analiza un Patrón para leer por primera vez o actualizar posteriormente sus cuentas
    @FXML
    private void analizarPatron(ActionEvent event) {
        //Nos aseguramos de tener un solo patron seleccionado en la lista de patrones
        ArrayList<Patron> seleccionado = new ArrayList<>();
        seleccionado.addAll(lvPatrones.getSelectionModel().getSelectedItems());

        if (seleccionado.size() == 1 & !cbAjusteEquipo.getValue().equals("Seleccionar...")) {
            patronActualizandose = lvPatrones.getSelectionModel().getSelectedItem();
            // Si empieza Analisis
            if (btAnalizarPatron.getText().equals("ANALIZAR")) {
                analizar();
                // Envio pulsaciones al puerto mediante un hilo, para evitar retraso en la ejecución de este hilo
                envioDatos = new HiloEnvioDatos("analizar", calibradoDuplicado, LOGGER);
                envioDatos.start();
                // Simulamos acondicionamiento del equipo, pasandole este controlador para que interactue con él
                cond = new HiloAcondicionamiento(this, LOGGER);
                cond.start();
                // Empezamos la medida, con la cuenta atrás de segundos correspondientes al ajuste del calibrado
                medida = new HiloMedida(this, calibradoDuplicado.getAjuste().getDuracion(), LOGGER);
                medida.start();

            // Si se aborta análisis en curso
            } else if (btAnalizarPatron.getText().equals("ABORTAR")) {
                // Durante el acondicionamiento
                if (cond.getSePuedeAbortar() & cond.getEstaCond()) {
                    cond.setSePuedeAbortar(false);
                    cond.setEstaCond(false);
                    abortar(patronActualizandose);
                } // Durante el ajuste de energia
                else if (cond.getSePuedeAbortar() & cond.getEstaAjustEner() & !cond.getTerminado()) {
                    cond.setSePuedeAbortar(false);
                    cond.setEstaAjustEner(false);
                    abortar(patronActualizandose);
                } // Durante la medida del patron
                else if (medida.getMidiendo()) {
                    medida.setMidiendo(false);
                    abortar(patronActualizandose);
                }
            }
        } else {
            if (seleccionado.size() == 0) {
                Alertas.alertaAnalisisPatronSeleccionado(0);
            } else if (seleccionado.size() > 1) {
                Alertas.alertaAnalisisPatronSeleccionado(seleccionado.size());
            } else {
                Alertas.alertaAjusteAnalisis();
            }
        }
    }

    // Asigna un Patrón de los existentes a la lista de patrones del calibrado
    @FXML
    private void asignarPatron(ActionEvent event) {
        int seleccionados = lvPatrones.getSelectionModel().getSelectedItems().size();

        if (seleccionados >= 1 & !cbAjusteEquipo.getValue().equals("Seleccionar...")) { // Si hay patrones seleccionados y hemos seleccionado algún ajuste
            for (Patron p : lvPatrones.getSelectionModel().getSelectedItems()) { //Recorremos los patrones seleccionados en la lista de patrones
                if (p.getCuentas() == 0) { //Si las cuentas son 0, no ha sido analizado todavia y no lo puedo asignar al calibrado
                    Alertas.alertaPatronNoAnalizado(p.getNombre(), calibradoDuplicado.getAjuste().getNombre());

                } else if (!listaPatronesAsignados.contains(p)) { //Si no está asignado ya, lo agrego (evito duplicidad de patrones)                
                    calibradoDuplicado.getListaPatrones().add(p);
                    if (!cbEcuacion.getValue().equals("Seleccionar...")) { // Si tiene asignada algún tipo de regresión, recalculo los coeficientes                        
                        calibradoDuplicado.getEcuacion().calculaCoeficientes(listaPatronesAsignados,calibradoDuplicado.getAjuste().getNombre(),calibradoDuplicado.getTipoRegresion());
                    } else { // Si no tiene asignada regresion, los coeficientes son 0
                        calibradoDuplicado.getEcuacion().coeficientesCero();
                    }
                }
            }
        } else { //No hemos seleccionado níngun patrón, alerta al usuario
            if (cbAjusteEquipo.getValue().equals("Seleccionar...")) { // Si no hemos seleccionado ajuste                
                Alertas.alertaAjuste();
            } else if (seleccionados == 0) { // Si no hemos seleccionado ningún patron en la lista
                Alertas.alertaAsignaPatron();
            }
        }
    }

    // Elimina un Patrón de la lista de patrones asignados al Calibrado
    @FXML
    private void quitarPatron(ActionEvent event) {
        int seleccion = lvPatronesAsignados.getSelectionModel().getSelectedItems().size();

        if (seleccion > 0) { //Si hay patrones seleccionados
            if (cbEcuacion.getValue().equals("Cuadrática") & !((listaPatronesAsignados.size() - seleccion) >= 3)) {
                Alertas.alertaRegresion("Cuadrática"); //Si quiero regresión cuadrática necesito al menos 3 patrones y no permito borrar
                cbEcuacion.setValue("Seleccionar...");                
                calibradoDuplicado.getEcuacion().coeficientesCero();
            } else if (cbEcuacion.getValue().equals("Lineal") & !((listaPatronesAsignados.size() - seleccion) >= 2)) {
                Alertas.alertaRegresion("Lineal"); //Si quiero regresión lineal necesito al menos 2 patrones y no permito borrar
                cbEcuacion.setValue("Seleccionar...");
                calibradoDuplicado.getEcuacion().coeficientesCero();                
            } else { // Quedan patrones suficiente para mantener el tipo de regresión, podemos quitar los patrónes de nuestro calibrado.
                calibradoDuplicado.getListaPatrones().removeAll(lvPatronesAsignados.getSelectionModel().getSelectedItems());
                if (!(cbEcuacion.getValue().isEmpty())) { //Si tiene asignada algún tipo de regresión, recalculo los coeficientes
                    calibradoDuplicado.getEcuacion().calculaCoeficientes(listaPatronesAsignados,calibradoDuplicado.getAjuste().getNombre(),calibradoDuplicado.getTipoRegresion());
                } else {
                    calibradoDuplicado.getEcuacion().coeficientesCero();
                }
            }
        } else { //No hemos seleccionado níngun patrón, alerta al usuario
            Alertas.alertaQuitarPatron();
        }
        lvPatronesAsignados.getSelectionModel().clearSelection(); //Elimino selección por defecto de las listas
        lvPatrones.getSelectionModel().clearSelection();
    }

    // Al pulsar "aceptar" se verifica que todo es correcto y ajustamos los atributos del 
    // calibradoOriginal con los configurados en calibradoDuplicado
    @FXML
    private void aceptar(ActionEvent event) {
        if (isInputValid()) { //Si la verificación de campos obligatorios devuelve true
            //Actualizamos el objeto Original con los datos del Duplicado
            calibradoOriginal.setNombre(calibradoDuplicado.getNombre());
            calibradoOriginal.setFecha(calibradoDuplicado.getFecha());
            calibradoOriginal.setAjuste(calibradoDuplicado.getAjuste());
            calibradoOriginal.setActivo(calibradoDuplicado.isActivo());
            calibradoOriginal.setTipoRegresion(calibradoDuplicado.getTipoRegresion());            
            calibradoOriginal.getEcuacion().setCoefCuadratico(calibradoDuplicado.getEcuacion().getCoefCuadratico());
            calibradoOriginal.getEcuacion().setCoefLineal(calibradoDuplicado.getEcuacion().getCoefLineal());
            calibradoOriginal.getEcuacion().setTerminoIndep(calibradoDuplicado.getEcuacion().getTerminoIndep());
            calibradoOriginal.getEcuacion().setCoefDeterminacion(calibradoDuplicado.getEcuacion().getCoefDeterminacion());
            calibradoOriginal.setListaPatrones(calibradoDuplicado.getListaPatrones());
            calibradoOriginal.actualizaPatronesString();
            calibradoOriginal.actualizaRangoString();            

            aceptarPulsado = true; //para que desde el controlador TabCalibracionControlador sepamos que se ha pulsado aceptar            
            stage.close(); //Cerramos la ventana del editor de calibrado
        }
    }

    // Si pulsamos "cancelar" no se guarda ningún cambio, simplemente se cierra la ventana
    @FXML
    private void cancelar(ActionEvent event) {
        //calibradoOriginal.getEcuacion().calculaCoeficientes(calibradoOriginal.getListaPatrones(),calibradoOriginal.getAjuste().getNombre(),calibradoOriginal.getTipoRegresion());
        stage.close(); //Cerramos la ventana y no guardamos ningún cambio de calibradoDuplicado sobre el calilbradoOriginal
    }

    // Actualiza los calculos de la ecuación de regresión
    @FXML
    private void calcularRegresion(ActionEvent event) {
        //Verificamos que para la regresión seleccionada tengamos patrones suficientes  
        if (cbEcuacion.getValue().equals("Seleccionar...")) {
            calibradoDuplicado.getEcuacion().coeficientesCero();                        
        } else if (cbEcuacion.getValue().equals("Cuadrática") & listaPatronesAsignados.size() < 3) {
            Alertas.alertaRegresion("Cuadrática"); //Necesitamos al menos 3 para regresión cuadrática
            calibradoDuplicado.getEcuacion().coeficientesCero();                        
            cbEcuacion.setValue("Seleccionar..."); //Cambiamos el valor del combo para poder borrar patrones y asignar más tarde la regresión            
        } else if (cbEcuacion.getValue().equals("Lineal") & listaPatronesAsignados.size() < 2) {
            Alertas.alertaRegresion("Lineal"); //Necesitamos al menos 2 para regresión lineas
            calibradoDuplicado.getEcuacion().coeficientesCero();                        
            cbEcuacion.setValue("Seleccionar...");
        } else {
            if (listaPatronesAsignados.size() >= 2) {
            calibradoDuplicado.getEcuacion().calculaCoeficientes(listaPatronesAsignados,calibradoDuplicado.getAjuste().getNombre(),calibradoDuplicado.getTipoRegresion());
            }
        }
    }

    // Actualiza las cuentas de los patrones a las correspondientes al método seleccionado en el combobox de ajuste de equipo.
    @FXML
    private void actualizarListas(ActionEvent event) {
        calibradoDuplicado.setAjuste(App.getControladorPrincipal().getAjuste(cbAjusteEquipo.getValue()));

        listaPatrones.forEach(p -> {
            try {
                p.RecuperarCuentasAjuste(cbAjusteEquipo.getValue());
            } catch (SQLException ex) {
                LOGGER.fatal("CALIBRACION: Error al recuprara las cuentas del calibrado " + calibradoDuplicado.getNombre()
                        + " con el ajuste " + calibradoDuplicado.getAjuste().getNombre() + "\n" + ex.getMessage());
            }
            if (p.getCuentas() == 0) { //Si algún patrón no ha sido analizado para ese ajuste                
                cbEcuacion.setValue("Seleccionar...");
            }
        });
        lvPatrones.refresh(); //Resfresco las listas de patrones
        lvPatronesAsignados.refresh();
    }

    // Permite modificar las cuentas manualmente, introduciendolas en el TextField y pulsando
    // tres veces con el raton
    @FXML
    private void truco(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 3) {
                patronActualizandose = lvPatrones.getSelectionModel().getSelectedItem();
                int cuentasViejas = patronActualizandose.getCuentas();
                int cuentasNuevas = Integer.valueOf(tfCuentasPatron.getText());
                String ajuste = cbAjusteEquipo.getValue();

                try {
                    CNC.actualizarCuentasPatron(patronActualizandose.getNombre(), ajuste, cuentasNuevas);
                    patronActualizandose.setCuentas(cuentasNuevas);
                    patronActualizandose.ActualizarCuentasAjuste(ajuste, cuentasNuevas);
                    lvPatrones.refresh();
                    lvPatronesAsignados.refresh();
                    calibradoDuplicado.getEcuacion().calculaCoeficientes(listaPatronesAsignados,calibradoDuplicado.getAjuste().getNombre(),calibradoDuplicado.getTipoRegresion());
                    LOGGER.info("CALIBRADO: Actualizadas correctamente las cuentas (antes: " + cuentasViejas + " - ahora: " + cuentasNuevas + ") del patrón "
                            + patronActualizandose.getNombre() + " para el ajuste " + calibradoDuplicado.getAjuste() + " en la tabla PatronAjuste");
                } catch (SQLException e) {
                    LOGGER.fatal("CALIBRADO: Error al actualizar en BBDD las cuentas del patrón " + patronActualizandose.getNombre() + " para el ajuste "
                            + calibradoDuplicado.getAjuste() + " en la tabla PatronAjuste" + "\n" + e.getMessage());
                    Alertas.alertaBBDD(e.getMessage());
                }
            }
        }
    }

    // Muestra una nueva ventana con la grafica de un calibrado
    @FXML
    private void muestraGrafica(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pasegados/labo/calibraciones/Grafica.fxml"));
            Parent root = loader.load();

            controladorGrafica = (GraficaControlador) loader.getController();
            controladorGrafica.setCalibrado(calibradoDuplicado);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Gráfica " + calibradoDuplicado.getNombre() + " - " + calibradoDuplicado.getTipoRegresion());
            controladorGrafica.dibuja();
            stage.setScene(scene);
            stage.setScene(scene);
            stage.setMinHeight(600);
            stage.setMinWidth(800);

            stage.showAndWait();

        } catch (IOException e) { // Error al localizar el FXML de la gráfica
            LOGGER.fatal("CALIBRADO: Error al abrir la gráfica del calibrado " + calibradoDuplicado.getNombre() + "\n" + e.getMessage());
            Alertas.alertaIOException(e.toString());
        }
    }

    // Permite seleccionar en la listView de todos los patrones el que se ha marcado en la de asignados
    @FXML
    private void seleccionarEnListaDisponibles(MouseEvent event) {
        // Utilizamos un cellfactory para sobreescribir el comportamiento normal en el cual si una vez seleccionado
        // pinchamos en otra lista o tabla, la lista  pierde el foco y el seleccionado queda marcado tenue. Al volver
        // a pinchar sobre este mismo objeto de la lista, no lo considera como getSelectedItem()
        // Con esta modificación si que vuelve a coger el item sobre el mismo item anterior
        lvPatronesAsignados.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Patron item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Rescatamos el patron seleccionado en la lista de asignados
        Patron selectedItem = lvPatronesAsignados.getSelectionModel().getSelectedItem();
        // y puede ser que haya mas de uno seleccionados
        MultipleSelectionModel<Patron> seleccionados = lvPatronesAsignados.getSelectionModel();
        // Si hay alguno seleccionado y es solo uno:
        if (selectedItem != null & seleccionados.getSelectedIndices().size() == 1) {
            for (Patron p : lvPatrones.getItems()) { // Recorro los elementos de la lista de todos los patrones
                if (p.equals(selectedItem)) { // y si coincide con el seleccionado
                    lvPatrones.getSelectionModel().clearSelection(); // desmarco lo que hubiera marcado
                    lvPatrones.getSelectionModel().select(selectedItem); // y marco el mismo patron que he pinchado en la otra lista
                    break; // Puedo salir de la iteracion for de patrones porque solo va a ser uno
                }
            }
        } else { // Si selecciono más de uno en asignados, no marco nada en la lista de disponibles
            lvPatrones.getSelectionModel().clearSelection();
        }
    }

    // Permite deseleccionar en la listView de Asignados el/los que estén seleccionado/s
    @FXML
    private void deseleccionarAsignados(MouseEvent event) {
        lvPatronesAsignados.getSelectionModel().clearSelection();
    }

    //OTROS METODOS NO GENERADOS EN EL FXML
    
    // Ajusta la interfaz tras pulsar el botón "Analizar"
    private void analizar() {
        btAnalizarPatron.setText("ABORTAR");
        tfCuentasPatron.setText("");
        lvPatrones.setDisable(true); // desactivo las listas y el textfield para que el usuario no manipule durante la medida
        lvPatronesAsignados.setDisable(true);
        tfCuentasPatron.setDisable(true);
    }

    // Procede a abortar el análisis del patrón al pulsar el botón "Abortar" y ajusta la interfaz
    private void abortar(Patron patron) {
        lvPatrones.setDisable(false);
        lvPatronesAsignados.setDisable(false);
        envioDatos = new HiloEnvioDatos("abortar", LOGGER);
        envioDatos.start();
        try {
            envioDatos.join();
            btAnalizarPatron.setText("ANALIZAR");
            LOGGER.warn("ABORTADO el análisis del patrón " + patron.getNombre() + " - " + patron.getConcentracion());
        } catch (InterruptedException ex) {
            LOGGER.fatal("Error al esperar la respuesta del equipo tras enviar secuencia para abortar."
                    + "\n" + ex.getMessage());
        }
    }

    // Validación de los campos del formulario, si no están completos se avisa con una Alerta al usuario
    private boolean isInputValid() {
        String mensajeMostrar = "";

        if (tfNombreCalibrado.getText() == null || tfNombreCalibrado.getText().length() == 0) { // No hay nombre de calibrado
            mensajeMostrar += "Debes seleccionar un nombre para la calibración\n";
        }
        for (Calibrado c : App.getControladorPrincipal().getListaCalibrados()) {
            if (c.getNombre().equals(tfNombreCalibrado.getText()) & !calibradoOriginal.getNombre().equals(calibradoDuplicado.getNombre())) { // Si estamos modificando si se permite                                          ) {
                mensajeMostrar += "Ya existe un calibrado con ese nombre";
            }
        }
        if (cbAjusteEquipo.getValue().equals("Seleccionar...")) { //No hay ajuste para el calibrado
            mensajeMostrar += "Debes seleccionar un ajuste para la calibración\n";
        }
        // El resto de campos los considero no obligatorios, se pueden completar posteriormente
        if (mensajeMostrar.length() == 0) { //No hay mensaje de error
            return true;
        } else { //Hay mensaje en alguno de los campos necesarios o en ambos
            Alertas.alertaErrorFormulario(mensajeMostrar);
            return false;
        }
    }

    /**
     * Recibe un Calibrado y lo usa como Calibrado Original, y lo clona sobre un Calibrado Duplicado sobre el que
     * realizaremos las modificaciones oportunas, preservando intacto el original.     *
     * @param calibrado Calibrado con el que trabajar en el formulario
     */
    public void setCalibrado(Calibrado calibrado) {
        this.calibradoOriginal = calibrado;
        this.calibradoDuplicado = (Calibrado) calibrado.clone(); //Para realizar modificaciones sin riesgo de estropear el objeto original

        calibradoDuplicado.setListaPatrones(FXCollections.observableArrayList()); //Evitamos que comparta la lista, creando una nueva con los mismos patrones        
        for (Patron p : calibradoOriginal.getListaPatrones()) {
            calibradoDuplicado.getListaPatrones().add(p); // añadimos los mismo patrones, pero los cambios en esta no afectan a la lista original
        }

        //Binding
        tfNombreCalibrado.textProperty().bindBidirectional(this.calibradoDuplicado.nombreProperty());
        dpFechaCalibrado.valueProperty().bindBidirectional(this.calibradoDuplicado.fechaProperty());
        //cbAjusteEquipo.valueProperty().bindBidirectional(this.calibradoDuplicado.getAjuste().nombreProperty()); //PROBLEMA        
        if (this.calibradoDuplicado.getAjuste()!=null){
            cbAjusteEquipo.setValue(calibradoDuplicado.getAjuste().getNombre()); // Sin binding porque sino cambia el bombre del objeto ajuste en cada cambio del combo
            //Ajustamos las cuentas de todos los patrones atendiendo al ajuste que usa el calibrado         
            listaPatrones.forEach(p -> {
                try {
                    p.RecuperarCuentasAjuste(calibradoDuplicado.getAjuste().getNombre());
                } catch (SQLException ex) {
                    LOGGER.fatal("CALIBRACION: Error al recuprara las cuentas del calibrado " + calibradoDuplicado.getNombre()
                            + " con el ajuste " + calibradoDuplicado.getAjuste().getNombre() + "\n" + ex.getMessage());
                }
            });
        }
        else{
            cbAjusteEquipo.setValue("Seleccionar...");
        }                        

        //Radiobutton a marcar
        if (calibradoDuplicado.isActivo() == true) {
            rbActivoSi.setSelected(true);
        } else {
            rbActivoNo.setSelected(true);
        }

        //Binding de los radiobutton con el estado del calibrado
        tgActivo.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (((RadioButton) newValue).getId().equals("rbActivoSi")) {
                calibradoDuplicado.setActivo(true);
            } else {
                calibradoDuplicado.setActivo(false);
            }
        });

        //Resto del binding
        lvPatronesAsignados.itemsProperty().bindBidirectional(this.calibradoDuplicado.listaPatronesProperty());
        cbEcuacion.valueProperty().bindBidirectional(this.calibradoDuplicado.tipoRegresionProperty());
        //this.calibradoDuplicado.getEcuacion().tipoRegresionProperty().bind(cbEcuacion.valueProperty()); // Así la ecuacion del calibrado tambien tiene el mismo tipo de regresion siempre
        
        
        
        // En estos ya el binding no es bidireccional
        lbCoefCuad.textProperty().bind(this.calibradoDuplicado.getEcuacion().coefCuadraticoProperty().asString());
        lbCoefLin.textProperty().bind(this.calibradoDuplicado.getEcuacion().coefLinealProperty().asString());
        lbTermiInd.textProperty().bind(this.calibradoDuplicado.getEcuacion().terminoIndepProperty().asString(Locale.US, "%.8f"));
        lbR2.textProperty().bind(this.calibradoDuplicado.getEcuacion().coefDeterminacionProperty().asString(Locale.US, "%.8f"));

        //Asignamos los patrones del calibrado a la listview de "Patrones asignados"
        listaPatronesAsignados = calibradoDuplicado.getListaPatrones(); //Muestro los patrones de esta calibracion en la lista de patrones asignadosv

        //Binding para mostrar las cuentas del patron seleccionado en la lista en el textfield de las cuentas
        lvPatrones.getSelectionModel().selectedItemProperty().addListener((lista, valorViejo, valorNuevo) -> {
            if (valorNuevo != null) {
                tfCuentasPatron.setText(String.valueOf(valorNuevo.getCuentas()));
            }
        });
    }

    /**
     * Este método establece el escenario sobre el que trabajar     *
     * @param stage     */
    public void setStage(Stage stage) {

        this.stage = stage;
    }

    /**
     * Este método devuelve si los cambios sobre el formulario del Calibrado son aceptados o no     *
     * @return boolean true si el usuario pulsa aceptar, o false si pulsa cancelar o cierra la ventana
     */
    public boolean sePulsaAceptar() {
        return aceptarPulsado;
    }

    /**
     * Este método da título al editor según sea un calibrado nuevo o modificar uno existente     *
     * @param titulo String con el título de la ventana del formulario
     */
    public void setTitulo(String titulo) {
        lbTitulo.setText(titulo);
    }

    /**
     * Este método establece las cuentas tras un análisis y las muestra en el TextField correspondiente     *
     * @param cuentas String con el numero de cuentas
     */
    public void setCuentas(String cuentas) {
        tfCuentasPatron.setText(cuentas);
    }

    /**
     * Este método es llamado cuando quedan 5 segundos para terminar el análisis del patrón, permitiendo escuchar los
     * datos que desde ese momento se reciban por el puerto COM.
     */
    public void recibirDatos() {
        puerto = App.getControladorConfiguracion().getPuerto();
        boolean conexion = puerto.activaPuerto();
        if (conexion) { // Si el puerto está abierto
            LOGGER.info("PATRON: Puerto abierto, recibiendo datos de cuentas de Patrón " + patronActualizandose.getNombre() + " desde OXFORD");
            puerto.escucharPuerto("patron");
            
            // Listener que se ejecuta al actualizarse el valor del TextField de cuentas de la muestra que se analiza
            tfCuentasPatron.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!newValue.equals("")) {
                        if(puerto.isAbierto()){ // si el puerto esta abierto                        
                            puerto.cierraConexion(); // podemos cerrar el puerto. Analisis terminado.
                            LOGGER.info("CALIBRADO: Analisis de Patrón " + patronActualizandose.getNombre() + " terminado. Puerto cerrado");
                        }                        
                        tfCuentasPatron.textProperty().removeListener(this); //Eliminamos el listener ya que hemos recibido la lectura de cuentas
                        lvPatrones.setDisable(false); // volvemos a activar las listas y el texfield para que el usuario pueda manipularlas
                        lvPatronesAsignados.setDisable(false);
                        tfCuentasPatron.setDisable(false);

                        int cuentasViejas = patronActualizandose.getCuentas();
                        int cuentasNuevas = Integer.valueOf(newValue);
                        String ajuste = cbAjusteEquipo.getValue();

                        try {  // Actualizamos las cuentas en la tabla PatronAjuste                       
                            CNC.actualizarCuentasPatron(patronActualizandose.getNombre(), calibradoDuplicado.getAjuste().getNombre(), cuentasNuevas);
                            patronActualizandose.setCuentas(cuentasNuevas);
                            patronActualizandose.ActualizarCuentasAjuste(ajuste, cuentasNuevas);
                            lvPatrones.refresh();
                            lvPatronesAsignados.refresh();
                            LOGGER.info("PATRON: Actualizadas correctamente las cuentas (antes: " + cuentasViejas + " - ahora: " + cuentasNuevas + ") del patrón " + patronActualizandose.getNombre() + " para el ajuste "
                                    + calibradoDuplicado.getAjuste() + " en la tabla PatronAjuste");                            
                        } catch (SQLException e) {
                            LOGGER.fatal("PATRON: Error al actualizar en BBDD las cuentas del patrón " + patronActualizandose.getNombre() + " para el ajuste "
                                    + calibradoDuplicado.getAjuste() + " en la tabla PatronAjuste" + "\n" + e.getMessage());
                            Alertas.alertaBBDD(e.getMessage());
                        }
                        // Ejecuciones no permitidas dentro del listener, usamos platform runlater
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                calibradoDuplicado.getEcuacion().calculaCoeficientes(listaPatronesAsignados,calibradoDuplicado.getAjuste().getNombre(),calibradoDuplicado.getTipoRegresion());
                                btAnalizarPatron.setText("ANALIZAR");
                            }
                        });
                    }
                }
            });
        } else {
            LOGGER.fatal("ERROR al intentar abrir el puerto COM");
            abortar(patronActualizandose);
            Alertas.alertaRecibirDatos();            
        }        
    }

    /**
     * Este método pèrmite tener acceso al HiloEnvioDatos lanzado para comunicar al equipo las pulsaciones que ponen en
     * marcha el comportamiento esperado por parte del mismo,
     *
     * @return HiloEnvioDatos thread ejecutado para enviar datos al equipo
     */
    public HiloEnvioDatos getEnvioDatos() {

        return envioDatos;
    }

    /**
     * Este método permite tener acceso al HiloAcondicionamiento lanzado para la medida
     *
     * @return HiloAcondicionamiento thread ejecutado para mostrar el acondicionamiento del equipo
     */
    public HiloAcondicionamiento getAcondicionamiento() {

        return cond;
    }
}
