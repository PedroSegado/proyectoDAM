package com.pasegados.labo;

import com.pasegados.labo.analisis.TabAnalisisControlador;
import com.pasegados.labo.calibraciones.TabCalibracionesControlador;
import com.pasegados.labo.conexionesbbdd.Conexion;
import com.pasegados.labo.configuracion.TabConfiguracionControlador;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.resultados.TabResultadosControlador;
import com.pasegados.labo.utilidades.TabUtilidadesControlador;
import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase de carga inicial del programa que lanza la GUI de JavaFX
 *
 * @author Predro A. Segado Solano
 */
public class App extends Application {
    // Todos los controladores, para poder acceder a ellos facilmente
    private static TabViewControlador controladorPrincipal;
    private static TabAnalisisControlador controladorAnalisis;
    private static TabResultadosControlador controladorResultados;
    private static TabCalibracionesControlador controladorCalibrados;
    private static TabUtilidadesControlador controladorUtilidades;
    private static TabConfiguracionControlador controladorConfiguracion;
    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private Stage stage; // Stage creado por si hay que reiniciar la App actuar sobre el mismo
    private static App app; // Devuelve esta clase, por si hay que llamar al metodo de reiniciarla

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {           
        this.stage = stage;
        App.app = this;
        //Comprobamos existencia BBDD: si existe podemos iniciar el programa               
        if (Conexion.getINSTANCIA().existeBBDD()){             
            LOGGER.info("Iniciando carga de la aplicación ...");            
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TabView.fxml"));
                Parent root = loader.load();
                // Al cargar el loader, se llaman a todos los metodos initialize anidados en TabView.fxml, y aprovechamos
                // cada unos de ellos para que use su setter en esta clase, para tener todos disponibles para interacturar.
                Scene scene = new Scene(root);
                stage.setTitle("Módulo controlador OXFORD LabX-3500");
                stage.setMinHeight(769d); // Esta resolución ajusta perfectamente a monitor 1280x800 (16:10)
                stage.setMinWidth(1293d); // manteniendo la barra de tareas de windows visible
                stage.setScene(scene);

                stage.setOnCloseRequest(event -> {
                    LOGGER.info("Usuario cierra la aplicación ...");
                });
                stage.show();
            } catch (IOException ex) {
                LOGGER.fatal("Error al cargar 'TabView.fxml' al inicio " + "\n" + ex.getMessage());
                Alertas.alertaCargaInicial(ex.getLocalizedMessage());
            }
        }
        else{ // No existe la BBDD, vamos a crearla            
            boolean crearBBDD = Alertas.alertaCrearNuevaBBDD();
            if (crearBBDD){
                try {                
                    Conexion.getINSTANCIA().crearEstructura(); // Patron singleton, una única instancia de Conexion
                    Alertas.alertaBBDDCreada();
                    reiniciar(); // reiniciar carga de la aplicación
                } catch (SQLException ex) {
                    LOGGER.fatal("Error al crear la BBDD" + "\n" + ex.getMessage());
                    Alertas.alertaBBDDErrorCrear();                
                }
            } else{
                //el ususario ha cancelado la creacion de la BBDD, se cerrará la aplicación al terminar este hilo
            }            
        }             
    }

    // GETTERS Y SETTERS CONTROLADORES PRINCIPAL Y DE CADA UNA DE LAS PESTAÑAS
    
    /**
     * Este método devuelve el controlador principal que contiene las pestañas
     *
     * @return TabViewControlador con el controlador principal que a su vez carga los demás
     */
    public static TabViewControlador getControladorPrincipal() {
        return controladorPrincipal;
    }

    /**
     * Este método establece el controlador principal que contiene las pestañas
     *
     * @param controladorPrincipal TabViewControlador con el controlador principal
     */
    public static void setControladorPrincipal(TabViewControlador controladorPrincipal) {
        App.controladorPrincipal = controladorPrincipal;
    }

    /**
     * Este método devuelve el controlador de la pestaña "Configuracion"
     *
     * @return TabConfiguracionControlador con el controlador de la pestaña "Configuracion"
     */
    public static TabConfiguracionControlador getControladorConfiguracion() {
        return controladorConfiguracion;
    }

    /**
     * Este método establece el controlador de la pestaña "Configuracion"
     *
     * @param controladorConfiguracion TabConfiguracionControlador con el controlador de la pestaña "Configuracion"
     */
    public static void setControladorConfiguracion(TabConfiguracionControlador controladorConfiguracion) {
        App.controladorConfiguracion = controladorConfiguracion;
    }

    /**
     * Este método devuelve el controlador de la pestaña "Analisis"
     *
     * @return TabAnalisisControlador con el controlador de la pestaña "Analisis"
     */
    public static TabAnalisisControlador getControladorAnalisis() {
        return controladorAnalisis;
    }

    /**
     * Este método establece el controlador de la pestaña "Analisis"
     *
     * @param controladorAnalisis TabAnalisisControlador con el controlador de la pestaña "Analisis"
     */
    public static void setControladorAnalisis(TabAnalisisControlador controladorAnalisis) {
        App.controladorAnalisis = controladorAnalisis;
    }

    /**
     * Este método devuelve el controlador de la pestaña "Calibraciones"
     *
     * @return TabCalibracionesControlador con el controlador de la pestaña "Calibraciones"
     */
    public static TabCalibracionesControlador getControladorCalibrados() {
        return controladorCalibrados;
    }

    /**
     * Este método establece el controlador de la pestaña "Calibraciones"
     *
     * @param controladorCalibrados TabCalibracionesControlador con el controlador de la pestaña "Calibraciones"
     */
    public static void setControladorCalibrados(TabCalibracionesControlador controladorCalibrados) {
        App.controladorCalibrados = controladorCalibrados;
    }

    /**
     * Este método devuelve el controlador de la pestaña "Resultados"
     *
     * @return TabResultadosControlador con el controlador de la pestaña "Resultados"
     */
    public static TabResultadosControlador getControladorResultados() {
        return controladorResultados;
    }

    /**
     * Este método establece el controlador de la pestaña "Resultados"
     *
     * @param controladorResultados TabResultadosControlador con el controlador de la pestaña "Resultados"
     */
    public static void setControladorResultados(TabResultadosControlador controladorResultados) {
        App.controladorResultados = controladorResultados;
    }

    /**
     * Este método devuelve el controlador de la pestaña "Utilidades"
     *
     * @return TabUtilidadesControlador con el controlador de la pestaña "Utilidades"
     */
    public static TabUtilidadesControlador getControladorUtilidades() {
        return controladorUtilidades;
    }

    /**
     * Este método establece el controlador de la pestaña "Utilidades"
     *
     * @param controladorUtilidades TabUtilidadesControlador con el controlador de la pestaña "Utilidades"
     */
    public static void setControladorUtilidades(TabUtilidadesControlador controladorUtilidades) {
        App.controladorUtilidades = controladorUtilidades;
    }
    
    
    // Método para reiniciar la aplicación
    public void reiniciar() {
        try {
            stop(); // Llamada al método stop() para detener la aplicación actual
        } catch (Exception ex) {
            LOGGER.fatal(ex.getMessage());
        }
        start(stage); // Iniciar una nueva instancia de la aplicación
    }
    
    public static App getApp(){
        return app;
    }
    
 
}
