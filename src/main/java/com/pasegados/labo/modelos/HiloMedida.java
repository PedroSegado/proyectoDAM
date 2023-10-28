package com.pasegados.labo.modelos;

import java.util.Objects;
import com.pasegados.labo.App;
import com.pasegados.labo.analisis.TabAnalisisControlador;
import com.pasegados.labo.calibraciones.EditorCalibracionControlador;
import org.apache.logging.log4j.Logger;



/**
 * Esta clase nos permite simular el ajuste de energia del equipo OXFORD,
 * realizando una cuenta atrás sobre el textArea del controlador, actualizando
 * los segundos de manera visual, ya que desde la misma clase solo se mostraría
 * la última actualización al finalizar el correspondiente bucle.
 *
 * @author Pedro Antonio Segado Solano
 */
public class HiloMedida extends Thread {
    
    private final static Configuracion CONFIG = App.getControladorConfiguracion().getConfiguracion();
    private final TabAnalisisControlador CONTROLADOR_ANALISIS; // Acceso al controlador de la vista
    private final EditorCalibracionControlador CONTROLADOR_CALIB; // Acceso al controlador de la vista
    private final int VALOR; // Valor inicial en segundos de la cuenta atrás
    private boolean midiendo;
    private final Logger logger;

    public HiloMedida(TabAnalisisControlador controlador, int valor, Logger logger) {
        this.CONTROLADOR_ANALISIS = controlador;
        this.CONTROLADOR_CALIB = null;        
        this.VALOR = valor;
        this.logger = logger;
        this.midiendo = false;
    }

    public HiloMedida(EditorCalibracionControlador controlador, int valor, Logger logger) {
        this.CONTROLADOR_ANALISIS = null;
        this.CONTROLADOR_CALIB = controlador;
        this.VALOR = valor;
        this.logger = logger;
        this.midiendo = false;
    }

    @Override
    public void run() {
                
        // Si usamos el contructor con el controlador del TabAnalisis
        if (!Objects.isNull(CONTROLADOR_ANALISIS)){ // No es nulo, mientras que el otro controlador si lo es   
            try {
                CONTROLADOR_ANALISIS.getAcondicionamiento().join(); //Esperamos que acabe el proceso de acondicionamiento
            } catch (InterruptedException ex) {
                logger.fatal(" Error esperando que termine el acondicionamiento del equipo" + "\n" + ex.getMessage());
            }

            //Solo se ejecuta si el ajuste previo termino y no fue abortado por el usuario
            if (CONTROLADOR_ANALISIS.getAcondicionamiento().getTerminado()) {
                
                CONTROLADOR_ANALISIS.setResultado("Empezando medida"); // Mostramos este texto en el controlador
                duerme(CONFIG.getPreMedida()); // Durante el tiempo establecido en configuracion-premedida
                
                this.midiendo = true; // Ahora estamos midiendo
                
                // Cuenta atras por los segundos indicados en VALOR (depende del ajuste de la calibración)      
                for (int i = VALOR; i >= 0 & midiendo; i--) { //Mientras midiendo porque usuario no ha abortado
                    if (i > 4) {
                        CONTROLADOR_ANALISIS.setResultado("Midiendo:  " + i + "\"");
                    }
                    if (i == 4) { // Cuando quedan 4 segundos nos preparamos para recibir datos abriendo el puerto COM
                        CONTROLADOR_ANALISIS.setResultado("Esperando lectura de cuentas ...");
                        CONTROLADOR_ANALISIS.recibirDatos(); //Comenzamos la lectura de las cuentas en el puerto
                    }
                    duerme(1000);
                }

                if (midiendo) { //Si se completa la medida, midiendo sigue siendo "true"                
                    CONTROLADOR_ANALISIS.setResultado("");
                    CONTROLADOR_ANALISIS.setFuente("normal");
                } else { // Hemos abortado y midiendo es "false"
                    CONTROLADOR_ANALISIS.setResultado("Abortado durante la medida");                   
                }
                desconectaRX(); // Desactivamos las imagenes de rayos X
            }
        }

        // Si usamos el contructor con el controlador del TabEditorCalib        
        if (!Objects.isNull(CONTROLADOR_CALIB)) { // No es nulo, mientras que el otro controlador si lo es           
            try {
                CONTROLADOR_CALIB.getAcondicionamiento().join(); //Esperamos que acabe el proceso de acondicionamiento
            } catch (InterruptedException ex) {
                logger.fatal("Error esperando que termine el acondicionamiento del equipo" + "\n" + ex.getMessage());
            }
                                   
            //Solo se ejecuta si el ajuste previo termino y no fue abortado por el usuario
            if (CONTROLADOR_CALIB.getAcondicionamiento().getTerminado()) {                
                this.midiendo = true;// Ahora estamos midiendo
                
                // Cuenta atras por los segundos indicados en VALOR (depende del ajuste de la calibración)
                for (int i = VALOR; i >= 0 & midiendo; i--) {                    
                    if (i == 4) {                        
                        CONTROLADOR_CALIB.recibirDatos(); //Comenzamos la lectura de las cuentas
                    }
                    duerme(1000);
                }
            }
            else{
                //NO hacemos nada, ya que el usuario habrá abortado durante el acondicionamiento                
            }
        }
    }

    // GETTERS Y SETTERS
    
    /**
     * Establece el valor para indicar si esta midiendo o se ha abortado
     * 
     * @param estado boolean true si esta midiendo o false si se ha abortado
     */
    public void setMidiendo(boolean estado) {
        midiendo = estado;
    }

    /**
     * Devuelve el valor que indica si esta midiendo o se ha abortado
     * 
     * @return booleano true si esta midiendo o false si se ha abortado
     */
    public boolean getMidiendo() {
        return midiendo;
    }

    // METODOS DE USO INTERNO
    
    // Duerme el hilo durante x milisegundos
    private void duerme(int milisegundos) {
        try {
            sleep(milisegundos); //13 segudos
        } catch (InterruptedException ex) {}
    }

    // Oculta la imagen de los rayos X
    private void desconectaRX() {
        CONTROLADOR_ANALISIS.getImagenDcha().setVisible(false);
        CONTROLADOR_ANALISIS.getImagenIzq().setVisible(false);
    }
}
