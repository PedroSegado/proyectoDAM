package com.pasegados.labo.analisis;

import java.sql.SQLException;
import java.sql.Date;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pasegados.labo.App;
import com.pasegados.labo.conexionesbbdd.ConexionesResultados;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.modelos.Analisis;
import com.pasegados.labo.modelos.Calibrado;
import com.pasegados.labo.modelos.Filtros;
import com.pasegados.labo.modelos.HiloAcondicionamiento;
import com.pasegados.labo.modelos.HiloEnvioDatos;
import com.pasegados.labo.modelos.HiloMedida;
import com.pasegados.labo.modelos.Puerto;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Esta clase representa al controlador de la pestaña Analisis
 * 
 * @author Pedro Antonio Segado Solano
 */
public class TabAnalisisControlador {

    // TextFields
    @FXML
    private Button btAnalizarMuestra;
    @FXML
    private TextField tfResultadoMuestra;
    @FXML
    private TextField tfCuentasMuestra;

    // Botones    
    @FXML
    private TextField tfMuestra;
    @FXML
    private TextField tfIdentificacion;

    // Combos
    @FXML
    private ComboBox<String> cbMetodo;

    // ImageViews
    @FXML
    private ImageView ivIzquierda;
    @FXML
    private ImageView ivDerecha;

