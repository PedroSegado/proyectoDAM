
package com.pasegados.labo.modelos;

import java.sql.Date;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Esta clase representa un objeto de tipo Analisis de azufre realizado en el equipo
 * OXFORD LAB-X 3500 
 *
 * @author Pedro Antonio Segado Solano
 */
public class Analisis {
    
    private IntegerProperty muestra; 
    private StringProperty identificacion; 
    private ObjectProperty<Date> fecha;
    private FloatProperty resultado; 
    private StringProperty calibrado;
    private IntegerProperty cuentas; 

    // CONSTRUCTORES

    /**
     * Este objeto representa un Analisis realizado en el equipo OXFORD
     */    
    public Analisis() {
        this(0, null, null, 0f, null, 0);
    }

    /**
     * Este objeto representa un Analisis realizado en el equipo OXFORD
     * 
     * @param muestra Entero que representa el nº de muestra, 9 dígitos
     * @param identificacion String que aclara la procedencia de la muestra
     * @param fecha Date SQL de la fecha en la que se realiza el análisis
     * @param resultado Float para el resultado en % del análisis, con 4 decimales
     * @param calibrado String con el nombre del calibrado con el que se analizó la muestra
     * @param cuentas Entero con el número de cuentas obtenidas en el equipo
     */
    public Analisis(int muestra, String identificacion, Date fecha, float resultado, String calibrado, int cuentas) {
        this.muestra = new SimpleIntegerProperty(muestra);   
        this.identificacion = new SimpleStringProperty(identificacion);
        this.fecha = new SimpleObjectProperty<>(fecha);
        this.resultado = new SimpleFloatProperty(resultado);
        this.calibrado = new SimpleStringProperty(calibrado);
        this.cuentas = new SimpleIntegerProperty(cuentas);
    }
    
    // GETTERS Y SETTERS

    /**
     * Devuelve el número de muestra del análisis
     * 
     * @return int con el número de la muestra
     */    
    public int getMuestra() {
        return muestra.get();
    }

    /**
     * Establece el valor del número de muestra del análisis
     * 
     * @param muestra int con el número de la muestra
     */
    public void setMuestra(int muestra) {
        this.muestra.set(muestra);
    }
    
    /**
     * Para hacer Binding entre el objeto Analisis y la columna de la tabla en la pestaña resultados
     * 
     * @return IntegerProperty con el numero de muestra del Analisis
     */
    public IntegerProperty muestraProperty() {
        return muestra;
    }

    /**
     * Devuelve la información de identificación de muestra, auxiliar al nº de muestra
     * 
     * @return String con el identificador de la muestra
     */
    public String getIdentificacion() {
        return identificacion.get();
    }

    /**
     * Establece la información de identificación de muestra, auxiliar al nº de muestra
     * 
     * @param identificacion String con el identificador de la muestra
     */
    public void setIdentificacion(String identificacion) {
        this.identificacion.set(identificacion);
    }
    
    /**
     * Para hacer Binding entre el objeto Analisis y la columna de la tabla en la pestaña resultados
     * 
     * @return StringProperty con la identificacion del Analisis
     */
    public StringProperty identificacionProperty() {
        return identificacion;
    }

    /**
     * Devuelve la fecha en la que se ha realizado el análisis
     * 
     * @return Date tipo SQL con la fecha
     */
    public Date getFecha() {
        return fecha.get();
    }

    /**
     * Establece la fecha en la que se ha realizado el análisis
     * 
     * @param fecha Date tipo SQL con la fecha
     */
    public void setFecha(Date fecha) {
        this.fecha.set(fecha);
    }
    
    /**
     * Para hacer Binding entre el objeto Analisis y la columna de la tabla en la pestaña resultados
     * 
     * @return ObjectoProperty con la fecha del Analisis
     */
    public ObjectProperty<Date> fechaProperty() {
        return fecha;
    }

    /**
     * Devuelve el resultado en % del análisis
     * 
     * @return float con el % de azufre obtenido
     */
    public float getResultado() {
        return resultado.get();
    }

    /**
     * Establece el resultado en % del análisis
     * 
     * @param resultado float con el % de azufre obtenido
     */
    public void setResultado(float resultado) {
        this.resultado.set(resultado);
    }
    
    /**
     * Para hacer Binding entre el objeto Analisis y la columna de la tabla en la pestaña resultados
     * 
     * @return FloatProperty con el resultado del Analisis
     */
    public FloatProperty resultadoProperty() {
        return resultado;
    }

    /**
     * Devuelve el nombre del calibrado con el que se realizó el análisis
     * 
     * @return String con el nombre del calibrado
     */
    public String getCalibrado() {
        return calibrado.get();
    }

    /**
     * Establece el nombre del calibrado con el que se realizó el análisis
     * 
     * @param calibrado String con el nombre del calibrado
     */
    public void setCalibrado(String calibrado) {
        this.calibrado.set(calibrado);
    }
    
    /**
     * Para hacer Binding entre el objeto Analisis y la columna de la tabla en la pestaña resultados
     * 
     * @return StringProperty con el calibrado usado en el Analisis
     */
    public StringProperty calibradoProperty() {
        return calibrado;
    }

    /**
     * Devuelve el nº de cuentas contabilizado en el análisis por el equipo OXFORD
     * 
     * @return int con el nº de cuentas
     */
    public int getCuentas() {
        return cuentas.get();
    }

    /**
     * Establece el nº de cuentas contabilizado en el análisis por el equipo OXFORD
     * 
     * @param cuentas int con el nº de cuentas
     */
    public void setCuentas(int cuentas) {
        this.cuentas.set(cuentas);
    }
    
    /**
     * Para hacer Binding entre el objeto Analisis y la columna de la tabla en la pestaña resultados
     * 
     * @return IntegerProperty con el numero de muestra del Analisis
     */
    public IntegerProperty cuentasProperty() {
        return cuentas;
    }    
 }