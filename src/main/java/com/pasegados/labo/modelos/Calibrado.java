package com.pasegados.labo.modelos;

import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Esta clase representa un objeto de tipo Calibrado
 *
 * @author Pedro Antonio Segado Solano
 */
public class Calibrado implements Cloneable {

    private StringProperty nombre;
    private ObjectProperty<LocalDate> fecha;
    private ObjectProperty<Ajuste>  ajuste;
    private BooleanProperty activo;
    private ObjectProperty<ObservableList<Patron>> listaPatrones;
    private FloatProperty minimo;
    private FloatProperty maximo;
    private StringProperty tipoRegresion;   
    private ObjectProperty<Ecuacion> ecuacion;
    private StringProperty patronesString;
    private StringProperty rangoString;

    // CONTRUCTORES
    
    /**
     * Este objeto representa un Calibrado para trabajar en el equipo OXFORD
     * LAB-X 3500 bajo unas determinadas condiciones y con una curva de
     * calibración determinada, que servirá para calcular el resultado de los
     * Analisis realizados.
     *
     * @param nombre String con el nombre de la calibración
     * @param fecha LocalDate con la fecha de creación o actualización del
     * calibrado
     * @param ajuste String con el nombre del objeto Ajuste que interviene en
     * este calibrado.
     * @param activo boolean que indica si el calibrado esta disponible al
     * usuario o no
     * @param listaPatrones ObservableList de objetos Patron, con todos los
     * patrones que incluye el calibrado
     * @param tipoRegresion String indicando si la regresión es "lineal" o
     * "cuadratica"
     */
    public Calibrado(String nombre, LocalDate fecha, Ajuste ajuste, boolean activo, ObservableList<Patron> listaPatrones, String tipoRegresion) {
        this.nombre = new SimpleStringProperty(nombre);
        this.fecha = new SimpleObjectProperty<>(fecha);
        this.ajuste = new SimpleObjectProperty<>(ajuste);
        this.activo = new SimpleBooleanProperty(activo);
        this.listaPatrones = new SimpleObjectProperty<>(listaPatrones);
        this.tipoRegresion = new SimpleStringProperty(tipoRegresion);
        //Calcular el resto de variables del objeto          
        this.ecuacion = new SimpleObjectProperty<>(new Ecuacion());
        this.minimo = new SimpleFloatProperty(0);
        this.maximo = new SimpleFloatProperty(0);       
        this.patronesString = new SimpleStringProperty(null);
        actualizaPatronesString();
        this.rangoString = new SimpleStringProperty(null);
        actualizaRangoString();        
    }

    public Calibrado() {
        this("", LocalDate.now(), new Ajuste("Seleccionar...", 0,0,0,0,0), true, FXCollections.observableArrayList(), "Seleccionar...");
    }

    // CLONADOR DE OBJETOS CALIBRADO
    
    @Override
    public Object clone() {
        Calibrado obj = new Calibrado();

        obj.setNombre(this.nombre.getValue());
        obj.setFecha(this.fecha.getValue());
        obj.setAjuste(this.ajuste.getValue());
        obj.setActivo(this.activo.getValue());
        obj.setListaPatrones(this.listaPatrones.getValue());
        obj.setMinimo(this.minimo.getValue());
        obj.setMaximo(this.maximo.getValue());
        obj.setTipoRegresion(this.tipoRegresion.getValue());    
        obj.setEcuacion(this.ecuacion.getValue());   

        return obj;
    }

    //GETTERS Y SETTERS

    /**
     * Devuelve el nombre del Calibrado
     * 
     * @return String con el nombre que recibe el Calibrado
     */
    
    public String getNombre() {
        return nombre.get();
    }

    /**
     * Establece el nombre del Calibrado
     * 
     * @param nombre String con el nombre para este Calibrado
     */
    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el textField del Editor y el label de la pestaña de Calibrados
     * 
     * @return StringProperty con el nombre del Calibrado
     */
    public StringProperty nombreProperty() {
        return nombre;
    }

