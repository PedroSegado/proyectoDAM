package com.pasegados.labo.modelos;

import java.util.Objects;
import com.pasegados.labo.App;
import com.pasegados.labo.analisis.TabAnalisisControlador;
import com.pasegados.labo.calibraciones.EditorCalibracionControlador;
import org.apache.logging.log4j.Logger;


/**
 * Esta clase nos permite simular el acondicionamiento y ajuste de energía del equipo OXFORD, mostrando información del
 * proceso en pantalla durante el análisis.
 *
 * @author Pedro Antonio Segado Solano
 */
public class HiloAcondicionamiento extends Thread {

    private final static Configuracion CONFIG = App.getControladorConfiguracion().getConfiguracion();
    private final static Puerto PUERTO = App.getControladorConfiguracion().getPuerto();
    private TabAnalisisControlador controladorAnalisis; // Acceso al controlador de la vista
    private EditorCalibracionControlador controladorCalibra; // Acceso al controlador de la vista
    private boolean sePuedeAbortar; // Para controlar los punto exactos en los que el usuario puede abortar el acondicionamiento
    private boolean estaAcondicionando; // Para controlar si está realizando el acondicionamiento previo
    private boolean estaAjustandoEnergia; // Para controlar si está realizando el ajuste de energía
    private boolean terminado; // Para controlar si el acondicionamiento ha terminado completamente
    private final Logger logger;

    // CONSTRUCTORES
    public HiloAcondicionamiento(TabAnalisisControlador controlador, Logger logger) {
        this.controladorAnalisis = controlador;
        this.controladorCalibra = null;
        this.logger = logger;
        sePuedeAbortar = false; // Impide que de origen se pueda cancelar el proceso
        estaAcondicionando = false; // No se está acondicionado
        estaAjustandoEnergia = false; // No se está ajustando energia
        terminado = false; // No ha terminado el proceso de acondicionamiento del equipo
    }

    public HiloAcondicionamiento(EditorCalibracionControlador controlador, Logger logger) {
        this.controladorAnalisis = null;
        this.controladorCalibra = controlador;
        this.logger = logger;
        sePuedeAbortar = false; // Impide que de origen se pueda cancelar el proceso
        estaAcondicionando = false; // No se está acondicionado
        estaAjustandoEnergia = false; // No se está ajustando energia
        terminado = false; // No ha terminado el proceso de acondicionamiento del equipo
    }

    @Override
    public void run() {

        // INICIO DEL ACONDICIONAMIENTO: DIFERENCIAMOS ENTRE SI ES CONTROLADOR DE ANALISIS O DE CALIBRACION
        if (!Objects.isNull(controladorAnalisis)) {
            // Esperamos que termine el hilo que envia las pulsaciones de teclas al equipo
            try {
                controladorAnalisis.getEnvioDatos().join();
            } catch (InterruptedException ex) {
                logger.fatal("Error esperando el final del envío de datos" + "\n" + ex.getMessage());
            }

            if (controladorAnalisis.getEnvioDatos().getComunicado()) { // Si ha comunicado correctamente
                controladorAnalisis.setFuente("pequenya"); // Cambia a letra de menor tamaño para mostrar mensajes
                conectaRX(); // Enciende el aviso de RX conectados

                // Primera pantalla de pre-acondicionamiento durante el tiempo indicado en la configuracion
                controladorAnalisis.setResultado("Iniciando acondicionamiento ...");
                duerme(CONFIG.getPreAcondicionamiento());
                sePuedeAbortar = true; // El usuario ya puede detener el ensayo desde este punto
                estaAcondicionando = true; // El equipo ya puede acondicionar

                // Segunda pantalla de acondicionamiento donde se hace la cuenta atras de x segundos, que acaba
                // tras mostrar el 1, sin llegar a 0
                for (int i = (CONFIG.getAcondicionamiento()); i >= 1 & sePuedeAbortar; i--) {
                    controladorAnalisis.setResultado("Acondicionamiento:  " + i + "\"");
                    if (i > 1) {
                        duerme(1000);
                    } else {
                        duerme(200); // El 1 se muestra muy poco tiempo en pantalla
                    }
                }

                // Si el usuario ha abortado durante el acondicionamiento  
                if (!estaAcondicionando) {
                    controladorAnalisis.setResultado("Abortado durante el acondicionamiento");
                    desconectaRX(); // apagamos el aviso de RX
                }

                // Tercera pantalla: "Pre-Ajuste de Energia", si el usuario no ha abortado previamente el proceso
                if (sePuedeAbortar) {
                    estaAcondicionando = false; // finaliza la parte de acondicionamiento
                    sePuedeAbortar = false; // durante el pre-ajuste no se puede abortar                  
                    controladorAnalisis.setResultado("Iniciando ajuste de energia ...");
                    duerme(CONFIG.getPreEnergia()); // Esperamos el tiempo de pre-energia establecido en configuracion-preenergia
                    sePuedeAbortar = true; // ya se puede abortar el ajuste de energia
                    estaAjustandoEnergia = true; // el equipo esta ajustando

                    // Cuarta pantalla: ajuste de energia, donde se hace la cuenta atras de "x" segundos, establecidos en configuracion-energia
                    for (int i = CONFIG.getEnergia(); i >= 0 & sePuedeAbortar; i--) { // mientras se pueda abortar
                        controladorAnalisis.setResultado("Ajuste de energia:  " + i + "\"");
                        duerme(1000);
                    }

                    if (!estaAjustandoEnergia) { // Si el ajuste de energia es abortado por el usuario
                        controladorAnalisis.setResultado("Abortado durante ajuste de energia");
                        desconectaRX();
                    } else { // El ajuste del equipo termina comletamente todas sus etapas
                        controladorAnalisis.setResultado("");
                        terminado = true;
                    }
                }
            }
        }

        // LLEGAMOS DESDE LA PESTAÑA DE CALIBRACION
        if (!Objects.isNull(controladorCalibra)) {
            // Esperamos que termine el hilo que envia las pulsaciones de teclas al equipo
            try {
                controladorCalibra.getEnvioDatos().join();
            } catch (InterruptedException ex) {
                logger.fatal("Error esperando el final del envío de datos" + "\n" + ex.getMessage());
            }

            if (controladorCalibra.getEnvioDatos().getComunicado()) { // Si ha comunicado correctamente

                // Primera pantalla: Esperamos el tiempo de pre-acondicionamiento    
                duerme(CONFIG.getPreAcondicionamiento());

                sePuedeAbortar = true; // El usuario ya puede detener el ensayo desde este punto
                estaAcondicionando = true; // El equipo esta acondicionando

                // Segunda pantalla: Acondicionamiento donde se hace la cuenta atras de "x" segundos                 
                for (int i = 10; i >= 1 & sePuedeAbortar; i--) {                    
                    if (i > 1) {
                        duerme(1000);
                    } else {
                        duerme(200); // El 1 se muestra muy poco tiempo en pantalla
                    }
                }

                // Tercera Pantalla: Pre-Ajuste de Energia, si no se ha abortado el acondicionamiento previo        
                if (sePuedeAbortar) {
                    estaAcondicionando = false; // finaliza la parte de acondicionamiento
                    sePuedeAbortar = false; // los segundos de pre-ajuste no se puede abortar                                      
                    duerme(CONFIG.getPreEnergia()); // Esperamos el tiempo de pre-energia establecido en configuracion-preenergia
                    sePuedeAbortar = true; // ya se puede abortar el ajuste de energia
                    estaAjustandoEnergia = true; // el equipo esta ajustando

                    // Cuarta pantalla: ajuste de energia, donde se hace la cuenta atras de "x" segundos, establecidos en configuracion-energia
                    for (int i = CONFIG.getEnergia(); i >= 0 & sePuedeAbortar; i--) { // mientras se pueda abortar                        
                        duerme(1000);
                    }

                    if (estaAjustandoEnergia) { // Si el ajuste de energia no se ha abortado, todo el procesao ha terminado correctamente                        
                        terminado = true;
                    }
                }
            }
        }
    }

