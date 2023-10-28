package com.pasegados.labo.utilidades;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.pasegados.labo.App;
import com.pasegados.labo.modelos.HiloEnvioDatos;
import com.pasegados.labo.modelos.Puerto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Esta clase representa al controlador de la pestaña Utilidades
 *
 * @author Pedro Antonio Segado Solano
 */
public class TabUtilidadesControlador {

    // TextField
    @FXML
    private TextField tfPantalla;

    // TextArea
    @FXML
    private TextArea taDatos;

    // Botones
    @FXML
    private Button btEscape;
    @FXML
    private Button bt7;
    @FXML
    private Button bt8;
    @FXML
    private Button bt9;
    @FXML
    private Button btYes;
    @FXML
    private Button bt4;
    @FXML
    private Button bt5;
    @FXML
    private Button bt6;
    @FXML
    private Button btNo;
    @FXML
    private Button btImprimir;
    @FXML
    private Button bt0;
    @FXML
    private Button bt1;
    @FXML
    private Button bt2;
    @FXML
    private Button bt3;
    @FXML
    private Button btDel;
    @FXML
    private Button btSpace;
    @FXML
    private Button btPunto;
    @FXML
    private Button btEnter;
    @FXML
    private ToggleButton btArriba;
    @FXML
    private ToggleButton btAbajo;
    @FXML
    private ToggleButton btSecundaria;
    @FXML
    private ToggleGroup especial;
    @FXML
    private Button btCopiar;
    @FXML
    private Button btGuardar;
    @FXML
    private Button btLimpiar;

    // Radio-Buttons
    @FXML
    private ToggleGroup lecturaContinua;
    @FXML
    private RadioButton rbNo;
    @FXML
    private RadioButton rbSi;
    
    // Otras variables
    private static final Logger LOGGER = LogManager.getLogger(TabUtilidadesControlador.class);
    
    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {
        App.setControladorUtilidades(this); // Establecemos en la clase App que este es el controlador FXML del la Tab Utilidades
        Font.loadFont(getClass().getResourceAsStream("Digital7-rg1mL.ttf"), 12); // Carga tipo de letras especial
    }

