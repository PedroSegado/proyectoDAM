package com.pasegados.labo.modelos;

import com.fazecast.jSerialComm.SerialPort;
import com.pasegados.labo.App;
import org.apache.logging.log4j.Logger;


/**
 * Esta clase envía por el puerto COM establecido en la configuración, las pulsaciones requeridas para el funcionamiento
 * del equipo, según el comportamiento esperado.
 *
 * @author Pedro A. Segado Solano
 */
public class HiloEnvioDatos extends Thread {

    private final static Configuracion CONFIG = App.getControladorConfiguracion().getConfiguracion();
    private final static Puerto PUERTO = App.getControladorConfiguracion().getPuerto();
    private String tipo;
    private Calibrado calibrado;
    private boolean comunicado;
    private final Logger logger;
    

    // CONSTRUCTORES
    public HiloEnvioDatos(String tipo, Calibrado calibrado, Logger logger) {
        this.tipo = tipo;
        this.calibrado = calibrado;
        this.logger = logger;
        comunicado = false; // Cambiará a true cuando termine el envío
    }

    public HiloEnvioDatos(String tipo, Logger logger) {
        this.tipo = tipo;
        this.logger = logger;
        comunicado = false; // Cambiará a true cuando termine el envío
    }

    @Override
    public void run() {
        // Booleano para comprobación de conexion abierta en puerto
        boolean conexion = false;

        // Si el puerto ya fué abierto previamente
        if (PUERTO.isAbierto()) {
            conexion = true;
        } else {
            conexion = PUERTO.activaPuerto();
        }

        // Si tenemos conexion correcta
        if (conexion) {
            SerialPort puertoSerieActivo = PUERTO.getPuertoActivo();

            String secuencia = "";
            //Preparamos la secuencia de pulsaciones a enviar según el euste
            switch (tipo) {
                case "escape" ->
                    secuencia = "esc"; // Tecla ESCAPE del equipo
                case "abortar" ->
                    secuencia = "abortar"; // Abortar proceso de medida
                case "analizar" ->
                    secuencia = App.getControladorConfiguracion().getPulsacionesAjuste(calibrado.getAjuste().getNombre());                
                default ->
                    secuencia = tipo;
            }

            //Creo array de strings con cada dato separando por las comas de la secuencia a realizar
            String[] secuenciaArray = secuencia.split(",");

            //Envio las pulsaciones al OXFORD
            for (String pulsacion : secuenciaArray) {
                switch (pulsacion) {
                    case "esc" -> {
                        puertoSerieActivo.writeBytes(new byte[]{(byte) 0x1b}, 1);
                    }
                    case "abortar" -> {
                        puertoSerieActivo.writeBytes(new byte[]{(byte) 0x1b}, 1);
                        esperar(CONFIG.getPulsaciones());
                        puertoSerieActivo.writeBytes(new byte[]{(byte) 0x1b}, 1);
                    }
                    case "ENT" -> {
                        puertoSerieActivo.writeBytes(new byte[]{(byte) 0x0d, (byte) 0x0a}, 2);
                    }
                    case "del" -> {
                        puertoSerieActivo.writeBytes(new byte[]{(byte) 0x08}, 1);
                    }
                    case "izq" -> {
                        puertoSerieActivo.writeBytes(new byte[]{(byte) 0x1b, (byte) 0x5b, (byte) 0x44}, 3); // ESC,[,D                        
                    }
                    case "dcha" -> {
                        puertoSerieActivo.writeBytes(new byte[]{(byte) 0x1b, (byte) 0x5b, (byte) 0x43}, 3); // ESC,[,C                        
                    }
                    default -> { // en este caso la secuencia pueden ser palabras o numeros enviados por el usuario                         
                        byte[] sendData = new byte[pulsacion.length()];
                        for (int i = 0; i < pulsacion.length(); i++) {
                            sendData[i] = (byte) pulsacion.charAt(i); // preparamos el array de bytes con los caracteres recibidos                            
                        }
                        puertoSerieActivo.writeBytes(sendData, pulsacion.length());                        
                    }
                }

                esperar(CONFIG.getPulsaciones());  // esperamos entra cada pulsación el tiempo indicado
            }
            // Cerramos el puerto una vez hemos transmitido todo siempre y cuando no este activa la lectura continua en "Utilidades"
            if (!App.getControladorUtilidades().getLecturaContinua()) {
                PUERTO.cierraConexion();
            }

            comunicado = true; // Para ver si el hilo ha comunicado correctamente
        } else { // Si no hay conexión lanzamos Alerta
            comunicado = false;
            Alertas.alertaPuerto();
            logger.fatal("Error al intentar abrir el puerto para enviar datos");             
        }
    }

    // Esperar x milisegundos
    private void esperar(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    // Devuelve booleano que indica si se ha completado la comunicación o no
    public boolean getComunicado() {
        return comunicado;
    }
}
