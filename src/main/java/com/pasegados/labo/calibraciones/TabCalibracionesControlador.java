package com.pasegados.labo.calibraciones;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import com.pasegados.labo.App;
import com.pasegados.labo.conexionesbbdd.ConexionesCalibrado;
import com.pasegados.labo.conexionesbbdd.ConexionesPatron;
import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.modelos.Analisis;
import com.pasegados.labo.modelos.Calibrado;
import com.pasegados.labo.modelos.ColeccionCalibraciones;
import com.pasegados.labo.modelos.HiloEnvioDatos;
import com.pasegados.labo.modelos.Patron;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Esta clase representa al controlador de la pestaña Calibraciones
 * 
 * @author Pedro Antonio Segado Solano
 */
public class TabCalibracionesControlador {

    // Tablas   
    @FXML
    private TableView<Patron> tvPatrones;
    private ObservableList<Patron> listaPatrones;
    
    @FXML
    private TableColumn<Patron, String> colPatronNombre;
    @FXML
    private TableColumn<Patron, LocalDate> colPatronFecha;
    @FXML
    private TableColumn<Patron, String> colPatronConcentracion; //Como String en vez de float para formatear a 4 decimales
    @FXML
    private TableView<Calibrado> tvCalibrados;
    private ObservableList<Calibrado> listaCalibrados;
    @FXML
    private TableColumn<Calibrado, String> colCalibradosNombre;
    @FXML
    private TableColumn<Calibrado, LocalDate> colCalibradosFecha;
    @FXML
    private TableColumn<Calibrado, Boolean> colCalibradosActivo;

    // Labels
    @FXML
    private Label lbNombre;
    @FXML
    private Label lbFecha;
    @FXML
    private Label lbAjuste;
    @FXML
    private Label lbActivo;
    @FXML
    private Label lbRango;
    @FXML
    private Label lbPatrones;
    @FXML
    private Label lbCoefCuad;
    @FXML
    private Label lbCoefLin;
    @FXML
    private Label lbTermInd;
    @FXML
    private Label lbCoefDeterminacion;

    //Botones
    @FXML
    private Button btAnadirPatron;
    @FXML
    private Button btActualizarPatron;
    @FXML
    private Button btEliminarPatron;
    @FXML
    private Button btEliminarCalibrado;
    @FXML
    private Button btActualizarCalibrado;
    @FXML
    private Button btAnadirCalibrado;
    @FXML
    private Button btGrafica;

    // Otras variables
    private static final ConexionesPatron CNP = ConexionesPatron.getINSTANCIA_PATRON();
    private static final ConexionesCalibrado CNC = ConexionesCalibrado.getINSTANCIA_CALIBRADO();
    private EditorPatronControlador controladorPatron = new EditorPatronControlador();
    private EditorCalibracionControlador controladorCalibrado = new EditorCalibracionControlador();
    private GraficaControlador controladorGrafica = new GraficaControlador();
    private static final Logger LOGGER = LogManager.getLogger(TabCalibracionesControlador.class);

    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {
        App.setControladorCalibrados(this); // Establecemos en la clase App que este es el controlador FXML del la Tab Calibraciones

        // COLUMNAS TABLA PATRONES
        colPatronNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());        
        colPatronFecha.setCellValueFactory(cellData -> cellData.getValue().fechaProperty());        
        colPatronConcentracion.setCellValueFactory(celda -> celda.getValue().concentracionProperty().asString(Locale.US, "%.4f")); //Mostrar 4 decimales siempre y . decimal

