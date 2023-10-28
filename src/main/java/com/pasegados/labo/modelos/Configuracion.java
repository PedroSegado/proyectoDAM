
package com.pasegados.labo.modelos;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Esta clase representa un objeto que almacena toda la configuración necesaria para
 * el funcionamiento y buena comununicación entre OXFORD y PC
 * 
 * @author Pedro A. Segado Solano
 */
public class Configuracion {
    
    private IntegerProperty pulsaciones;
    private IntegerProperty preAcondicionamiento;
    private IntegerProperty acondicionamiento;
    private IntegerProperty preEnergia;
    private IntegerProperty energia;
    private IntegerProperty preMedida;

    // CONSTRUCTORES
    
    /**
     * Este objeto representa la configuración necesaria para los ajustes de funcionamiento del
     * programa
     * 
     * @param pulsaciones int con los milisegundos de espera entre cada pulsación enviada al puerto COM
     * @param preAcondicionamiento int con los milisegundos que dura el preacondicionamiento
     * @param acondicionamiento int con los segundos que dura la cuenta atras del acondicionamiento
     * @param preEnergia int con los milisegundos que dura el preajuste de energia
     * @param energia int con los segundos que dura la cuenta atrás del ajuste de energía
     * @param preMedida int con los milisegundos que dura el ajuste previo a la medida
     */
    public Configuracion(int pulsaciones, int preAcondicionamiento, int acondicionamiento, int preEnergia, int energia, int preMedida) {
        this.pulsaciones = new SimpleIntegerProperty(pulsaciones);
        this.preAcondicionamiento = new SimpleIntegerProperty(preAcondicionamiento);
        this.acondicionamiento = new SimpleIntegerProperty(acondicionamiento);
        this.preEnergia = new SimpleIntegerProperty(preEnergia);
        this.energia = new SimpleIntegerProperty(energia);
        this.preMedida = new SimpleIntegerProperty(preMedida);
    }

    public Configuracion() {
        this(0,0,0,0,0,0);
    }

    // GETTERS Y SETTERS

    /**
     * Devuelve los milisegundos que hay entre cada pulsación enviada por el puerto COM
     * 
     * @return int con los milisegundos de espera
     */
    
    public int getPulsaciones() {
        return pulsaciones.get();
    }

    /**
     * Establece los milisegundos que hay entre cada pulsación enviada por el puerto COM
     * 
     * @param pulsaciones int con los milisegundos de espera
     */
    public void setPulsaciones(int pulsaciones) {
        this.pulsaciones.set(pulsaciones);
    }

    /**
     * Para realizar Binding en el editor de Ajustes
     * 
     * @return IntegerProperty con los milisegundos de espera
     */
    public IntegerProperty pulsacionesProperty() {
        return pulsaciones;
    }
            
    /**
     * Devuelve los milisegundos que dura el PreAcondicionamiento del equipo
     * 
     * @return int con los milisegundos de duración
     */
    public int getPreAcondicionamiento() {
        return preAcondicionamiento.get();
    }

    /**
     * Establece los milisegundos que dura el PreAcondicionamiento del equipo
     * 
     * @param preAcondicionamiento int con los milisegundos de duración
     */
    public void setPreAcondicionamiento(int preAcondicionamiento) {
        this.preAcondicionamiento.set(preAcondicionamiento);
    }
    
    /**
     * Para realizar Binding en el editor de Ajustes
     * 
     * @return IntegerProperty con los milisegundos de espera
     */
    public IntegerProperty preAcondicionamientoProperty() {
        return preAcondicionamiento;
    }

    /**
     * Devuelve los milisegundos que dura la cuenta atrás del Acondicionamiento del equipo
     * 
     * @return int con los segundos de duración
     */
    public int getAcondicionamiento() {
        return acondicionamiento.get();
    }

    /**
     * Establece los milisegundos que dura la cuenta atrás del Acondicionamiento del equipo
     * 
     * @param acondicionamiento int con los segundos de duración
     */
    public void setAcondicionamiento(int acondicionamiento) {
        this.acondicionamiento.set(acondicionamiento);
    }
    
    /**
     * Para realizar Binding en el editor de Ajustes
     * 
     * @return IntegerProperty con los milisegundos de espera
     */
    public IntegerProperty acondicionamientoProperty() {
        return acondicionamiento;
    }

    /**
     * Devuelve los milisegundos que dura el PreAjuste de Energía del equipo
     * 
     * @return int con los milisegundos de duración
     */
    public int getPreEnergia() {
        return preEnergia.get();
    }

    /**
     * Establece los milisegundos que dura el PreAjuste de Energía del equipo
     * 
     * @param preEnergia int con los milisegundos de duración
     */
    public void setPreEnergia(int preEnergia) {
        this.preEnergia.set(preEnergia);
    }
    
    /**
     * Para realizar Binding en el editor de Ajustes
     * 
     * @return IntegerProperty con los milisegundos de espera
     */
    public IntegerProperty preEnergiaProperty() {
        return preEnergia;
    }

    /**
     * Devuelve los milisegundos que dura la cuenta atrás del Ajuste de Energía del equipo
     * 
     * @return int con los segundos de duración     
     */
    public int getEnergia() {
        return energia.get();
    }

    /**
     * Establece los milisegundos que dura la cuenta atrás del Ajuste de Energía del equipo
     * 
     * @param energia int con los segundos de duración
     */
    public void setEnergia(int energia) {
        this.energia.set(energia);
    }
    
    /**
     * Para realizar Binding en el editor de Ajustes
     * 
     * @return IntegerProperty con los milisegundos de espera
     */
    public IntegerProperty energiaProperty() {
        return energia;
    }

    /**
     * Devuelve los milisegundos que dura el PreAjuste de Medida del equipo
     * 
     * @return int con los milisegundos de duración     
     */
    public int getPreMedida() {
        return preMedida.get();
    }

    /**
     * Establece los milisegundos que dura el PreAjuste de Medida del equipo
     * 
     * @param preMedida int con los milisegundos de duración 
     */
    public void setPreMedida(int preMedida) {
        this.preMedida.set(preMedida);
    }
    
    /**
     * Para realizar Binding en el editor de Ajustes
     * 
     * @return IntegerProperty con los milisegundos de duración    
     */
    public IntegerProperty preMedidaProperty() {
        return preMedida;
    }
}