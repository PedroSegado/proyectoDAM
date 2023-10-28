
package com.pasegados.labo.modelos;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Esta clase representa un objeto de tipo Ajuste de los que incluye el equipo OXFORD.
 * En función de las necesidades del calibrado, el equipo necesita ajustar unas 
 * condiciones de intensidad y tiempo de exposición. Para llegar a activar este ajuste
 * se simulan las pulsaciones de teclado que haría el analista físicamente sobre el 
 * equipo, pero enviandolas por el puerto COM.
 * Durante la medida de una muestra, se tendrá en cuenta la duración de la medida en
 * función del objeto ajuste que corresponde al calibrado con el que se trabaja.
 * 
 * @author Pedro A. Segado Solanog
 */
public class Ajuste implements Cloneable {
    
    private StringProperty nombre; // Nombre del ajuste 
    private StringProperty secuencia; // Pulsaciones para cargar la secuencia en el equipo, separadas por comas
    private IntegerProperty duracion; // Duración en segundos del análisis
    
    //CONSTRUCTORES
    
    public Ajuste(String nombre, String secuencia, int tiempo ) {        
        this.nombre = new SimpleStringProperty(nombre);        
        this.secuencia = new SimpleStringProperty(secuencia);
        this.duracion = new SimpleIntegerProperty(tiempo);
    }

    public Ajuste() {
        this("", "", 0);
    }

    // CLONADOR DE OBJETOS AJUSTE
    
    @Override
    public Object clone() {
        Ajuste obj = new Ajuste();
        obj.setNombre(this.nombre.getValue());
        obj.setSecuencia(this.secuencia.getValue());
        obj.setDuracion(this.duracion.getValue());        
        return obj;
    }

    //GETTERS Y SETTERS

    /**
     * Devuelve el nombre del Ajuste
     * @return
     */
    public String getNombre() {
        return nombre.get();
    }

    /**
     * Ajusta el nombre del Ajuste al indicado
     * @param nombre String con el nombre seleccionado para el ajuste
     */
    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    /**
     * Para hacer Binding entre el objeto Ajuste y el textField del controlador
     * @return StringProperty con el nombre del ajuste
     */
    public StringProperty nombreProperty() {
        return nombre;
    }
    
    /**
     * Devuelve la secuencia, separada por comas, de pulsaciones a enviar al equipo
     * para usar este ajuste
     * @return String con la secuencia a teclear para este ajuste
     */
    public String getSecuencia() {
        return secuencia.get();
    }

    /**
     * Ajusta la secuencia, separada por comas, de pulsaciones a enviar al equipo
     * @param secuencia String con la secuencia a teclear
     */
    public void setSecuencia(String secuencia) {
        this.secuencia.set(secuencia);
    }

    /**
     * Para hacer Binding entre el objeto Ajuste y el textField del controlador
     * @return StringProperty con la secuencia a teclear para este ajuste
     */
    public StringProperty secuenciaProperty() {
        return secuencia;
    }
    
    /**
     * Devuelve la duración en segundos de la medida en este ajuste
     * @return Int con los segundos necesarios para completar el análisis
     */
    public int getDuracion() {
        return duracion.get();
    }

    /**
     * Ajusta los segundos necesarios para completar un análisis con este ajuste
     * @param tiempo int Con los segundos necesarios
     */
    public void setDuracion(int tiempo) {
        this.duracion.set(tiempo);
    }

    /**
     * Para hacer Binding entre el objeto Ajuste y el textField del controlador
     * @return IntegerProperty con los segundos necesarios para este ajuste
     */
    public IntegerProperty tiempoProperty() {
        return duracion;
    }
    
    /**
     * Sobreescribe el método toString para mostrar solo el nombre del objeto en 
     * el listView de la pestaña Configuración
     */
    @Override
    public String toString() {
        return nombre.get();
    }
 
    /**
     * Muestra el String completo con la información del objeto Ajuste
     * @return String con toda la info del objeto Ajuste
     */
    public String toStringCompleto() {
        return "Ajuste{" + "nombre=" + nombre.get() + ", secuencia=" + secuencia.get() + ", duracion=" + duracion.get() + '}';
    }    
}
