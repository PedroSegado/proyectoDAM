package com.pasegados.labo;

import java.sql.SQLException;
import com.pasegados.labo.conexionesbbdd.ConexionesCalibrado;
import com.pasegados.labo.conexionesbbdd.ConexionesConfiguracion;
import com.pasegados.labo.conexionesbbdd.ConexionesPatron;
import com.pasegados.labo.conexionesbbdd.ConexionesResultados;
import com.pasegados.labo.modelos.Analisis;
import com.pasegados.labo.modelos.Patron;
import com.pasegados.labo.modelos.Calibrado;
import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Alertas;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Esta clase representa al controlador principal del programa, que a su vez incluye las pestañas, controlada cada una
 * de ellas por su propio controlador
 *
 * @author Pedro Antonio Segado Solano
 */
public class TabViewControlador {

    @FXML
    private Tab tabConfiguracion;
    @FXML
    private Tab tabCalibraciones;
    @FXML
    private Tab tabResultados;
    @FXML
    private Tab tabAnalisis;
    @FXML
    private Tab tabUtilidades;

    // Listas para uso en el programa y conexiones Singleton a la BBDD para llenar estas listas
    private final ObservableList<Ajuste> LISTA_AJUSTES = FXCollections.observableArrayList();
    private final ConexionesConfiguracion CNCF = ConexionesConfiguracion.getINSTANCIA_CONFIGURACION();
    private final ObservableList<Analisis> LISTA_RESULTADOS = FXCollections.observableArrayList();
    private final ConexionesResultados CNR = ConexionesResultados.getINSTANCIA_RESULTADOS();
    private final ObservableList<Patron> LISTA_PATRONES = FXCollections.observableArrayList();
    private final ConexionesPatron CNP = ConexionesPatron.getINSTANCIA_PATRON();
    private final ObservableList<Calibrado> LISTA_CALIBRADOS = FXCollections.observableArrayList();
    private final ConexionesCalibrado CNC = ConexionesCalibrado.getINSTANCIA_CALIBRADO();
    private final ObservableList<String> LISTA_METODOS_ACTIVOS = FXCollections.observableArrayList();
    private final ObservableList<String> LISTA_TODOS_METODOS = FXCollections.observableArrayList();

    // Otros
    private static final Logger logger = LogManager.getLogger(TabViewControlador.class);
    
    /**
     * Inicializa automaticamente el controlador al crear el objeto, ejecutándose su contenido
     */
    public void initialize(){
        App.setControladorPrincipal(this); // Establecemos en la clase App que este es el controlador FXML Principal
        iniciaListener(); // Prepara listener sobre listas que lo necesitan
        cargaListas(); // Generamos las listas necesarias para las tableviews y listviews de las pestañas, asignándolas                       
    }                  // a los controladores de las pestañas que las necesiten
    