    // --------------------- TECLADO --------------------- //
    @FXML
    private void enviarTeclaA7B(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "A";
        } else if (btAbajo.isSelected()) {
            tecla = "B";
        } else {
            tecla = "7";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaEscape(ActionEvent event) {
        enviarOxford("esc");
    }

    @FXML
    private void enviarTeclaC8D(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "C";
        } else if (btAbajo.isSelected()) {
            tecla = "D";
        } else {
            tecla = "8";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaE9F(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "E";
        } else if (btAbajo.isSelected()) {
            tecla = "F";
        } else {
            tecla = "9";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaGYesH(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "G";
        } else if (btAbajo.isSelected()) {
            tecla = "H";
        } else if (btSecundaria.isSelected()) {
            tecla = "/";
        } else {
            tecla = "y";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaI4J(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "I";
        } else if (btAbajo.isSelected()) {
            tecla = "J";
        } else {
            tecla = "4";
        }
        enviarOxford(tecla);    }

    @FXML
    private void enviarTeclaK5L(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "K";
        } else if (btAbajo.isSelected()) {
            tecla = "L";
        } else {
            tecla = "5";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaM6N(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "M";
        } else if (btAbajo.isSelected()) {
            tecla = "N";
        } else {
            tecla = "6";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaONoP(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "O";
        } else if (btAbajo.isSelected()) {
            tecla = "P";
        } else if (btSecundaria.isSelected()) {
            tecla = "#";
        } else {
            tecla = "n";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaQ0R(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "Q";
        } else if (btAbajo.isSelected()) {
            tecla = "R";
        } else {
            tecla = "0";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaS1T(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "S";
        } else if (btAbajo.isSelected()) {
            tecla = "T";
        } else {
            tecla = "1";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaU2V(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "U";
        } else if (btAbajo.isSelected()) {
            tecla = "V";
        } else {
            tecla = "2";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaW3X(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "W";
        } else if (btAbajo.isSelected()) {
            tecla = "X";
        } else {
            tecla = "3";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaYDelZ(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "Y";
        } else if (btAbajo.isSelected()) {
            tecla = "Z";
        } else {
            tecla = "del";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarTeclaIgualEspacioMas(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "=";
        } else if (btAbajo.isSelected()) {
            tecla = "+";
        } else if (btSecundaria.isSelected()) {
            tecla = "(";
        } else {
            tecla = " ";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarIzqPuntoDcha(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "izq";
        } else if (btAbajo.isSelected()) {
            tecla = "dcha";
        } else if (btSecundaria.isSelected()) {
            tecla = "*";
        } else {
            tecla = ".";
        }
        enviarOxford(tecla);
    }

    @FXML
    private void enviarMenosEntPorc(ActionEvent event) {
        String tecla;
        if (btArriba.isSelected()) {
            tecla = "-";
        } else if (btAbajo.isSelected()) {
            tecla = "%";
        } else if (btSecundaria.isSelected()) {
            tecla = ")";
        } else {
            tecla = "ENT";
        }
        enviarOxford(tecla);
    }

    // Este metodo envia la pulsación de una tecla al OXFORD por el puerto COM
    private void enviarOxford(String tecla) {
        if (tecla.equals("ENT") & (!tfPantalla.getText().contains("Enviado:") & !tfPantalla.getText().isEmpty())) {
            tecla = tfPantalla.getText();
        }
        // Envio pulsaciones al puerto mediante un hilo, para evitar retraso en la ejecución de este hilo
        HiloEnvioDatos envioDatos = new HiloEnvioDatos(tecla, LOGGER);
        envioDatos.start();

        try { // Vamos a esperar que termine el proceso del hilo para seguir
            envioDatos.join();
        } catch (InterruptedException ex) {
            LOGGER.fatal("Error en el envio de tecla " + tecla + " al OXFORD" + "\n" + ex.getMessage());
        }
        if (envioDatos.getComunicado()) { // Si el hilo ha comunicado con éxito con el equipo
            tfPantalla.setText("Enviado: " + tecla);
        }
    }

    // Permite recibir datos en continuo, sin cerrar el puerto, al actuvar el radioButton rbSi.
    @FXML
    private void abrirPuerto(ActionEvent event) {
        Puerto puerto = App.getControladorConfiguracion().getPuerto();
        boolean conexion = puerto.activaPuerto();
        if (conexion) {
            LOGGER.info("Puerto abierto: Preparado para recibir datos en continuo desde el OXFORD");            
            puerto.escucharPuerto("todo");
        }
    }

    // Permite cerrar el puerto al activar el radiobutton rbNo.
    @FXML
    private void cerrarPuerto(ActionEvent event) {

        App.getControladorConfiguracion().getPuerto().cierraConexion();
        LOGGER.info("Puerto cerrado: Terminada la lectura de datos desde el OXFORD");
    }

    // Copia el contenido del textArea al portapapeles
    @FXML
    private void copiarTextArea(ActionEvent event) {
        StringSelection stringSelection = new StringSelection(taDatos.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    // Guarda el contenido del textArea a un archivo de texto mediante un fileChooser
    @FXML
    private void guardarTextArea(ActionEvent event) {
        File archivo = selectorArchivoGrabar(); // Selección del archivo donde grabar

        if (archivo != null) { // Hemos seleccionado archivo y no cancelado en la ventana de selección
            try {
                FileWriter fw = new FileWriter(archivo, false); // Creamos archivo y machacamos si existe (false)
                fw.write(taDatos.getText());
                fw.close(); // Cerramos el fileWriter
            } catch (IOException e) {
                LOGGER.fatal("Error al guardar en archivo: " + e.getMessage());  
            }
        }
    }
    
    // Usado por guardaTextArea. Abre el selector de archivos para escoger un archivo donde grabar, si no existe lo crea     
    private File selectorArchivoGrabar() {
        Stage escenario = new Stage(); // Variable para alojar el escenario creado en el App
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setTitle("Seleccionar archivo destino de los datos recibidos desde el OXFORD");
        fileChooser.setInitialFileName("datosRecibidos.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt"));
        File file = fileChooser.showSaveDialog(escenario);
        return file; // Devuelve un File donde grabar o null si se cancela la seleccion	
    }

    // Borra por completo el textArea
    @FXML
    private void limpiarTextArea(ActionEvent event) {
        taDatos.clear();
    }

    /**
     * Este médoto permite añadir texto al textArea conforme se va recibiendo desde el equipo OXFORD por el puerto COM
     * @param texto String con la información recibida
     */
    public void addTextArea(String texto) {
        taDatos.appendText(texto);
        LOGGER.info("Recibido: " + texto);       
    }

    /**
     * Esté metodo permite comprobar si está activa la lectura continua, para evitar cerrar el puerto tras un envío de
     * datos.
     * @return boolean true si está marcado el radiobutton de lectura continua, false si no
     */
    public boolean getLecturaContinua(){
        if (rbSi.isSelected()){
            return true;
        }
        return false;
    }
}