        // COLUMNAS TABLA CALIBRADO
        colCalibradosNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());   
        colCalibradosFecha.setCellValueFactory(cellData -> cellData.getValue().fechaProperty());   
        colCalibradosActivo.setCellValueFactory(cellData -> cellData.getValue().activoProperty());   
        // Creamos un cell factory personalizado para ver si/no en vez de true/false
        colCalibradosActivo.setCellFactory(column -> new TableCell<Calibrado, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item ? "Sí" : "No");
                }
            }
        });
        
         
        //BINDING CALIBRADOS (no es necesario bidireccional pues solo muestra información en las label)       
        tvCalibrados.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv != null) {
                lbNombre.textProperty().bind(nv.nombreProperty());
                lbFecha.textProperty().bind(nv.fechaProperty().asString());
                lbActivo.textProperty().bind(Bindings.when(nv.activoProperty()).then("SI").otherwise("NO"));         
                                
                if (nv.getAjuste()!=null) {
                    lbAjuste.textProperty().bind(nv.getAjuste().nombreProperty());
                } else {                    
                    lbAjuste.textProperty().unbind();
                    lbAjuste.textProperty().set("No Establecido");    
                }
                           
                lbPatrones.textProperty().bind(nv.patronesStringProperty());
                lbRango.textProperty().bind(nv.rangoStringProperty());                
                lbCoefCuad.textProperty().bind(nv.coefCuadraticoProperty().asString());                
                lbCoefLin.textProperty().bind(nv.coefLinealProperty().asString());
                lbTermInd.textProperty().bind(nv.terminoIndepProperty().asString(Locale.US, "%.8f"));
                lbCoefDeterminacion.textProperty().bind(nv.coefDeterminacionProperty().asString(Locale.US, "R\u00B2= " + "%.8f"));

            } else {
                lbNombre.setText("");
                lbFecha.setText("");
                lbActivo.setText("");
                lbAjuste.setText("");
                lbPatrones.setText("");
                lbRango.setText("");
                lbCoefCuad.setText("");
                lbCoefLin.setText("");
                lbTermInd.setText("");
                lbCoefDeterminacion.setText("");
            }
        });        
    }
    

    // -------------------- PATRONES ------------------------ //
        
    // Abre el editor de patrones para crear un nuevo Patron
    @FXML
    private void nuevoPatron(ActionEvent event) {       
        Patron nuevoPatron = new Patron();

        if (accedeFormularioPatron(nuevoPatron, "nuevo")) { // Si en el editor se ha pusaldo "Aceptar"
            try { // intentamos añadir a BBDD y si se consigue añadimos también a la lista
                CNP.insertar(nuevoPatron);
                listaPatrones.add(nuevoPatron);
                LOGGER.info("PATRON: Creado nuevo con datos " + nuevoPatron.toStringCompleto());
            } catch (SQLException e) { // Error al comunicar con BBDD
                LOGGER.fatal("PATRON: Error al guardar en BBDD el nuevo patron con datos " + nuevoPatron.toStringCompleto() + "\n" + e.getMessage());
                Alertas.alertaBBDD(e.getMessage());
            }
        }
    }

    // Modifica un Patrón existente abriendo el editor de patrones y mostrando sus datos ya existentes
    @FXML
    private void modificarPatron(ActionEvent event) {
        Patron seleccionado = tvPatrones.getSelectionModel().getSelectedItem();        

        if (seleccionado != null) { // si hemos seleccionado uno en la tabla de patrones
            String valorViejo = seleccionado.toStringCompleto(); // Para informar posteriormente en el log
            String nombreOriginal = seleccionado.getNombre(); // Para actualizar en BBDD, por si cambio el nombre al objeto
                        
            if (accedeFormularioPatron(seleccionado, "modificar")) { // Si en el editor se ha pulsado "Aceptar"                
                try { //Modifico en la BBDD, en la lista ya se ha cambiado            
                    CNP.actualizarPatron(seleccionado, nombreOriginal);
                    LOGGER.info("PATRON: Modificado el patrón con datos " + valorViejo + " a los nuevos valores " + seleccionado.toStringCompleto());
                } catch (SQLException e) {
                    LOGGER.fatal("PATRON: Error al modificar en BBDD el patron con datos " + valorViejo + "\n" + e.getMessage());
                    Alertas.alertaBBDD(e.getMessage());
                }
                // Recorremos los calibrados existentes
                for (Calibrado c : listaCalibrados) {                     
                    if (c.getListaPatrones().contains(seleccionado)){ // Si el calibrado contiene el patron modificado
                        c.actualizaPatronesString();                        
                        c.ajustaCoeficientes("normal"); // ajustemos coeficientes y strings del calibrado afectado                        
                        c.actualizaRangoString();
                    }
                }               
            }
        } else { // No se ha seleccionado nada en la lista
            Alertas.alertaErrorModificar("patrón");
        }
    }

    // Elimina un Patrón existente
    @FXML
    private void eliminarPatron(ActionEvent event) {        
        Patron seleccionado = tvPatrones.getSelectionModel().getSelectedItem();        

        if (seleccionado != null) { // si hemos seleccionado uno en la tabla de patrones
            Patron patron = tvPatrones.getSelectionModel().getSelectedItem();
            //Alerta confimracion
            boolean confirmacion = Alertas.alertaEliminaObjeto("patron", patron.getNombre());
            if (confirmacion) { 
                //El usuario ha aceptado eliminarlo. Se quita el registro de la BBDD.
                try {
                    CNP.eliminarPatron(patron.getNombre());
                    listaPatrones.remove(patron); // y de la lista de patrones                    
                    LOGGER.warn("PATRON: Eliminado el patrón con datos " + patron.toStringCompleto());
                    //Compruebo si el patrón se usa en algún calibrado, para quitarlo de la lista de este
                    for (Calibrado c : listaCalibrados) {
                        if (c.getListaPatrones().contains(patron)) {
                            c.getListaPatrones().remove(patron); //eliminamos el patron de la lista del calibrado
                            c.actualizaPatronesString(); //actualizamos el string de los patrones usados
                            c.actualizaRangoString();   //actualizamos el rango del calibrado
                            c.ajustaCoeficientes("normal"); //recalculamos los coeficientes con los patrones que quedan
                            
                            // Verifico ademas que a ese calibrado le queden al menos 3 patrones para que su regresión
                            // sea cuadrática, o 2 para lineal. Si no los tiene, cambio la regresión a "Seleccionar..."
                            // para que el usuario lo solucione y además desactivo la calibracion
                            
                            if (!c.cumple()){ // no cumple requisitos para la regresión asignada                                                                
                                try{ // Actualizo el calibrado en la BBDD y en el propio objeto                                
                                    CNC.actualizarCalibrado(c, c.getNombre()); 
                                    c.setActivo(false); // desactivo el calibrado
                                    c.setTipoRegresion("Seleccionar..."); // elimino regresión para que el usario actúe
                                    c.setCoefCuadratico(0f); // coeficientes todos a 0
                                    c.setCoefLineal(0f);
                                    c.setTerminoIndep(0f);
                                    c.setCoefDeterminacion(0f);
                                    // Aviso al usuario con un Alert de lo ocurrido
                                    Alertas.alertaCalibradoFaltanPatrones(patron.getNombre(), c.getNombre());
                                    LOGGER.warn("CALIBRADO: Desactivado el calibrado "+ c.getNombre() + " por falta de patrones en regresion " + c.getTipoRegresion());
                                } catch (SQLException e) {
                                    LOGGER.fatal("CALIBRADO: Error al actualizar calibrado " + c.getNombre() + " en BBDD tras eliminar el patron con datos " 
                                                 + patron.toStringCompleto() + "\n" + e.getMessage());
                                    Alertas.alertaBBDD(e.getMessage());
                                }                                
                            }
                        }
                    } 
                } catch (SQLException e) {
                    LOGGER.fatal("PATRON: Error al eliminar en BBDD el patron con datos " + patron.toStringCompleto() + "\n" + e.getMessage());
                    Alertas.alertaBBDD(e.getMessage());
                }

                tvPatrones.getSelectionModel().clearSelection(); // no dejo nada seleccionado en la tabla para evitar borrados accidentales
            }            
        } else { // No se ha seleccionado nada en la lista
            Alertas.alertaErrorBorrar("patrón");
        }
    }

    // Llama a la ventana del formulario, pasandole a esta los datos del Patron o creando uno nuevo, según el parámetro
    // titulo sea "nuevo" o "modificar". 
    private boolean accedeFormularioPatron(Patron patron, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/pasegados/labo/calibraciones/EditorPatron.fxml"));
            Parent root = loader.load();

            controladorPatron = (EditorPatronControlador) loader.getController();
            controladorPatron.setPatron(patron); // Le pasamos un patron nuevo o uno a modificar

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Gestión de patrones");
            if (titulo.equals("modificar")) {
                controladorPatron.setTitulo("Modificar Patrón:");
            } else {
                controladorPatron.setTitulo("Nuevo Patrón:");
            }
            controladorPatron.setStage(stage);
            stage.setScene(scene);
            stage.setMinHeight(388d);
            stage.setMinWidth(412d);
            stage.setMaxHeight(388d);
            stage.setMaxWidth(412d);

            stage.showAndWait(); // mostramos y esperamos a que el usuario acepte, cancele o cierre la ventana

            if (controladorPatron.sePulsaAceptar()) { // Si el usuario ha pulsado el boton "Aceptar"
                return true;  
            }            
        } catch (IOException ex) { // Error al localizar el FXML del editor
            LOGGER.fatal("PATRON: Error al abrir el formulario de 'Gestión de Patrones'" + "\n" + ex.getMessage());
            Alertas.alertaAbrirEditor("patrones");            
        }
        return false; // si llegamos aquí el usuario ha cancelado o cerrado el formulario.
    }

    // -------------------- CALIBRADOS ------------------------ //
        
    // Abre el editor de calibrados patrones para crear un nuevo Calibrado
    @FXML
    private void nuevoCalibrado(ActionEvent event) {
        Calibrado nuevoCalibrado = new Calibrado();

        if (accedeFormularioCalibrado(nuevoCalibrado, "nuevo")) { // Si en el editor se ha pulsado "Aceptar"
            try { // intentamos añadir a BBDD y si se consigue añadimos también a la lista                
                CNC.insertarCalibrado(nuevoCalibrado);
                listaCalibrados.add(nuevoCalibrado);
                LOGGER.info("CALIBRADO: Creado nuevo con datos " + nuevoCalibrado.toString());                
                // Insertamos los datos referentes a la lista de patrones del nuevo calibrado en la tabla "CalibradoPatron"
                for (Patron p : nuevoCalibrado.getListaPatrones()) {
                    try{
                        CNC.insertarListaPatronesCalibrado(nuevoCalibrado.getNombre(), p.getNombre());
                        LOGGER.info("CALIBRADO: Agregado correctamente el patron " + p.getNombre() + 
                                               " correspondientes al calibrado " + nuevoCalibrado.getNombre() + " en tabla CalibradoPatron");
                    } catch (SQLException e) {
                        LOGGER.fatal("CALIBRADO: Error al guardar en BBDD el patron " + p.getNombre() +  " correspondientes al calibrado " +
                                                 nuevoCalibrado.getNombre() + " en tabla CalibradoPatron" + "\n" + e.getMessage());
                        Alertas.alertaBBDD(e.getMessage());
                    }
                }
            } catch (SQLException e) {
                LOGGER.fatal("CALIBRADO: Error al guardar en BBDD el nuevo calibrado con datos " + nuevoCalibrado.toString() + "\n" + e.getMessage());
                Alertas.alertaBBDD(e.getMessage());
            }
        }
    }

    // Modifica un Calibrado existente abriendo el editor de calibrados y mostrando sus datos ya existentes
    @FXML
    private void modificarCalibrado(ActionEvent event) {
        Calibrado seleccionado = tvCalibrados.getSelectionModel().getSelectedItem();
        
        if (seleccionado != null) { // si hemos seleccionado uno en la tabla de calibrados
            String valorViejo = seleccionado.toString(); // Para informar posteriormente en el log
            ArrayList<Patron> listaOriginal = new ArrayList<>(); // Para comparar los patrones de antes con los de después de la modificación
            listaOriginal.addAll(seleccionado.getListaPatrones());
            String nombreOriginal = seleccionado.getNombre(); // Por si renonmbro el calibrado, poder hacer el update en BBDD
            
            if (accedeFormularioCalibrado(seleccionado, "modificar")) { // Si en el editor se ha pulsado "Aceptar"                
                try {//Modifico en la BBDD, en la lista ya se ha cambiado   
                    CNC.actualizarCalibrado(seleccionado, nombreOriginal);
                    LOGGER.info("CALIBRADO: Modificado el calibrado con datos " + valorViejo + " a los nuevos valores " + seleccionado.toString());

                    //Actualizamos en la BBDD los cambios referentes a la lista de patrones y a las cuentas de los mismos
                    for (Patron p : listaPatrones) {
                        
                        //Si el patron es nuevo en el calibrado, lo insertamos en la BBDD
                        if (!listaOriginal.contains(p) & seleccionado.getListaPatrones().contains(p)) {                            
                            try{
                                CNC.insertarListaPatronesCalibrado(seleccionado.getNombre(), p.getNombre());
                                LOGGER.info("CALIBRADO: Agregado correctamente el nuevo patron " + p.getNombre() + 
                                                       " correspondientes al calibrado " + seleccionado.getNombre() + " en tabla CalibradoPatron");
                            } catch (SQLException e) {
                                LOGGER.fatal("CALIBRADO: Error al guardar en BBDD el patron " + p.getNombre() +  " correspondientes al calibrado " +
                                                         seleccionado.getNombre() + " en tabla CalibradoPatron" + "\n" + e.getMessage());
                                Alertas.alertaBBDD(e.getMessage());
                            }                            
                        
                        //Si se ha eliminado el patron del calibrado, lo eliminamos de la BBDD  
                        } else if (!seleccionado.getListaPatrones().contains(p) & listaOriginal.contains(p)) {
                            try{
                                CNC.eliminarListaPatronesCalibrado(seleccionado.getNombre(), p.getNombre());
                                LOGGER.info("CALIBRADO: Eliminado correctamente el patron " + p.getNombre() + 
                                                       " correspondientes al calibrado " + seleccionado.getNombre() + " en tabla CalibradoPatron");
                            } catch (SQLException e) {
                                LOGGER.fatal("CALIBRADO: Error al guardar en BBDD el patron " + p.getNombre() +  " correspondientes al calibrado " +
                                                         seleccionado.getNombre() + " en tabla CalibradoPatron" + "\n" + e.getMessage());
                                Alertas.alertaBBDD(e.getMessage());
                            }                                   
                        }                                 
                    }
                } catch (SQLException e) {
                    LOGGER.fatal("CALIBRADO: Error al modificar en BBDD el calibrado con datos " + valorViejo + "\n" + e.getMessage());
                    Alertas.alertaBBDD(e.getMessage());
                }                
                // Si hemos cambiado el nombre, en la lista de Resultados, actualizado el nombre de calibrado al nuevo
                // En la BBDD se hace automaticamente gracias al ON UPDATE CASCADE
                if (!nombreOriginal.equals(seleccionado.getNombre())){
                    for (Analisis a : App.getControladorPrincipal().getListaResultados()){
                        if (a.getCalibrado().equals(nombreOriginal)){
                            a.setCalibrado(seleccionado.getNombre());
                        }
                    }
                }
            }          
        } else { // No se ha seleccionado nada en la lista
            Alertas.alertaErrorModificar("calibrado");
        }
    }

    // Elimina un Calibrado existente
    @FXML
    private void eliminarCalibrado(ActionEvent event) {
        Calibrado seleccionado = tvCalibrados.getSelectionModel().getSelectedItem();
        
        if (seleccionado != null) { // si hemos seleccionado uno en la tabla de calibrados
            Calibrado calibrado = tvCalibrados.getSelectionModel().getSelectedItem();
            //Alerta confirmacion
            boolean confirmacion = Alertas.alertaEliminaObjeto("calibrado", calibrado.getNombre());
            if (confirmacion) {                
                // El usuario ha aceptado eliminarlo. Se quita el registro de la BBDD.
                try {
                    CNC.eliminarCalibrado(calibrado.getNombre());
                    tvCalibrados.getItems().remove(calibrado); // también de la lista de Calibrados                    
                    LOGGER.warn("CALIBRADO: Eliminado el calibrado con datos " + calibrado.toString());                    
                } catch (SQLException e) {
                    LOGGER.fatal("CALIBRADO: Error al eliminar en BBDD el calibrado con datos " + calibrado.toString() + "\n" + e.getMessage());         
                    Alertas.alertaBBDD(e.getMessage());
                }
                
                tvCalibrados.getSelectionModel().clearSelection(); // no dejo nada seleccionado en la tabla para evitar borrados accidentales
                
                // Si hay resultados con este calibrado, los elimino de la lista, y se copia a la tabla AnalisisOLD
                // En la BBDD se hace automaticamente gracias a un trigger
                ArrayList<Analisis> aux = new ArrayList<>();
                aux.addAll(App.getControladorPrincipal().getListaResultados());
                for (Analisis a : aux){                    
                    if (a.getCalibrado().equals(seleccionado.getNombre())){
                        App.getControladorPrincipal().getListaResultados().remove(a);
                    }
                }
            }            
        } else {
            Alertas.alertaErrorBorrar("calibrado");
        }
    }
    
    // Llama a la ventana del formulario, pasandole a esta los datos del Calibrado o creando uno nuevo, según el
    // parámetro titulo sea "nuevo" o "modificar". 
    private boolean accedeFormularioCalibrado(Calibrado calibrado, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/pasegados/labo/calibraciones/EditorCalibracion.fxml"));
            Parent root = loader.load();

            controladorCalibrado = (EditorCalibracionControlador) loader.getController();
            controladorCalibrado.setCalibrado(calibrado);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Gestión de calibraciones");
            if (titulo.equals("modificar")) {
                controladorCalibrado.setTitulo("Modificar Calibración:");
            } else {
                controladorCalibrado.setTitulo("Nueva Calibración:");
            }
            controladorCalibrado.setStage(stage);            
            stage.setScene(scene);
            stage.setMinHeight(838d);
            stage.setMinWidth(712d);
            stage.setMaxHeight(838d);
            stage.setMaxWidth(712d);

            stage.showAndWait(); // mostramos y esperamos a que el usuario acepte, cancele o cierre la ventana

            if (controladorCalibrado.sePulsaAceptar()) { // Si el usuario ha pulsado el boton "Aceptar"
                return true;
            }
        } catch (IOException ex) { // Error al localizar el FXML del editor
            LOGGER.fatal("CALIBRADO: Error al abrir el formulario de 'Gestión de Calibraciones'" + "\n" + ex.getMessage());
            Alertas.alertaAbrirEditor("calibrados");
        }
        return false; // si llegamos aquí el usuario ha cancelado o cerrado el formulario.
    }
        
    // Muestra una nueva ventana con la grafica de un calibrado
    @FXML
    private void muestraGrafica(ActionEvent event) {        
        Calibrado seleccionado = tvCalibrados.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pasegados/labo/calibraciones/Grafica.fxml"));                
                Parent root = loader.load();
               
                controladorGrafica = (GraficaControlador) loader.getController();                
                controladorGrafica.setCalibrado(seleccionado);
                
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Gráfica " + seleccionado.getNombre() + " - " + seleccionado.getTipoRegresion());

                controladorGrafica.setScene(scene);
                controladorGrafica.dibuja();
                stage.setScene(scene);
                stage.setScene(scene);
                stage.setMinHeight(600);
                stage.setMinWidth(800);

                stage.showAndWait(); // mostramos y esperamos a que el usuario cierre la ventana de la gráfica

            } catch (IOException e) { // Error al localizar el FXML de la gráfica
                LOGGER.fatal("CALIBRADO: Error al abrir la gráfica del calibrado " + seleccionado.getNombre() + "\n" + e.getMessage());      
                Alertas.alertaIOException(e.toString());
            }
        } else { // No se ha seleccionado ningún calibrado
            Alertas.alertaGrafica();
        }
    }
    
    // Muestra el informe jasper reports del calibrado seleccionado
    @FXML
    private void verInforme(ActionEvent event) {        
    	Calibrado seleccionado = tvCalibrados.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            generaImagenGrafica(seleccionado); // Genero la imagen de la gráfica para el informe                
        
            try { // Intentamos generar informe
                JasperReport jasperReport = JasperCompileManager.compileReport(this.getClass().getResourceAsStream("informeCalibrado.jrxml"));		
                HashMap<String, Object> parameters = new HashMap<>();            
                JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(ColeccionCalibraciones.getColeccionCalibraciones()));
                JRViewer viewer = new JRViewer(print);
                viewer.setOpaque(true);
                viewer.setVisible(true);
                JasperViewer.viewReport(print, false);
            } catch (JRException e) {
                    LOGGER.fatal("CALIBRADO: Error al generar el informe del calibrado " + seleccionado.getNombre() + "\n" + e.getMessage());
                    Alertas.alertaInforme("calibrado" ,seleccionado.getNombre(), e.getMessage());
            }    	
        } else { // No se ha seleccionado ningún calibrado
            Alertas.alertaInformeNoSeleccionado();
        }
    }

    /**
     * Este método devuelve el controlador del editor/formulario de Calibraciones.
     * @return EditorCalibracionControlador con el controlador en uso
     */
    public EditorCalibracionControlador getControladorCalibrado() {
        return controladorCalibrado;
    }

    /**
     * Esté metodo permite asignar la lista de Calibrados de la que se va a nutrir la tableView de Calibrados
     * @param listaCalibrados ObservableList de tipo Calibrado con la que trabajar 
     */
    public void setListaCalibrados(ObservableList<Calibrado> listaCalibrados) {
        this.listaCalibrados = listaCalibrados;
        tvCalibrados.setItems(this.listaCalibrados);
    }

    /**
     * Esté metodo permite asignar la lista de Patrones de la que se va a nutrir la tableView de Patrones
     * @param listaPatrones ObservableList de tipo Patron con la que trabajar 
     */
    public void setListaPatrones(ObservableList<Patron> listaPatrones) {
        this.listaPatrones = listaPatrones;
        tvPatrones.setItems(this.listaPatrones);
    }
 
    /**
     * Este metodo devuelve una lista con un unico calibrado, seleccionado en la tabla, para generar su informe en
     * JasperReports mediante JavaBeans
     * @return ObservableList de tipo Calibrado con la que trabajar 
     */
    public ObservableList<Calibrado> getListaInforme() {    	
    	ObservableList<Calibrado> lista = FXCollections.observableArrayList();
    	lista.add(tvCalibrados.getSelectionModel().getSelectedItem());
    	return lista;        
    }
    
    // Guarda una imagen de la gráfica de un calibrado
    private void generaImagenGrafica(Calibrado seleccionado){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Grafica.fxml"));                
                Parent root = loader.load();
                Scene scene = new Scene(root);               
                controladorGrafica = (GraficaControlador) loader.getController();                
                controladorGrafica.setCalibrado(seleccionado);
                controladorGrafica.setScene(scene);
                controladorGrafica.dibuja(); // Crea la grafica
                controladorGrafica.guardaImagenPNG(scene, ".\\chart.png"); // Guarda la gráfica en el directorio padre con el nombre chart.jpg                
            } catch (IOException e) { // Error al localizar el FXML de la gráfica
                LOGGER.fatal("CALIBRADO: Error al acceder a la gráfica del calibrado " + seleccionado.getNombre() + "\n" + e.getMessage());
                Alertas.alertaIOException(e.toString());
            }
    }

    @FXML
    private void sincronizarCoeficientes(ActionEvent event) {
        
        Calibrado seleccionado = tvCalibrados.getSelectionModel().getSelectedItem();
        
        if (seleccionado != null) { // si hemos seleccionado uno en la tabla de calibrados
            
            // Alerta avisando al usuario de la importancia de la posición actual en el menú
            if (Alertas.alertaPrevioSincro()){
                Ajuste ajuste = seleccionado.getAjuste(); // rescatamos el ajuste que usa el calibrado            
                        
                String actualizarCoeficientes = ajuste.getSecuenciaAjusteCoeficientes(seleccionado);
            
                //System.out.println(actualizarCoeficientes);            
                HiloEnvioDatos envioDatos = new HiloEnvioDatos(actualizarCoeficientes, LOGGER);
                envioDatos.start();
            }            
        } else {
            Alertas.alertaErrorSincro();
        }        
    }
}
