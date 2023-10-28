package com.pasegados.labo.resultados;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.util.HashMap;
import java.util.Locale;
import com.pasegados.labo.App;
import com.pasegados.labo.conexionesbbdd.ConexionesResultados;
import com.pasegados.labo.modelos.Alertas;
import com.pasegados.labo.modelos.Analisis;
import com.pasegados.labo.modelos.ColeccionResultados;
import com.pasegados.labo.modelos.Filtros;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
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

public class TabResultadosControlador {

    // Label
    @FXML
    private Label lbBusqueda;

    // DatePicker
    @FXML
    private DatePicker dpBuscaFecha;
    @FXML
    private DatePicker dpBuscaFechaHasta;

    // Tabla
    @FXML
    private TableView<Analisis> tvResultados;
    private ObservableList<Analisis> listaResultados;
    private final ObservableList<Analisis> LISTA_BUSQUEDA = FXCollections.observableArrayList();
    @FXML
    private TableColumn<Analisis, Number> colResultadosNumMuestra;
    @FXML
    private TableColumn<Analisis, String> colResultadosIdentificacion;
    @FXML
    private TableColumn<Analisis, Date> colResultadosFecha;
    @FXML
    private TableColumn<Analisis, String> colResultadosResultado; //Como String en vez de Number para formatear a 4 decimales
    @FXML
    private TableColumn<Analisis, String> colResultadosCalibrado;
    @FXML
    private TableColumn<Analisis, Number> colResultadosCuentas;

    // TextField
    @FXML
    private TextField tfBuscNumMuestra;
    @FXML
    private TextField tfBuscaIdentificacion;

    // CheckBox
    @FXML
    private CheckBox ckBuscNumMuestra;
    @FXML
    private CheckBox ckBuscaIdentificacion;
    @FXML
    private CheckBox ckBuscaFecha;
    @FXML
    private CheckBox ckBuscaRango;
    @FXML
    private CheckBox ckBuscaCalibrado;

    // Botones
    @FXML
    private Button btBuscar;
    @FXML
    private Button btReiniciarBusqueda;

    //ComboBox
    @FXML
    private ComboBox<String> cbBuscaCalibrado;

    //Otras variables
    private final ConexionesResultados CNR = ConexionesResultados.getINSTANCIA_RESULTADOS();
    //private final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("MM/dd/yyyy"); //Da formato al pasar LocalDate a String
    private static final Logger LOGGER = LogManager.getLogger(TabResultadosControlador.class);
    
    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido.
     */
    public void initialize() {
        App.setControladorResultados(this); // Establecemos en la clase App que este es el controlador FXML del la Tab Resultados
        tfBuscNumMuestra.setTextFormatter(new TextFormatter<>(Filtros.getNumeroEspecialFilter(9, "%_"))); // Para filtrar que solo se introduzcan un máximo x caracteres, (numeros + caracteres especiales indicados)
        tfBuscaIdentificacion.setTextFormatter(new TextFormatter<>(Filtros.getMaxTamanioFilter(20))); // Para filtrar que solo admita max 20 caracteres

        // COLUMNAS TABLA RESULTADOS
        colResultadosNumMuestra.setCellValueFactory(cellData -> cellData.getValue().muestraProperty());
        colResultadosIdentificacion.setCellValueFactory(cellData -> cellData.getValue().identificacionProperty());
        colResultadosFecha.setCellValueFactory(cellData -> cellData.getValue().fechaProperty());
        colResultadosResultado.setCellValueFactory(cellData -> cellData.getValue().resultadoProperty().asString(Locale.US, "%.4f"));
        colResultadosCalibrado.setCellValueFactory(cellData -> cellData.getValue().calibradoProperty());
        colResultadosCuentas.setCellValueFactory(cellData -> cellData.getValue().cuentasProperty());
    }

    // Ajuste la interfaz para buscar una fecha correspondiente a un solo día
    @FXML
    private void usarFechaBusqueda(ActionEvent event) {
        if (ckBuscaFecha.isSelected()) {
            ckBuscaRango.setDisable(false);
        } else {
            ckBuscaRango.setDisable(true);
            dpBuscaFechaHasta.setDisable(true);
            ckBuscaRango.setSelected(false);
            dpBuscaFechaHasta.setValue(null);
            ckBuscaFecha.setText("Fecha");
        }
    }