    // GETTERS Y SETTERS
    /**
     * Devuelve si el usuario puede abortar o no el análisis
     *
     * @return boolean true si se puede abortar o false si no se puede
     */
    public boolean getSePuedeAbortar() {
        return sePuedeAbortar;
    }

    /**
     * Establece si el usuario puede abortar o no el análisis
     *
     * @param estado boolean true si se puede abortar o false si no se puede
     */
    public void setSePuedeAbortar(boolean estado) {
        sePuedeAbortar = estado;
    }

    /**
     * Devuelve si el proceso está en el momento de acondicionamiento o no
     *
     * @return boolean true si esta acondicionando o false si no lo está
     */
    public boolean getEstaCond() {
        return estaAcondicionando;
    }

    /**
     * Establece si el proceso está en el momento de acondicionamiento o no
     *
     * @param estado boolean true si esta acondicionando o false si no lo está
     */
    public void setEstaCond(boolean estado) {
        estaAcondicionando = estado;
    }

    /**
     * Devuelve si el proceso está en el momento de ajuste de energía o no
     *
     * @return boolean true si esta ajustando energia o false si no lo está
     */
    public boolean getEstaAjustEner() {
        return estaAjustandoEnergia;
    }

    /**
     * Establece si el proceso está en el momento de ajuste de energía o no
     *
     * @param estado boolean true si esta ajustando energia o false si no lo está
     */
    public void setEstaAjustEner(boolean estado) {
        estaAjustandoEnergia = estado;
    }

    /**
     * Devuelve si el proceso ha terminado completamente o si se ha interrumpido por acción del usuario
     *
     * @return boolean true si ha terminado correctamente o false si ha sido interrumpido
     */
    public boolean getTerminado() {
        return terminado;
    }

    /**
     * Establedce si el proceso ha terminado completamente o si se ha interrumpido por acción del usuario
     *
     * @param estado boolean true si ha terminado correctamente o false si ha sido interrumpido
     */
    public void setTerminado(boolean estado) {
        terminado = estado;
    }

    //METODOS PARA USO INTERNO DE LA CLASE
    
    // Permite para el proceso durante unos milisegundos
    private void duerme(int milisegundos) {
        try {
            sleep(milisegundos); //13 segudos
        } catch (InterruptedException ex) {
        }
    }

    // Activa el aviso de Rayos X encendidos
    private void conectaRX() {
        controladorAnalisis.getImagenDcha().setVisible(true);
        controladorAnalisis.getImagenIzq().setVisible(true);
    }

    // Desactiva el aviso de Rayos X encendidos
    private void desconectaRX() {
        controladorAnalisis.getImagenDcha().setVisible(false);
        controladorAnalisis.getImagenIzq().setVisible(false);
    }
}
