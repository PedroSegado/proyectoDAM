package com.pasegados.labo.configuracion;

import java.io.IOException;
import java.sql.SQLException;
import com.fazecast.jSerialComm.SerialPort;
import com.pasegados.labo.App;
import com.pasegados.labo.conexionesbbdd.Conexion;
import com.pasegados.labo.conexionesbbdd.ConexionesConfiguracion;
import com.pasegados.labo.conexionesbbdd.ConexionesResultados;
import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.modelos.Calibrado;
import com.pasegados.labo.modelos.Configuracion;
import com.pasegados.labo.modelos.Filtros;
import com.pasegados.labo.modelos.Patron;
import com.pasegados.labo.modelos.Puerto;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Esta clase representa al controlador de la pestaña Configuración
 *
 * @author Pedro Antonio Segado Solano
 */
public class TabConfiguracionControlador {

    // ComboBox
    @FXML
    private ComboBox<Integer> cbBPS;
    ObservableList<Integer> listaBPS = FXCollections.observableArrayList();
    @FXML
    private ComboBox<Integer> cbBDD;
    ObservableList<Integer> listaBDD = FXCollections.observableArrayList();
    @FXML
    private ComboBox<String> cbBPD;
    ObservableList<String> listaBPD = FXCollections.observableArrayList();
    @FXML
    private ComboBox<String> cbPAR;
    ObservableList<String> listaPAR = FXCollections.observableArrayList();
    @FXML
    private ComboBox<String> cbPuerto;
    ObservableList<String> listaPuertos = FXCollections.observableArrayList();

    // TextFields
    @FXML
    private TextField tfPreAcond;
    @FXML
    private TextField tfAcond;
    @FXML
    private TextField tfPreEnerg;
    @FXML
    private TextField tfEner;
    @FXML
    private TextField tfPreMed;
    @FXML
    private TextField tfPulsaciones;

    // Botones         
    @FXML
    private Button btAnadirAjsute;
    @FXML
    private Button btModificarAjsute;
    @FXML
    private Button btEliminarAjsute;
    @FXML
    private Button btReiniciar;
    @FXML
    private Button btGuardar;
    @FXML
    private Button btBorrarBBDD;
    @FXML
    private Button btCopiaSeguridad;
    @FXML
    private Button btRestaurarCopia;

    // Listas
    @FXML
    private ListView<Ajuste> lvAjustes;
    private ObservableList<Ajuste> listaAjustes;