    /**
     * Devuelve la fecha en la que se creo o modificó el Calibrado
     * 
     * @return LocalDate con la fecha de creación/modificación del Calibrado
     */
    public LocalDate getFecha() {
        return fecha.get();
    }

    /**
     * Establece la fecha en la que se ha creado o modificado el Calibrado
     * 
     * @param fecha LocalDate con la fecha de creación/modificación
     */
    public void setFecha(LocalDate fecha) {
        this.fecha.set(fecha);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el DatePicker del Editor y el label de la pestaña de Calibraciones
     * 
     * @return ObjectProperty con la fecha del Calibrado
     */
    public ObjectProperty<LocalDate> fechaProperty() {
        return fecha;
    }

    /**
     * Devuelve el nombre del Ajuste utilizado en este Calibrado
     * 
     * @return String con el nombre del Ajuste
     */
    public Ajuste getAjuste() {
        return ajuste.get();
    }

    /**
     * Establece el nombre del Ajuste utilizado en este Calibrado
     * 
     * @param ajuste String con el nombre del Ajuste
     */
    public void setAjuste(Ajuste ajuste) {
        this.ajuste.set(ajuste);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el textField del Editor y el label de la pestaña de Calibraciones
     * 
     * @return StringProperty con el nombre del Ajuste de este Calibrado
     */
    public ObjectProperty<Ajuste> ajusteProperty() {
        return ajuste;
    }

    /**
     * Devuelve si el Calibrado está activo o no para usarse en un Analisis
     * 
     * @return boolean true=SI está activo, false=NO está activo
     */
    public boolean isActivo() {
        return activo.get();
    }

    /**
     *  Establece si el Calibrado está activo o no para usarse en un Analisis
     * 
     * @param activo boolean true=SI está activo, false=NO está activo
     */
    public void setActivo(boolean activo) {
        this.activo.set(activo);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el textField del Editor y el label de la pestaña de Calibraciones
     * 
     * @return BooleanProperty con el estado del Calibrado (true=activado/false=desactivado)
     */
    public BooleanProperty activoProperty() {
        return activo;
    }

    /**
     * Devuelve la lista de objetos Patron que conforman el Calibrado
     * 
     * @return ObservableList con los patrones implicados en el Calibrado
     */
    public ObservableList<Patron> getListaPatrones() {
        return listaPatrones.get();
    }

    /**
     * Establece la lista de objetos Patron que conforman el Calibrado
     * 
     * @param listaPatrones ObservableList con los patrones implicados en el Calibrado
     */
    public void setListaPatrones(ObservableList<Patron> listaPatrones) {
        this.listaPatrones.set(listaPatrones);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el Editor de Calibraciones
     * 
     * @return ObjectProperty con la lista de objetos Patron implicados en el Calibrado
     */
    public ObjectProperty<ObservableList<Patron>> listaPatronesProperty() {
        return listaPatrones;
    }

    
  
    
    
    public Ecuacion getEcuacion() {
        return ecuacion.get();
    }    
    public void setEcuacion(Ecuacion ecuacion) {
        this.ecuacion.set(ecuacion);
    }
    public ObjectProperty<Ecuacion> ecuacionProperty() {
        return ecuacion;
    }
    
    
    
    
    /**
     * Devuelve el valor mínimo para el que el Calibrado es efectivo
     * 
     * @return float con el % mínimo para que el que el calibrado es válido
     */
    public float getMinimo() {
        return minimo.get();
    }

    /**
     * Establece el valor mínimo para el que el Calibrado es efectivo
     * 
     * @param minimo float con el % mínimo para que el que el calibrado es válido
     */
    public void setMinimo(float minimo) {
        this.minimo.set(minimo);
    }
    
    /**
     * Devuelve el valor máximo para el que el Calibrado es efectivo
     * @return float con el % máximo para que el que el calibrado es válido
     */
    public float getMaximo() {
        return maximo.get();
    }

    /**
     * Establece el valor máximo para el que el Calibrado es efectivo
     * @param maximo float con el % máximo para que el que el calibrado es válido
     */
    public void setMaximo(float maximo) {
        this.maximo.set(maximo);
    }

    /**
     * Devuelve el tipo de regresión usadada por el Calibrado (lineal, cuadrática)
     * 
     * @return String con el tipo de regresión
     */
    public String getTipoRegresion() {
        return tipoRegresion.get();
    }

    /**
     * Establece el tipo de regresión usadada por el Calibrado (lineal, cuadrática)
     * 
     * @param regresion String con el tipo de regresión
     */
    public void setTipoRegresion(String regresion) {
        this.tipoRegresion.set(regresion);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el Editor de Calibraciones
     * 
     * @return StringProperty con con el tipo de regresión
     */
    public StringProperty tipoRegresionProperty() {
        return tipoRegresion;
    }

    /**
     * Devuelve un String con el nombre de todos los patrones implicados en el Calibrado
     * 
     * @return String con el nombre de todos los patrones
     */
    public String getPatronesString() {
        return patronesString.get();
    }

    /**
     * Establece un String con el nombre de todos los patrones implicados en el Calibrado
     * 
     * @param patrones String con el nombre de todos los patrones
     */
    public void setPatronesString(String patrones) {
        this.patronesString.set(patrones);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el label de la pestaña de Calibraciones
     * 
     * @return StringProperty con el nombre de todos los patrones
     */
    public StringProperty patronesStringProperty() {
        return patronesString;
    }

    /**
     * Devuelve un String con el rango operativo del Calibrado (desde % mínimo a % máximo)
     * 
     * @return String con el rango de trabajo del Calibrado
     */
    public String getRangoString() {
        return rangoString.get();
    }

    /**
     * Establece el rango operativo del Calibrado (desde % mínimo a % máximo)
     * 
     * @param rango String con el rango de trabajo del Calibrado
     */
    public void setRangoString(String rango) {
        this.rangoString.set(rango);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el label de la pestaña de Calibraciones
     * 
     * @return StringProperty con el rango de trabajo del Calibrado
     */
    public StringProperty rangoStringProperty() {
        return rangoString;
    }

    //OTROS METODOS    
    
    /**
     * Este método crea un String en el que se mencionan, separados por comas,
     * el nombre de todos los patrones involucrados en este calibrado.
     */
    public void actualizaPatronesString() {
        if (listaPatrones.get() != null) { // Si hay objetos Patron en la lista
            String patrones = "";
            for (int i = 0; i < this.getListaPatrones().size(); i++) {
                if (i != this.getListaPatrones().size() - 1) { // Hasta el penultimo agrego una coma detrás
                    patrones = patrones + this.getListaPatrones().get(i).getNombre() + ", ";
                } else {
                    patrones = patrones + this.getListaPatrones().get(i).getNombre();
                }
            }
            setPatronesString(patrones); // Utilizo el setter para establecer el String que muestra todos los patrones
        }
    }

    /**
     * Este método crea un String en el que se muestra el rango operativo del
     * calibrado, mostrando el mínimo y máximo valor en los que un resultado
     * puede considerarse válido.
     */
    public void actualizaRangoString() {
        if (listaPatrones.get() != null) {
            float minimo = 100;
            float maximo = 0;
            for (Patron p : listaPatrones.get()) {
                if (p.getConcentracion() < minimo) {
                    minimo = p.getConcentracion();
                }
                if (p.getConcentracion() > maximo) {
                    maximo = p.getConcentracion();
                }
            }

            setMinimo(minimo);
            setMaximo(maximo);
            setRangoString("desde " + String.format("%.4f", this.minimo.get()) + "% hasta " + String.format("%.4f", this.maximo.get()) + "%");
        }
    }

    
    
    public boolean cumple(){
        if (tipoRegresion.get().equals("Lineal") & listaPatrones.get().size()<2){
            
            return false;            
        }
        else if (tipoRegresion.get().equals("Cuadrática") & listaPatrones.get().size()<3){
            
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Calibrado{" + "nombre=" + nombre.get() + ", fecha=" + fecha.get() + ", ajuste=" + ajuste.get() +
               ", activo=" + activo.get() + ", tipoRegresion=" + tipoRegresion.get() + ", patrones=" + patronesString.get() + '}';
    }  
}