    // Ajusta la interfaz para buscar "desde" una fecha "hasta" otra fecha posterior
    @FXML
    private void usarRangoBusqueda(ActionEvent event) {
        if (ckBuscaRango.isSelected()) {
            ckBuscaFecha.setText("Desde");
            dpBuscaFechaHasta.setDisable(false);
        } else {
            ckBuscaFecha.setText("Fecha");
            dpBuscaFechaHasta.setDisable(true);
        }
    }

    // Ajusta la interfaz cuando el usuario pulsa reiniciar, mostrando los últimos 25 resultados y cambiando el texto del lbBusqueda
    @FXML
    private void reiniciaBusqueda(ActionEvent event) {
        lbBusqueda.setText("Últimos 25 análisis:");
        ckBuscNumMuestra.setSelected(false);
        tfBuscNumMuestra.setText("");
        ckBuscaIdentificacion.setSelected(false);
        tfBuscaIdentificacion.setText("");
        ckBuscaFecha.setSelected(false);
        dpBuscaFecha.setValue(null);
        ckBuscaRango.setSelected(false);
        dpBuscaFechaHasta.setValue(null);
        ckBuscaCalibrado.setSelected(false);
        cbBuscaCalibrado.setValue("");
        tvResultados.setItems(listaResultados);
    }

