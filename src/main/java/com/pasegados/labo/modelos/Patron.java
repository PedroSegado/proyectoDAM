
package com.pasegados.labo.modelos;

import java.sql.SQLException;
import java.time.LocalDate;
import com.pasegados.labo.conexionesbbdd.ConexionesCalibrado;
import com.pasegados.labo.conexionesbbdd.ConexionesPatron;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * Esta clase representa un Patrón de azufre que se usará posteriormente junto a 
 * otros para crear un Calibrado
 * 
 * @author Pedro Antonio Segado Solano
 */
public class Patron implements Cloneable {

    private final ConexionesPatron CNP = ConexionesPatron.getINSTANCIA_PATRON();
    private final ConexionesCalibrado CNC = ConexionesCalibrado.getINSTANCIA_CALIBRADO();
    private StringProperty nombre;
    private ObjectProperty<LocalDate> fecha;
    private FloatProperty concentracion;
    private int cuentas;

    // CONSTRUCTORES

    /**
     * Patrón para generar posteriormente Calibrados
     * 
     * @param nombre String con el nombre del Patron
     * @param fecha LocalDate con la fecha de preparación del Patrón
     * @param concentracion float con la concentración exacta del patrón, redondeada a 4 decimales
     */
    
    public Patron(String nombre, LocalDate fecha, float concentracion) {
        this.nombre = new SimpleStringProperty(nombre);
        this.fecha = new SimpleObjectProperty<>(fecha);
        this.concentracion = new SimpleFloatProperty(concentracion);
        this.cuentas = 0;
    }

    /**
     * Constructor por defecto para usar como punto de partida en el Editor al clonar
     */
    public Patron() {
        this("", null, 0f);
    }
    
    // CLONADOR DE OBJETOS PATRON 
    
    @Override
    public Object clone() {
        Patron obj = new Patron();
        obj.setNombre(this.nombre.getValue());
        obj.setFecha(this.fecha.getValue());
        obj.setConcentracion(this.concentracion.getValue());
        obj.setCuentas(this.cuentas);
        return obj;
    }

    //GETTERS Y SETTERS

    /**
     * Devuelve el nombre del Patrón
     * @return String con el nombre
     */
    
    public String getNombre() {
        return nombre.get();
    }

    /**
     * Establece el nombre del Patrón
     * @param nombre String con el nombre
     */
    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    /**
     * Para Binding en el Editor de Patrones
     * @return StringProperty con el nombre
     */
    public StringProperty nombreProperty() {
        return nombre;
    }

    /**
     * Devuelve la fecha de preparación del Patrón
     * @return LocalDate seleccionado con el DatePicker en el editor de patrones
     */
    public LocalDate getFecha() {
        return fecha.get();
    }

    /**
     * Establece la fecha de preparación del Patrón
     * @param fecha LocalDate seleccionado con el DatePicker en el editor de patrones
     */
    public void setFecha(LocalDate fecha) {
        this.fecha.set(fecha);
    }

    /**
     * Para Binding en el Editor de Patrones
     * @return ObjectProperty con la fecha de preparación del Patrón    
     */
    public ObjectProperty<LocalDate> fechaProperty() {
        return fecha;
    }

    /**
     * Devuelve la concentración en % de azufre que contiene el patrón
     * @return float con el % de azufre que contiene el Patrón
     */
    public float getConcentracion() {
        return concentracion.get();
    }

    /**
     * Establece la concentración en % de azufre que contiene el patrón
     * @param concentracion float con el % de azufre que contiene el Patrón     
     */
    public void setConcentracion(float concentracion) {
        this.concentracion.set(concentracion);
    }

    /**
     * Para Binding en el Editor de Patrones
     * @return FloatProperty con la concentración del Patrón   
     */
    public FloatProperty concentracionProperty() {
        return concentracion;
    }

    /**
     * Devuelve el nº de cuentas reportado por el OXFORD para este patrón bajo un Ajuste determinado
     * @return int con el nº de cuentas
     */
    public int getCuentas() {
        return cuentas;
    }

    /**
     * Establece el nº de cuentas reportado por el OXFORD para este patrón bajo un Ajuste determinado
     * @param cuentas int con el nº de cuentas
     */
    public void setCuentas(int cuentas) {
        this.cuentas = cuentas;
    }

    
   
    // OTROS METODOS

    /**
     * Permite ajustar las cuentas del patrón a las correspondientes al Ajuste que se está usando
     * por el Calibrado en uso
     * @param ajuste String con el nombre del Ajuste del Calibrado con el que se trabaja
     */
    public void RecuperarCuentasAjuste(String ajuste) throws SQLException {                
        int verCuentas = 0;        
        if(!ajuste.equals("Seleccionar...")){
            verCuentas = CNC.cargarCuentasPatron(nombre.get(), ajuste);
        }        
        this.setCuentas(verCuentas);
    }
    
     /**
     * Permite actualizar las cuentas del patrón a las correspondientes al Ajuste que se está usando
     * por el Calibrado en uso
     * @param ajuste String con el nombre del Ajuste del Calibrado con el que se trabaja
     */
    public void ActualizarCuentasAjuste(String ajuste, int cuentas) throws SQLException {                
        CNC.actualizarCuentasPatron(nombre.get(), ajuste, cuentas);        
    }

    // Sobreescribimos el toString para mostrar un String personalizado
    @Override
    public String toString() {
        String cuentasString = (cuentas==0) ? "SIN ANALIZAR" : String.valueOf(cuentas);
        return nombre.get() + " (" + String.format("%.4f",concentracion.get()).replace(",",".") + "% - " + cuentasString + ")";
    }    
    
    public String toStringCompleto() {
     
        return "Patron{" + "nombre=" + nombre.get() + ", fecha=" + fecha .get()+ ", concentracion=" + concentracion.get() + '}'; //No informamos de las cuentas por ser
    }                                                                                                                           // un atributo de la relación Patron/Ajuste
}