    // Inicia el Listener que mantiene actualizados los combos solo con los calibrados activos con los que se puede
    // trabajar y también mantiene el nombre del calibrado actualizado si se cambia editándolo en su formulario
    private void iniciaListener() {       
        LISTA_CALIBRADOS.addListener((ListChangeListener<Calibrado>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {  // Si se añade un calibrado a la lista de calibrados
                    for (Calibrado c : change.getAddedSubList()) { 
                            LISTA_TODOS_METODOS.add(c.getNombre()); // metodo a la lista de String para combos que lo necesiten
                        if (c.isActivo()) { // Si el añadido esta marcado como "activo"
                            LISTA_METODOS_ACTIVOS.add(c.getNombre()); // entonces lo añadimos a la lista que se asigna a los combos
                            logger.info("Calibrado " + c.getNombre() + " añadido a la lista de Calibrados ACTIVOS para el análisis" );                        
                        }
                        c.activoProperty().addListener((observable, oldEstado, newEstado) -> { // listener para posibles cambios de la propiedad "activo"
                            if (newEstado.equals(true)) { // Si es editado como "activo"
                                LISTA_METODOS_ACTIVOS.add(c.getNombre()); // lo añade a la lista
                                logger.info("Calibrado " + c.getNombre() + " añadido a la lista de Calibrados ACTIVOS para el analisis" );                        
                            } else { // si es editado a "inactivo"
                                LISTA_METODOS_ACTIVOS.remove(c.getNombre()); // lo elimina de a lista
                                logger.info("Calibrado " + c.getNombre() + " eliminado de la lista de Calibrados activos para el análisis (INACTIVADO)" );                        
                            }
                        });
                        c.nombreProperty().addListener((observable, oldNombre, newNombre) -> { // listener por si renombramos el Calibrado
                            if (c.isActivo()) { // si el calibrado renombrado está marcado como "activo)
                                LISTA_METODOS_ACTIVOS.remove(oldNombre); // quitamos el viejo nombre de la lista
                                LISTA_METODOS_ACTIVOS.add(newNombre); // y añadimos el nuevo nombre
                                logger.info("El calibrado " + oldNombre.toString() + " ha cambiado de nombre a " + newNombre.toString() + " en la lista de calibrados ACTIVOS para el analisis" );                        
                            }
                        });
                    }
                } else if (change.wasRemoved()) { // Si se borra el calibrado de la lista calibrados                      
                    for (Calibrado c : change.getRemoved()) {
                        LISTA_METODOS_ACTIVOS.remove(c.getNombre()); // eliminanos el nombre de la lista que se asigna a los combos
                        LISTA_TODOS_METODOS.remove(c.getNombre());
                        logger.info("Calibrado " + c.getNombre() + " eliminado de la lista de Calibrados activos para el análisis (ELIMINADO)" );                     
                    }
                }
            }
        });        
    }
        
    // Prepara las listas necesarias para el funcionamiento del programa, rescatando la información de la BBDD
    private void cargaListas() {
        try {
            //PESTAÑA AJUSTES
            LISTA_AJUSTES.addAll(CNCF.cargarAjustes());
            App.getControladorConfiguracion().setListaAjustes(LISTA_AJUSTES);
            //PESTAÑA RESULTADOS             
            LISTA_RESULTADOS.addAll(CNR.cargarResultados());
            App.getControladorResultados().setListaResultados(LISTA_RESULTADOS);
            //PESTAÑA CALIBRADOS : PATRONES             
            LISTA_PATRONES.addAll(CNP.cargarPatrones());
            App.getControladorCalibrados().setListaPatrones(LISTA_PATRONES);
            //PESTAÑA CALIBRADOS : CALIBRADOS
            LISTA_CALIBRADOS.addAll(CNC.cargarCalibrados());
            App.getControladorCalibrados().setListaCalibrados(LISTA_CALIBRADOS);
        } catch (SQLException e) {            
            logger.fatal("Error al cargar información de la BBDD:" + "\n" + e.getMessage() );
            Alertas.alertaBBDD(e.getMessage());
        }
        // Asignamos la lista de calibrados activos a los dos combos que la necesitan
        App.getControladorAnalisis().setComboMetodos(LISTA_METODOS_ACTIVOS);        
        App.getControladorResultados().setComboMetodos(LISTA_TODOS_METODOS); 
    }
       
    // GETTERS LISTAS (SETTERS SON INNECESARIOS PUES SE GENERAN DESDE LA BBDD)
    
    /**
     * Devuelve la lista de objetos Ajuste
     * @return ObservableList con todos los Ajustes
     */
    public ObservableList<Ajuste> getListaAjustes() {
        return LISTA_AJUSTES;
    }

    /**
     * Devuelve la lista de objetos Analisis
     * @return ObservableList con todos los Analisis
     */
    public ObservableList<Analisis> getListaResultados() {
        return LISTA_RESULTADOS;
    }

    /**
     * Devuelve la lista de objetos Patron
     * @return ObservableList con todos los Analisis
     */
    public ObservableList<Patron> getListaPatrones() {
        return LISTA_PATRONES;
    }

    /**
     * Devuelve la lista de objetos Calibrado
     * @return ObservableList con todos los Analisis
     */
    public ObservableList<Calibrado> getListaCalibrados() {
        return LISTA_CALIBRADOS;
    }

    //OTROS METODOS    
    
    /**
     * Este método devuelve el objeto Calibrado cuyo atributo nombre coincide con el indicado en el parámetro
     * @param nombre String con el nombre del Calibrado, que es su PK en la BBDD
     * @return Calibrado cuyo nombre coincide con el pasado por parámetro
     */
    public Calibrado getCalibrado(String nombre) {
        for (Calibrado c : LISTA_CALIBRADOS) {
            if (c.getNombre().equals(nombre)) {
                return c;
            }
        }
        return null; // No existe Calibrado con el nombre indicado, devolvemos null
    }

    /**
     * Este método devuelve el objeto Ajuste cuyo atributo nombre coincide con el indicado en el parámetro
     * @param nombre String con el nombre del Ajuste, que es su PK en la BBDD
     * @return Ajuste cuyo nombre coincide con el pasado por parámetro
     */
    public Ajuste getAjuste(String nombre) {
        for (Ajuste a : LISTA_AJUSTES) {
            if (a.getNombre().equals(nombre)) {
                return a;
            }
        }
        return null; // No existe Ajuste con el nombre indicado, devolvemos null
    }    
}