    // Otras variables
    private final ConexionesConfiguracion CNCF = ConexionesConfiguracion.getINSTANCIA_CONFIGURACION();
    private final ConexionesResultados CNR = ConexionesResultados.getINSTANCIA_RESULTADOS();
    private Puerto puerto;
    private final SerialPort[] PORTS = Puerto.getPuertosSistema();
    private Configuracion config; // Objeto Configuración que almacena toda la info
    private EditorAjusteControlador controladorAjuste = new EditorAjusteControlador();
    private static final Logger LOGGER = LogManager.getLogger(TabConfiguracionControlador.class);
    @FXML
    private Button btBorrarResultados;

    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {
        App.setControladorConfiguracion(this); // Establecemos en la clase App que este es el controlador FXML del la Tab Configuracion        
        // Filtros de los textFields
        tfPulsaciones.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(1000))); // tiempo entre pulsaciones maximo permitido de 1000 ms
        tfPreAcond.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(20000))); // tiempo preacondicionamiento maximo 20000 ms
        tfAcond.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(20))); // tiempo acondicionamiento maximo 20 s
        tfPreEnerg.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(10000))); // tiempo preenergia maximo 10000 ms
        tfEner.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(20))); // tiempo energia maximo 20 s
        tfPreMed.setTextFormatter(new TextFormatter<>(Filtros.getNumeroFilter(10000))); // tiempo premedidao maximo 10000 ms
        //Combobox PUERTOS + Busca puertos COM en el equipo
        for (SerialPort port : PORTS) {
            listaPuertos.add(port.getSystemPortName()); //añade los nombres de los puertos a la lista
        }
        cbPuerto.setItems(listaPuertos); // asignamos la lista al combobox de puerto
        // Combobox BPS
        listaBPS.addAll(110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000);
        cbBPS.setItems(listaBPS);
        //Combobox BDD
        listaBDD.addAll(4, 5, 6, 7, 8);
        cbBDD.setItems(listaBDD);
        //Combobox PARIDAD
        listaPAR.addAll("Par", "Impar", "Ninguna", "Marca", "Espacio");
        cbPAR.setItems(listaPAR);
        //Combobox BDP
        listaBPD.addAll("1", "1.5", "2");
        cbBPD.setItems(listaBPD);
        //Cargamos los datos de configuración y del puerto desde BBDD      
        cargaConfiguracion(null);

        //BINDING CONFIGURACION, cualquier cambio en los texfield se traslada al úico objeto de configuración que existe
        tfPulsaciones.textProperty().bindBidirectional(config.pulsacionesProperty(), new NumberStringConverter("0"));
        tfPreAcond.textProperty().bindBidirectional(config.preAcondicionamientoProperty(), new NumberStringConverter("0"));
        tfAcond.textProperty().bindBidirectional(config.acondicionamientoProperty(), new NumberStringConverter("0"));
        tfPreEnerg.textProperty().bindBidirectional(config.preEnergiaProperty(), new NumberStringConverter("0"));
        tfEner.textProperty().bindBidirectional(config.energiaProperty(), new NumberStringConverter("0"));
        tfPreMed.textProperty().bindBidirectional(config.preMedidaProperty(), new NumberStringConverter("0"));
    }

    // Guarda toda la configuración del programa y puerto tal y como se encuentra en pantalla en ese momento
    @FXML
    private void guardaConfig(ActionEvent event) {
        // Preparo el objeto Puerto
        puerto.setNombrePuerto(cbPuerto.getValue());
        puerto.setBps(cbBPS.getValue());
        puerto.setBdd(cbBDD.getValue());
        puerto.setParidad(cbPAR.getValue());
        puerto.setBdp(cbBPD.getValue());
        // Preparo el objeto Configuracion
        config.setPulsaciones(Integer.valueOf(tfPulsaciones.getText()));
        config.setPreAcondicionamiento(Integer.valueOf(tfPreAcond.getText()));
        config.setAcondicionamiento(Integer.valueOf(tfAcond.getText()));
        config.setPreEnergia(Integer.valueOf(tfPreEnerg.getText()));
        config.setEnergia(Integer.valueOf(tfEner.getText()));
        config.setPreMedida(Integer.valueOf(tfPreMed.getText()));

        try { // Guardamos ambos en la BBDD, en la tabla Configuración que contendrá una único registro
            CNCF.actualizarDatosPuerto(puerto);
            CNCF.actualizarDatosConfig(config);
        } catch (SQLException e) { // Error al comunicar con BBDD
            LOGGER.fatal("Error al guardar datos relativos a la configuración" + "\n" + e.getMessage());
            Alertas.alertaBBDD(e.getMessage());
        }
    }

    // Permite cargar la configuración del programa y del puerto tal y como está en la BBDD
    @FXML
    private void cargaConfiguracion(ActionEvent event) {
        try {
            puerto = CNCF.cargarDatosPuerto(); // Puerto desde la BBDD 
            config = CNCF.cargarDatosConfiguracion(); // Configuración desde la BBDD 
            // Ajusto los combos relativos al puerto COM con la información recibiba
            cbPuerto.setValue(puerto.getNombrePuerto());
            cbBPS.setValue(puerto.getBps());
            cbBDD.setValue(puerto.getBdd());
            cbPAR.setValue(puerto.getParidad());
            cbBPD.setValue(puerto.getBdp());
            // Ajusto los textFields relativos a la configuración con  la información recibida
            tfPulsaciones.setText(String.valueOf(config.getPulsaciones()));
            tfPreAcond.setText(String.valueOf(config.getPreAcondicionamiento()));
            tfAcond.setText(String.valueOf(config.getAcondicionamiento()));
            tfPreEnerg.setText(String.valueOf(config.getPreEnergia()));
            tfEner.setText(String.valueOf(config.getEnergia()));
            tfPreMed.setText(String.valueOf(config.getPreMedida()));
        } catch (SQLException e) { // Error de comunicación con BBDD
            LOGGER.fatal("Error al cargar datos relativos a la configuración" + "\n" + e.getMessage());
            Alertas.alertaBBDD(e.getMessage());
        }
    }

    // Abre el editor de ajustes para crear un nuevo Ajuste
    @FXML
    private void nuevoAjuste(ActionEvent event) {
        Ajuste nuevoAjuste = new Ajuste(); // preparamos nuevo objeto

        if (accedeFormularioAjuste(nuevoAjuste, "nuevo")) { // Si se pulsa aceptar en el formulario de Ajustes
            try { // Añadimos el nuevo Ajuste a la BBDD y a la lista de Ajustes
                CNCF.insertarAjuste(nuevoAjuste);
                listaAjustes.add(nuevoAjuste);
                LOGGER.info("Nuevo ajuste +" + nuevoAjuste.getNombre() + " guardado  en la tabla 'Ajustes'");
            } catch (SQLException e) {
                LOGGER.fatal("Error al guardar el nuevo ajuste " + nuevoAjuste.getNombre() + " en la tabla 'Ajustes'" + "\n" + e.getMessage());
                Alertas.alertaBBDD(e.getMessage());
            }
        }
    }

    // Modifica un Ajuste existente abriendo el editor de ajustes y mostrando en él sus datos
    @FXML
    private void modificarAjuste(ActionEvent event) {
        // Seleccionamos el objeto marcado en la lista de Ajustes
        Ajuste seleccionado = lvAjustes.getSelectionModel().getSelectedItem();

        if (seleccionado != null) { // Si hay algo seleccionado en la lista
            String valorViejo = seleccionado.toStringCompleto(); // Para informar en el log
            String nombreOriginal = seleccionado.getNombre(); // Guardamos el nombre original por si lo moficiamos poder hacer el UPDATE

            if (accedeFormularioAjuste(seleccionado, "modificar")) { // Si se pulsa aceptar en el formulario/editor de Ajustes                
                try { // Actualizamos valores en BBDD
                    CNCF.actualizarAjuste(seleccionado, nombreOriginal);
                    LOGGER.info("Modificado el ajuste con datos " + valorViejo + " a los nuevos valores " + seleccionado.toStringCompleto());
                } catch (SQLException e) {
                    LOGGER.fatal("Error al modificar en BBDD el ajuste con datos " + valorViejo + "\n" + e.getMessage());
                    Alertas.alertaBBDD(e.getMessage());
                }                
            }
        } else { // No se ha seleccionado ningún Ajuste en la lista
            Alertas.alertaErrorModificar("ajuste");
        }
    }

    // Elimina un Ajuste existente de la BBDD y lista
    @FXML
    private void eliminarAjuste(ActionEvent event) {
        // Seleccionamos el objeto marcado en la lista de Ajustes
        Ajuste seleccionado = lvAjustes.getSelectionModel().getSelectedItem();
        String usanAjuste = "";

        if (seleccionado != null) { // Si ha algo seleccionado      
            // Verifico si se usa en algún Calibrado, para avisar al usuario
            int tamanyo = App.getControladorPrincipal().getListaCalibrados().size();
            for (int i = 0; i < tamanyo; i++) {
                if (App.getControladorPrincipal().getListaCalibrados().get(i).getAjuste().getNombre().equals(seleccionado.getNombre())) {
                    if (usanAjuste.isBlank()) { // primero en escribirse
                        usanAjuste += "'" + App.getControladorPrincipal().getListaCalibrados().get(i).getNombre() + "'";
                    } else if (i < (tamanyo - 1)) { // ultimo en escribirse, pero y ya hay otro/s antes
                        usanAjuste = usanAjuste.replaceAll(" y ", ", ") + " y '" + App.getControladorPrincipal().getListaCalibrados().get(i).getNombre() + "'";
                    } else { // lo demas
                        usanAjuste += " y '" + App.getControladorPrincipal().getListaCalibrados().get(i).getNombre() + "'";
                    }
                }
            }

            // Alerta confirmacion por parte del usuario
            boolean confirmacion;
            if (usanAjuste.isBlank()) { // no se usa el ajuste en ningun calibrado {
                confirmacion = Alertas.alertaEliminaObjeto("ajuste", seleccionado.getNombre());
            } else {
                confirmacion = Alertas.alertaEliminaObjeto("ajuste", seleccionado.getNombre(), usanAjuste);
            }

            if (confirmacion) { // Si acepta, eliminar registro de BBDD
                try { // Realizamos DELETE en BBDD
                    CNCF.eliminarAjuste(seleccionado.getNombre());
                    LOGGER.warn("Eliminado el ajuste con datos " + seleccionado.toStringCompleto());
                    listaAjustes.remove(seleccionado);

                    //Verifico los objetos Calibrado, y si alguno usaba este ajuste, pasa al valor NULL
                    //En BBDD se realiza automaticamente con el ON DELETE SET NULL                    
                    for (Calibrado c : App.getControladorPrincipal().getListaCalibrados()) {
                        if (c.getAjuste().equals(seleccionado)) {
                            c.setAjuste(null);
                            c.setActivo(false);// y ya no se puede usar para analizar hasta que no se le asigne un ajuste
                            c.setTipoRegresion("Seleccionar...");// y el tipo de regresion a seleccionar por el ususario
                            c.ajustaCoeficientes("cero");
                            for (Patron p : c.getListaPatrones()) {
                                p.setCuentas(0); // y las cuentas de los patrones a 0 hasta que se elija otro ajuste y se actualicen
                            }
                        }
                    }

                } catch (SQLException e) {
                    if (e.getCause().toString().contains("SQLIntegrityConstraintViolationException")) {
                        LOGGER.warn("Error al eliminar en BBDD el ajuste con datos " + seleccionado.toStringCompleto()
                                + " debido a que está asignado a algún calibrado" + "\n" + e.getMessage());
                        Alertas.alertaAjusteUsadoCalibrado(seleccionado.getNombre());
                    } else {
                        LOGGER.fatal("Error al eliminar en BBDD el ajuste con datos " + seleccionado.toStringCompleto() + "\n" + e.getMessage());
                        Alertas.alertaBBDD(e.getMessage());
                    }
                }
                lvAjustes.getSelectionModel().clearSelection(); // desmarco objeto de la lista para evitar borrados accidentales
            } //Si llegamos aqui hemos cancelado el borrado del patron en la alerta, no pasa nada
        } else { // No hay nada seleccionado para borrar
            Alertas.alertaErrorBorrar("ajuste");
        }
    }