    // Otros (Puerto COM, conexiones a BBDD)
    private Puerto puerto;
    private final ConexionesResultados CNR = ConexionesResultados.getINSTANCIA_RESULTADOS();
    private HiloAcondicionamiento acond; // Para simular el acondicionamiento previo al análisis   
    private HiloMedida medida; // Para simular la cuenta atras en pantalla durante la medida
    private HiloEnvioDatos envioDatos; // Para enviar los datos necesarios por el puerto COM    
    private static final Logger LOGGER = LogManager.getLogger(TabAnalisisControlador.class);

    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {        
        App.setControladorAnalisis(this); // Establecemos en la clase App que este es el controlador FXML del la Tab Analisis
        tfMuestra.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(999999999))); // Para filtrar que solo se introduzcan dígitos y hasta un max. 999999999
        tfIdentificacion.setTextFormatter(new TextFormatter<>(Filtros.getMaxTamanioFilter(20))); // Para filtrar que solo admita un max. 20 caracteres   
        
        // Preparo el escuchador del textfield cuentas, encargado de que cada vez que cambie el texto, llame al método
        // que calcula el resultado en % de azufre en función del calibrado con el que se analiza y estas cuentas.
        tfCuentasMuestra.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String valorViejo, String valorNuevo) {
                if (!valorNuevo.equals("")) { // Si el valor recibido no es cadena vacia                                        
                    if(puerto.isAbierto()){ // si el puerto esta abierto                        
                        puerto.cierraConexion(); // podemos cerrar el puerto. Analisis terminado.
                        LOGGER.info("Analisis terminado. Puerto cerrado");
                    }
                    // Para actuar sobre los elementos fxml del interfaz necesitamos usar Platform.runLater
                    Platform.runLater(() -> { // ya que no se puede directamente desde el listener
                            // Configuramos la interfaz para un nuevo análisis
                            btAnalizarMuestra.setText("Analizar");
                            cbMetodo.setDisable(false);
                            tfMuestra.setDisable(false);
                            tfIdentificacion.setDisable(false);
                        }
                    );
                    // Rescatamos el objeto calibrado del tipo seleccionado en el analisis 
                    Calibrado calib = App.getControladorPrincipal().getCalibrado(cbMetodo.getValue());
                    // Calculamos el resultado en % azufre en función de este calibrado y las cuentas del tfCuentas
                    resultado(calib);
                }
            }
        });         
    }

    // Inicia el proceso de ánalisis tras pulsar el botón "Analizar"
    @FXML
    private void analizaMuestra(ActionEvent event) {            
        // Verificamos de que se han introducido los datos necesarios
        if (tfMuestra.getText().isEmpty() || cbMetodo.getSelectionModel().getSelectedIndex() == -1) {
            Alertas.alertaMuestra();
        } 
        else { // Si se han introducido todos los datos correctamente empezamos el proceso de análisis
            try { 
                // Si no existe esa PK en la BBDD, podemos analizar. 
                if (!CNR.existePK(Integer.valueOf(tfMuestra.getText()), tfIdentificacion.getText(), cbMetodo.getValue())) {
                    //Vamos a rescatar el objeto calibrado que coincide con el nombre seleccionado en cbMetodo
                    Calibrado calib = App.getControladorPrincipal().getCalibrado(cbMetodo.getValue());
                    
                    // Si en el botón pone "Analizar", empieza el ensayo                    
                    if (btAnalizarMuestra.getText().equals("Analizar")) { 
                        // Ajustamos el botón y resto de componentes mientras está analizando     
                        analizar(); 
                        // Se envían las pulsaciones al puerto mediante un hilo, para evitar retraso en la ejecución del hilo principal
                        envioDatos = new HiloEnvioDatos("analizar", calib, LOGGER);
                        envioDatos.start();
                        // Simulamos acondicionamiento del equipo, pasandole este controlador para que interactúe con él
                        acond = new HiloAcondicionamiento(this, LOGGER);
                        acond.start();
                        // Empieza la medida, con la cuenta atrás en segundos correspondiente a la duración del ajuste del calibrado
                        medida = new HiloMedida(this, calib.getAjuste().getDuracion(), LOGGER);
                        medida.start();

                    // Si en el botón pone "Abortar", abortamos en ensayo en curso    
                    } else if (btAnalizarMuestra.getText().equals("Abortar")) { 
                        // Durante el acondicionamiento
                        if (acond.getSePuedeAbortar() & acond.getEstaCond()) { // si se permite abortar y el acondicionamiento está en curso
                            acond.setSePuedeAbortar(false); // ya no se puede abortar mas, porque ha sido abortado
                            acond.setEstaCond(false); // ya no está acondicionando
                            abortar(); // ajustamos nuestro interfaz a la nueva situación
                        } // Durante el ajuste de energia
                        else if (acond.getSePuedeAbortar() & acond.getEstaAjustEner() & !acond.getTerminado()) { // si se puede abortar y el ajuste de energía esta en curso
                            acond.setSePuedeAbortar(false); // ya no se puede abortar mas                        // y además el acondionamiento ha terminado con éxito   
                            acond.setEstaAjustEner(false); // ya no esta ajsutando energia
                            abortar(); // ajustamos nuestro interfaz a la nueva situación
                        } // Durante la medida de la muestra
                        else if (medida.getMidiendo()) { // Si está ya en el proceso de medida 
                            medida.setMidiendo(false); // ya no está midiendo
                            abortar(); // ajustamos nuestro interfaz a la nueva situación
                        }
                    }                
                } else { // Ya existe la PK en la BBDD, avisamos al usuario para que cambie alguno de los 3 campos que forman la PK
                    Alertas.alertaPKAnalisis();
                }
            } catch (SQLException ex) {
                LOGGER.fatal("Error al comunicar con la BBDD durante la verificación de PK." + "\n" + ex.getMessage()); 
                Alertas.alertaBBDD(ex.getMessage());
            }
        }
    }

    // OTROSMETODOS NO FXML PARA ACCEDER DESDE OTRAS CLASES

    /**
     * Este método es llamado cuando quedan 5 segundos para terminar el análisis, permitiendo escuchar los datos que desde
     * ese momento se reciban por el puerto COM.
     */
    public void recibirDatos() {        
        puerto = App.getControladorConfiguracion().getPuerto();
        // Activamos el puerto para poder recibir datos
        boolean conexion = puerto.activaPuerto();
        if (conexion) { // conexión exitosa
            LOGGER.info("Puerto abierto, recibiendo datos de cuentas desde OXFORD");
            puerto.escucharPuerto("muestra");
        } else{ // fallo conexión
            LOGGER.fatal("ERROR al intentar abrir el puerto COM");
            abortar(); // cambiamos la interfaz para nuevo análisis
            Alertas.alertaRecibirDatos();
        }
    }
    
    /**
     * Este método permite establecer la lista de la que se alimenta el ComboBox cbMetodo.
     * @param lista ObservableList con los String de los nombres de todos los calibrados activos
     */
    public void setComboMetodos(ObservableList<String> lista) {
        cbMetodo.setItems(lista);
    }

    /**
     * Este método permite ajustar el texto del TextField tfCuentasMuestra.     
     * @param cuentas String con el dato de cuentas recibido por el puerto COM
     */
    public void setCuentas(String cuentas) {
        tfCuentasMuestra.setText(cuentas);
    }
    
    /**
     * Este método permite ajustar el texto del TextField tfResultadoMuestra para mostrar información del proceso de
     * medida.
     * @param texto String con la información a mostrar al usuario
     */
    public void setResultado(String texto) {
        tfResultadoMuestra.setText(texto);
    }

    /**
     * Este método permite ajustar el tipo y tamaño de la fuente del TextField tfResultadoMuestra     *
     * @param texto String indicando si la tipología es "normal" o "pequenya"
     */
    public void setFuente(String texto) {
        if (texto.equals("normal")) {
            tfResultadoMuestra.setFont(Font.font("System", FontWeight.BOLD, 126));
        }
        if (texto.equals("pequenya")) {
            tfResultadoMuestra.setFont(Font.font("System", FontWeight.BOLD, 32));
        }
    }

    /**
     * Este método permite tener acceso al HiloAcondicionamiento lanzado para la medida     *
     * @return HiloAcondicionamiento thread ejecutado para mostrar el acondicionamiento del equipo
     */
    public HiloAcondicionamiento getAcondicionamiento() {
        return acond;
    }

    /**
     * Este método pèrmite tener acceso al HiloEnvioDatos lanzado para comunicar al equipo las pulsaciones que ponen en
     * marcha el comportamiento esperado por parte del mismo (seleccion de calibrado, inicio de ensayo, etc...)     *
     * @return HiloEnvioDatos thread ejecutado para enviar datos al equipo
     */
    public HiloEnvioDatos getEnvioDatos() {
        return envioDatos;
    }

    /**
     * Este método permite acceder al ImageView de la derecha que muestra que los rayos X están activos     *
     * @return ImageView de los rayos X situado a la derecha de la pantalla
     */
    public ImageView getImagenDcha() {
        return ivDerecha;
    }

    /**
     * Este método permite acceder al ImageView de la izquierda que muestra que los rayos X están activos     *
     * @return ImageView de los rayos X situado a la izquierda de la pantalla
     */
    public ImageView getImagenIzq() {
        return ivIzquierda;
    }
    
    // OTROS METODOS NO FXML PARA USO PRIVADO INTERNO DE ESTA CLASE
    
    // Calculamos el resultado a partir de la cuentas, usando la ecuacion del calibrado, y lo guardamos en BBDD
    private void resultado(Calibrado c) {  
        setFuente("normal"); // El texto a tamaño normal para mostrar el resultado
        // Paso el String de tfCuentas a Float
        float cuentas = Float.valueOf(tfCuentasMuestra.getText()); 
        // Calculo el % azufre con los terminos de la ecuación del calibrado con el que he analizado
        double resultado = (c.getCoefCuadratico() * Math.pow(cuentas, 2)) + (c.getCoefLineal() * cuentas) + c.getTerminoIndep();        
        // Creo un string del resultado con 4 decimales
        String res = String.valueOf(new Formatter(Locale.US).format("%.4f", resultado)); // Formateado para usar . como decimal
        // Muestro el resultado en el textfield correspondiente
        tfResultadoMuestra.setText(res + " %");
        
        if (resultado <c.getMinimo()*0.9 || resultado >c.getMaximo()*1.1){
            tfResultadoMuestra.setStyle("-fx-text-fill: red;");
        }
                
        LOGGER.info("ANALIZADO: " + tfMuestra.getText() + " - " + tfIdentificacion.getText() + " - " + cbMetodo.getValue() 
                              + " con resultado " + tfResultadoMuestra.getText() + " y cuentas " + tfCuentasMuestra.getText());
        // Creamos objeto Analisis y lo configuramos con sus valores
        Analisis analisis = new Analisis();
        analisis.setMuestra(Integer.parseInt(tfMuestra.getText()));
        analisis.setIdentificacion(tfIdentificacion.getText());
        
        // Vamos a obtener la fecha actual sin la hora (con guardar fecha es suficiente)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Crear un objeto java.sql.Date a partir del Calendar  y asignarlo al atributo fecha del análisis
        Date fecha = new Date(calendar.getTimeInMillis());
        analisis.setFecha(fecha);
        analisis.setResultado(Float.parseFloat(res));
        analisis.setCalibrado(cbMetodo.getValue());
        analisis.setCuentas(Integer.parseInt(tfCuentasMuestra.getText()));

        // Guardamos el análisis en la BBDD y en la lista de últimos 25 resultados       
        try {
            CNR.insertar(analisis);
            App.getControladorPrincipal().getListaResultados().add(0, analisis);  //Insertamos en lista de ultimos 25 analisis
            if (App.getControladorPrincipal().getListaResultados().size() > 25) { // Si tenemos más de 25 objetos en la lista
                App.getControladorPrincipal().getListaResultados().remove(25);  //Eliminamos el antiguo 25 que ahora sería el 26
            }     
            LOGGER.info("Análisis guardado correctamente en BBDD"); 
        } catch (SQLException ex) {
            LOGGER.fatal("Error al comunicar con la BBDD, analisis no guardado" + "\n" + ex.getMessage()); 
            Alertas.alertaBBDD(ex.getMessage());            
        }    
        // Podemos cerrar la conexión del puerto, hemos terminado el trabajo
        puerto.cierraConexion();         
    }    
    
    // Establece los valores de los componentes en pantalla cuando se inicia el Analisis con el botón Analizar
    private void analizar() {
        tfResultadoMuestra.setStyle("-fx-text-fill: white;");
        btAnalizarMuestra.setText("Abortar"); // cambiamos texto del botón para poder "Abortar"
        tfResultadoMuestra.setText(""); // borramos cualquier texto previo del textfield del resultado
        tfCuentasMuestra.setText(""); // y del de las cuentas que se reciben desde el equipo OXFORD
        cbMetodo.setDisable(true); // desactivamos combos y tf para que usario no cambie la seleccion
        tfMuestra.setDisable(true); // durante el ensayo
        tfIdentificacion.setDisable(true);
        LOGGER.info("INICIADO el análisis de la muestra " + tfMuestra.getText() + " - " + tfIdentificacion.getText() + " - " + cbMetodo.getValue()); 
    }
    
    // Establece los valores de los componentes  en pantalla cuando se aborta el analisis con el botón Abortar
    private void abortar() {
        // Enviamos por el puerto COM la orden de abortar
        envioDatos = new HiloEnvioDatos("abortar", LOGGER); 
        envioDatos.start();        
        try {
            // Esperamos que finalice el hilo que manda datos al equipo            
            envioDatos.join(); 
            cbMetodo.setDisable(false); // Activamos el combo
            tfMuestra.setDisable(false); // y los textfield para que el usuario pueda operar en ellos
            tfIdentificacion.setDisable(false);
            btAnalizarMuestra.setText("Analizar"); // Volvemos a cambiar el botón al modo "Analizar"
            LOGGER.warn("ABORTADO el análisis de la muestra " + tfMuestra.getText() + " - " + tfIdentificacion.getText() + " - " + cbMetodo.getValue()); 
        } catch (InterruptedException ex) {
            LOGGER.fatal("Error al esperar la respuesta del equipo al intentar abortar." + "\n" + ex.getMessage()); 
        }
    }
}