    // Muestra el informe con los últimos 25 resutados o con la búsqueda realizada, lo que se tenga en pantalla
    @FXML
    private void verInforme(ActionEvent event) {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(this.getClass().getResourceAsStream("informeResultados.jrxml"));
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("tipoInforme", lbBusqueda.getText()); // Paso como parametro el texto de lbBusqueda para que jasper diferencie el titulo del informe
            JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(ColeccionResultados.getColeccionResultados()));
            JRViewer viewer = new JRViewer(print);
            viewer.setOpaque(true);
            viewer.setVisible(true);
            JasperViewer.viewReport(print, false);
        } catch (JRException e) {            
            LOGGER.fatal("Error al generar el informe de últimos resultados/búsqueda " + "\n" + e.getMessage());
            if (lbBusqueda.getText().startsWith("Últimos")){
                Alertas.alertaInforme("últimos 25 resultados" ,"", e.getMessage());
            }
            else{
                Alertas.alertaInforme("búsqueda de resultados" ,"", e.getMessage());
            }            
        }
    }

    // Realiza una busqueda en la BBDD con las condiciones marcadas por el usuario
    @FXML
    private void buscarMuestra(ActionEvent event) {
        lbBusqueda.setText("Resultado de la búsqueda:");

        try { //Control excepcion nullpointer si se marca la opción de busqueda por fecha y no se selecciona ninguna en calendario
            String busqueda = "";

            if (ckBuscNumMuestra.isSelected() || ckBuscaIdentificacion.isSelected() || ckBuscaFecha.isSelected() || ckBuscaCalibrado.isSelected()) {
                busqueda = "WHERE ";
                // Si hemos marcado buscar por nº de muestra    
                if (ckBuscNumMuestra.isSelected()) {                    
                    busqueda = busqueda + "numMuestra LIKE '" + tfBuscNumMuestra.getText() + "'";
                    // Si tenemos mas parámetros que filtren la busqueda añadimos AND
                    if (ckBuscaIdentificacion.isSelected() || ckBuscaFecha.isSelected() || ckBuscaCalibrado.isSelected()) busqueda = busqueda + " AND ";                    
                }
                // Si hemos marcado buscar por identificación de muestra
                if (ckBuscaIdentificacion.isSelected()) {                    
                    busqueda = busqueda + "identificacion LIKE '" + tfBuscaIdentificacion.getText() + "'";
                    // Si tenemos mas parámetros que filtren la busqueda añadimos AND
                    if (ckBuscaFecha.isSelected() || ckBuscaCalibrado.isSelected()) busqueda = busqueda + " AND ";                    
                }
                // Si hemos marcado buscar por fecha
                if (ckBuscaFecha.isSelected()) {
                    if (!ckBuscaRango.isSelected()) { // Si vamos a buscar un solo día
                        //String fecha = dpBuscaFecha.getValue().format(FORMATO_FECHA);
                        busqueda = busqueda + "fecha = '" + dpBuscaFecha.getValue() + "'";
                    } else { // si vamos a buscar en un rango
                        if (dpBuscaFecha.getValue().isAfter(dpBuscaFechaHasta.getValue())) { // Comprobamos que la fecha inicial sea anterior a la final
                            IllegalArgumentException miExcepcion = new IllegalArgumentException("Error al ajustar indicar en inicio una fecha posterior a la de fin");
                            throw miExcepcion; // lanzo excepción para capturarla e informar, no realizo la búsqueda
                        }
                        //String fecha = dpBuscaFecha.getValue().format(FORMATO_FECHA);
                        //String fechaFin = dpBuscaFechaHasta.getValue().format(FORMATO_FECHA);
                        busqueda = busqueda + "fecha BETWEEN '" + dpBuscaFecha.getValue() + "' AND '" + dpBuscaFechaHasta.getValue() + "'";
                    }
                    // // Si tenemos mas parámetros que filtren la busqueda añadimos AND. Solo queda calibración.
                    if (ckBuscaCalibrado.isSelected()) busqueda = busqueda + " AND ";                    
                }
                // Si hemos marcado buscar por calibrado
                if (ckBuscaCalibrado.isSelected()) {                    
                    busqueda = busqueda + "calibrado = '" + cbBuscaCalibrado.getValue() + "'";
                }
            } // Cierre del if con nuestro WHERE ya creado para la busqueda SQL

            String log = busqueda.isBlank() ? "Todos los análisis" : busqueda;
            LOGGER.info("Busqueda: " + "Busqueda filtrada como: " + log);

            // BBDD - Cargar los Analisis de la busqueda  
            LISTA_BUSQUEDA.clear(); // Borramos cualquier objeto de una busqueda anterior
            LISTA_BUSQUEDA.addAll(CNR.buscarResultados(busqueda));
            tvResultados.setItems(LISTA_BUSQUEDA); // Asignamos a la tabla la lista con los objetos de la busqueda

        } catch (NullPointerException ex) { //Salta si se marca buscar fecha pero no se selecciona ninguna
            LOGGER.info("Error al buscar por fecha y no marcar ninguna en el calendario: " + "\n" + ex.getMessage());
            Alertas.alarmaFechaBusqueda();
        } catch (SQLException ex) {
            LOGGER.fatal("Error acceso BBDD: " + ex.getMessage());
            Alertas.alertaBBDD(ex.getMessage());            
        } catch (IllegalArgumentException ex) {
            LOGGER.info("Error al buscar por fechas mal ajustadas: " + "\n" + ex.getMessage());
            Alertas.alarmaFechaRangoBusqueda();
        }
    }

    /**
     * Este método establece la lista de métodos de análisis para la busqueda, siendo estos todos
     * los calibrados activos.     * 
     * @param lista ObservableList de tipo String con el nombre de los calibrados activos
     */
    public void setComboMetodos(ObservableList<String> lista) {
        cbBuscaCalibrado.setItems(lista);
        cbBuscaCalibrado.getItems().add("ELIMINADOS");
    }

    /**
     * Este método establece la lista de resultados que almacenará los resultados de las búsquedas.     * 
     * @param listaResultados ObservableList de tipo Análisis 
     */
    public void setListaResultados(ObservableList<Analisis> listaResultados) {
        this.listaResultados = listaResultados;
        tvResultados.setItems(this.listaResultados);
    }

    /**
     * Este método devuelve la lista de análisis obtenidos en una búsqueda     * 
     * @return ObservableList de tipo Análisis con los que cumplen los criterios de búsqueda
     */
    public ObservableList<Analisis> getListaInforme() {
        if (lbBusqueda.getText().contains("Resultado")) {
            return LISTA_BUSQUEDA; // La lista de la búsqueda realizada
        }
        return listaResultados; // Los 25 últimos       
    }
}
