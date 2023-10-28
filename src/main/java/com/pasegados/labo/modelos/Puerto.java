
package com.pasegados.labo.modelos;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.pasegados.labo.App;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Esta clase representa a un puerto serie del PC en el que se ejecuta el Software
 * 
 * @author Pedro Antonio Segado Solano
 */
public class Puerto {

    private static SerialPort[] puertosSistema = SerialPort.getCommPorts(); //Puertos COM del sistema operativo

    private SerialPort puertoActivo;
    private String nombrePuerto;
    private int bps;
    private int bdd;
    private String paridad;
    private String bdp;
    private boolean abierto;

    // CONSTRUCTORES
    
    public Puerto() {
        this("No leido", 0,0,"No leido","No leido");
    }

    /**
     * Representa a un puerto del PC y su configuración para la correcta comunicación
     * 
     * @param nombrePuerto String con el nombre del puerto tal y como aparece en el sistema
     * @param bps int con los Bits Por Segundo 
     * @param bdd int con los Bits De Datos
     * @param paridad String con la Paridad del puerto
     * @param bdp String con los Bits De Parada
     */
    public Puerto(String nombrePuerto, int bps, int bdd, String paridad, String bdp) {
        this.nombrePuerto = nombrePuerto;
        this.bps = bps;
        this.bdd = bdd;
        this.paridad = paridad;
        this.bdp = bdp;
        this.abierto = false;
    }
    
    //GETTERS Y SETTER
    
    public static SerialPort[] getPuertosSistema() {
        return puertosSistema;
    }

    public static void setPuertosSistema(SerialPort[] puertosSistema) {
        Puerto.puertosSistema = puertosSistema;
    }

    public SerialPort getPuertoActivo() {
        return puertoActivo;
    }

    public void setPuertoActivo(SerialPort puertoActivo) {
        this.puertoActivo = puertoActivo;
    }

    public String getNombrePuerto() {
        return nombrePuerto;
    }

    public void setNombrePuerto(String nombrePuerto) {
        this.nombrePuerto = nombrePuerto;
    }

    public int getBps() {
        return bps;
    }

    public void setBps(int bps) {
        this.bps = bps;
    }

    public int getBdd() {
        return bdd;
    }

    public void setBdd(int bdd) {
        this.bdd = bdd;
    }

    public String getParidad() {
        return paridad;
    }

    public void setParidad(String paridad) {
        this.paridad = paridad;
    }

    public String getBdp() {
        return bdp;
    }

    public void setBdp(String bdp) {
        this.bdp = bdp;
    }

    public boolean isAbierto() {
        return abierto;
    }

    public void setAbierto(boolean abierto) {
        this.abierto = abierto;
    }
    
    
    
    
    //OTROS METODDOS
    public int buscaNumeroPuerto(String puerto) {

        int numPuerto = 0; //almacenará la posición numérica del puerto con el nombre indicado

        for (int i = 0; i < puertosSistema.length; i++) {
            if (puertosSistema[i].getSystemPortName().equals(puerto)) {
                numPuerto = i; //posición numerica del puerto dentro del array de puertos del sistema operativo
                break;
            }
        }
        return numPuerto;
    }

    
    //METODOS  
    
    public boolean activaPuerto() {

        //Busco el número de puerto en el sistema que corresponde a ese nombre de puerto
        try{
        puertoActivo = puertosSistema[buscaNumeroPuerto(nombrePuerto)];
        }catch (ArrayIndexOutOfBoundsException e){
           return false; //Si hay configurado un puerto pero el pc no tiene ningun puerto serie            
        }

        //Ajusto los valores del puerto a los indicados
        puertoActivo.setBaudRate(bps);
        puertoActivo.setNumDataBits(bdd);

        switch (paridad) {
            case "Ninguna" ->
                puertoActivo.setParity(SerialPort.NO_PARITY);
            case "Par" ->
                puertoActivo.setParity(SerialPort.EVEN_PARITY);
            case "Impar" ->
                puertoActivo.setParity(SerialPort.ODD_PARITY);
            case "Marca" ->
                puertoActivo.setParity(SerialPort.MARK_PARITY);
            case "Espacio" ->
                puertoActivo.setParity(SerialPort.SPACE_PARITY);
            default -> {
            }
        }

        switch (bdp) {
            case "1" -> puertoActivo.setNumStopBits(SerialPort.ONE_STOP_BIT);
            case "1.5" -> puertoActivo.setNumStopBits(SerialPort.ONE_POINT_FIVE_STOP_BITS);
            case "2" -> puertoActivo.setNumStopBits(SerialPort.TWO_STOP_BITS);
            default -> {
            }
        }

        //Ajusto de tiempos de lectura del puerto (TIMEOUT_Mode, READ_TIMEOUT_milliSec, WRITE_TIMEOUT_milliSec)
        puertoActivo.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0); //Timeout de 1 para lectura
        puertoActivo.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 0); // Timeout de 1 para escritura
        puertoActivo.flushIOBuffers();
        
        abierto = puertoActivo.openPort();
        return abierto; //Devuelve true si abre el puerto correctamente
        
    }

    public void escucharPuerto(String tipo) {
                   
            puertoActivo.addDataListener(new SerialPortDataListener() { //Añadimos escuchador al puerto

                String cadena = ""; //Almacena la cadena recibida por el puerto de comunicaciones

                @Override
                public void serialEvent(SerialPortEvent event) {

                    int size = event.getSerialPort().bytesAvailable();
                    byte[] buffer = new byte[size];
                    event.getSerialPort().readBytes(buffer, size);
                    puertoActivo.flushIOBuffers(); //eliminamos buffer

                    for (byte b : buffer) {
                        if (b == 0x0a) { //salto de linea, termina de leer linea completa
                            cadena = cadena + String.valueOf((char) b);
                            //System.out.print("lectura: " + cadena); //eliminado println para no saltar linea
                            
                            if (tipo.equals("todo")) App.getControladorUtilidades().addTextArea(cadena);

                            //Se comprueba si la linea es la del resultado de cuentas, y si coincide se muestra el resultado y se cierra la conexion
                            if (verificaTexto(cadena.substring(0, cadena.length() - 1))) {
                                String verifica = cadena.substring(0, cadena.length() - 1); //eliminamos el byte del salto de linea
                                String cuentas = verifica.replace("cps", "").replace(" ", "");
                                
                                if (tipo.equals("muestra")) {
                                    App.getControladorAnalisis().setCuentas(cuentas);                                    
                                } else if (tipo.equals("patron")) {
                                    App.getControladorCalibrados().getControladorCalibrado().setCuentas(cuentas);                                    
                                }
                            }
                            cadena = ""; //borramos la cadena para la siguiente captura de linea completa                            
                        } else {
                            cadena = cadena + String.valueOf((char) b);
                        }
                    }
                    puertoActivo.flushIOBuffers();
                }

                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }
            });
        
    }

    //Cierra conexion del puerto serie
    public void cierraConexion() {
        puertoActivo.removeDataListener(); 
        puertoActivo.flushIOBuffers(); 
        puertoActivo.closePort();
        abierto = false;
        //System.out.println("Puerto cerrado");
    }

    //Verifica que la linea completa (finaliza en salto de linea) concuerda con el patron de resultado de cuentas "cps"
    private boolean verificaTexto(String cadena) {
        Pattern pat = Pattern.compile("^[\\s]{1,2}[0-9]{4,5}[\\s]cps[\\s]{2,4}");
        Matcher mat = pat.matcher(cadena);
        return mat.matches();
    }
 
}