// Llama a la ventana del formulario, pasandole a esta los datos del Ajuste o creando uno nuevo, según el parámetro
// titulo sea "nuevo" o "modificar".
    private boolean accedeFormularioAjuste(Ajuste ajuste, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/pasegados/labo/configuracion/EditorAjuste.fxml"));
            Parent root = loader.load();

            controladorAjuste = (com.pasegados.labo.configuracion.EditorAjusteControlador) loader.getController();
            controladorAjuste.setAjuste(ajuste); // Pasamos el objeto ajuste recibido para trabajar con él

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Gestión de Ajustes");

            if (titulo.equals("modificar")) { // El titulo en función de si es "nuevo" o "modificar" existente
                controladorAjuste.setTitulo("Modificar Ajuste:");
            } else {
                controladorAjuste.setTitulo("Nuevo Ajuste:");
            }

            controladorAjuste.setStage(stage);
            stage.setScene(scene);
            stage.setMinHeight(486d);
            stage.setMinWidth(374d);
            stage.setMaxHeight(536d);
            stage.setMaxWidth(424d);

            stage.showAndWait(); // Esperamos respuesta del usuario en el editor

            if (controladorAjuste.sePulsaAceptar()) { // El usuario acepta los cambios en el editor
                lvAjustes.refresh();
                return true;
            }
        } catch (IOException ex) { // Error al localizar y abrir el editor
            LOGGER.fatal("Error al abrir el formulario de 'Gestión de Ajustes'" + "\n" + ex.getMessage());
            Alertas.alertaAbrirEditor("ajustes");
        }

        return false; // Si llegamos aqui el usuario a cancelado o cerrado la ventana sin aceptar
    }

    // OTROS METODOS NO FXML
    /**
     * Este método devuelve el Puerto creado por este controlador de la pestaña "Configuración".
     *
     *
     * @return Puerto configurado tal y como vemos en la pestaña
     */
    public Puerto getPuerto() {
        return puerto;
    }

    /**
     * Este método devuelve la Configuracion relativa al funcionamiento del equipo, tal y como aparece en la pestaña
     * "Configuracion".
     *
     *
     * @return Configuración con los atributos tal y como vemos en la pestaña
     */
    public Configuracion getConfiguracion() {
        return config;
    }

    /**
     * Este método recibe la lista de objetos Ajuste para trabajar en su gestión desde la pestaña "Configuración
     *
     *
     * @param listaAjustes ObservableList de objetos Ajuste rescatada desde la BBDD al inicio del programa
     */
    public void setListaAjustes(ObservableList<Ajuste> listaAjustes) {
        this.listaAjustes = listaAjustes;
        lvAjustes.setItems(this.listaAjustes);
    }

    
    /**
     * Este método permite borrar la BBDD en uso, eliminando los archivos del equipo.
     */
    @FXML
    private void borrarBBDD(ActionEvent event) {
        // Antes de borrar indicaremos que es conveniente hacer copia de seguridad
        boolean copia = Alertas.alertaPreviaCrearCopia();
        boolean exitoCopiaSeg = true;
        if (copia) {
            exitoCopiaSeg = copiaSeguridadBBDD(copia); // Si falla el proceso de creado de copia, exito pasa a false
        }
        // Alerta de borrado indicando al usuario si se ha creado copia o no, o si la copia ha fallado
        boolean borrado = Alertas.alertaBorradoBBDD(copia, exitoCopiaSeg);
        if (borrado) {
            try { // Detenemos por completo la base de datos, para que se puedan borrar todos los archivos
                Conexion.getINSTANCIA().iniciarConexion();
                Conexion.getINSTANCIA().cerrarBase(); // Cierra
                Conexion.getINSTANCIA().detenerConexion();

                File directorioBaseDeDatos = new File("./bbdd");
                File[] archivos = directorioBaseDeDatos.listFiles();

                if (archivos != null) {
                    for (File archivo : archivos) {
                        archivo.delete();
                    }
                }
                // Borra el directorio
                directorioBaseDeDatos.delete();
                
                //Informamos de que se ha borrado con exito y que se va a reinicar la App
                Alertas.alertaBBDDBorradoExito();
                App.getApp().reiniciar(); // Reiniciamos la app para que cargue los datos nuevos
            } catch (SQLException ex) {
                Alertas.alertaBBDDBorradoFracaso(ex.getMessage());
            }
        }
    }

    @FXML
    private void realizarCopia(ActionEvent event) {
        copiaSeguridadBBDD(false);
    }

    private boolean copiaSeguridadBBDD(boolean borradoPrevio) {
        // Solicitaremos confirmación al usuario, salvo que la haya dado anteriormente porque iba a borrar la bbdd
        boolean copia;
        if (borradoPrevio) { // Si seleccionó hacer copia previa al borrado de la BBDD
            copia = true;
        } else {
            copia = Alertas.alertaCrearCopia(); // Solicitamos confirmación al usuario
        }
        if (copia) { // Tenemos confirmacion            
            String directorioFecha = (LocalDate.now().toString()).replaceAll("-", "") + "/";

            File directorioCopias = new File("./copiaSeguridad/" + directorioFecha);
            if (!directorioCopias.exists()) {
                directorioCopias.mkdirs();
            }

            File directorioBBDD = new File("./bbdd");
            File[] archivos = directorioBBDD.listFiles();
            // Recorro los archivos de la BBDD y los copia al directorio de copia de seguridad
            for (File f : archivos) {
                try {
                    Files.copy(f.toPath(), new File(directorioCopias, f.getName()).toPath(),StandardCopyOption.REPLACE_EXISTING);                    
                } catch (IOException ex) {
                    // Informamos de problema al crear la copia
                    LOGGER.fatal("Error al crear copia de seguridad: " + ex.getMessage());
                    Alertas.alertaCopiaCreadaFracaso(directorioFecha);
                    return false;
                }
            }
            // Informamos de que se ha creado con éxito
            LOGGER.info("Copia " + directorioCopias.getAbsolutePath() + " creada con éxito");
            Alertas.alertaCopiaCreadaExito(directorioCopias.getAbsolutePath());
            return true; 
        }
        return false;
    }

    @FXML
    private void restaurarCopia(ActionEvent event) {
        // Si el directorio de copias no existe, lo creamos para usarlo en el futuro
        File directorioParaCopias = new File("./copiaSeguridad/");
            if (!directorioParaCopias.exists()) {
                directorioParaCopias.mkdirs();
            }

        //Alerta pidiendo confirmacion al usuario para restaurar, donde se selecciona el directorio y lo devuelve
        File directorioCopia = Alertas.alertaRestaurarCopia();

        if (directorioCopia != null) {
            try { // Detenemos por completo la base de datos, para que se puedan sobrescribir los archivos
                Conexion.getINSTANCIA().iniciarConexion();
                Conexion.getINSTANCIA().cerrarBase(); // Cierra
                Conexion.getINSTANCIA().detenerConexion();
                
                File[] archivos = directorioCopia.listFiles();
                File directorioBBDD = new File("./bbdd");
                // Recorro los archivos de la copia los copio al directorio de la base de datos, sobreescribiendo los existentes
                for (File f : archivos) {
                    try {
                        Files.copy(f.toPath(), new File(directorioBBDD, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        //                 
                    }
                }
                
                //Alerta exito indicando que se va a reiniciar
                App.getApp().reiniciar(); // Reiniciamos la app para que cargue los datos nuevos
            } catch (SQLException ex) {
                LOGGER.fatal("Error al restaurar BBDD " + directorioCopia.getAbsolutePath() + ": " + ex.getMessage());
                //Alerta fallo
            }           
        }
    }

    @FXML
    private void eliminarResultadosBBDD(ActionEvent event) {
        try {
            CNR.eliminaAnalisis();            
            //Alerta exito indicando que se va a reiniciar
                App.getApp().reiniciar(); // Reiniciamos la app para que cargue los datos nuevos
        } catch (SQLException ex) {
           // java.util.logging.Logger.getLogger(TabConfiguracionControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
