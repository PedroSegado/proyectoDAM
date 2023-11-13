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
    private IntegerProperty analisisPagina; // en que pagina de los menús esta este ajuste
    private IntegerProperty analisisMenu; // en que menú está dentro de la página
    private IntegerProperty calibracionPagina; // en que pagina de los menús esta este ajuste
    private IntegerProperty calibracionMenu; // en que menú está dentro de la página
    private IntegerProperty duracion; // Duración en segundos del análisis
        
    //CONSTRUCTORES
    
    public Ajuste(String nombre, int analisisPagina, int analisisMenu, int calibracionPagina, int calibracionMenu, int tiempo ) {        
        this.nombre = new SimpleStringProperty(nombre);        
        this.analisisPagina = new SimpleIntegerProperty(analisisPagina);
        this.analisisMenu = new SimpleIntegerProperty(analisisMenu);
        this.calibracionPagina = new SimpleIntegerProperty(calibracionPagina);
        this.calibracionMenu = new SimpleIntegerProperty(calibracionMenu);
        this.duracion = new SimpleIntegerProperty(tiempo);
    }

    public Ajuste() {
        this("",1,1,1,1,0);
    }

    // CLONADOR DE OBJETOS AJUSTE
    
    @Override
    public Object clone() {        
        Ajuste obj = new Ajuste();
        obj.setNombre(this.nombre.getValue());
        obj.setAnalisisPagina(this.analisisPagina.getValue());
        obj.setAnalisisMenu(this.analisisMenu.getValue());
        obj.setCalibracionPagina(this.calibracionPagina.getValue());
        obj.setCalibracionMenu(this.calibracionMenu.getValue());
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

    
    
    public int getAnalisisPagina() {
        return analisisPagina.get();
    }

    public void setAnalisisPagina(int analisisPagina) {
        this.analisisPagina.set(analisisPagina);
    }
    
    public IntegerProperty analisisPaginaProperty() {
        return analisisPagina;
    }
    
    public int getAnalisisMenu() {
        return analisisMenu.get();
    }

    public void setAnalisisMenu(int analisisMenu) {
        this.analisisMenu.set(analisisMenu);
    }
    
    public IntegerProperty analisisMenuProperty() {
        return analisisMenu;
    }
    
    public int getCalibracionPagina() {
        return calibracionPagina.get();
    }

    public void setCalibracionPagina(int calibracionPagina) {
        this.calibracionPagina.set(calibracionPagina);
    }
    
    public IntegerProperty calibracionPaginaProperty() {
        return calibracionPagina;
    }
    
    public int getCalibracionMenu() {
        return calibracionMenu.get();
    }

    public void setCalibracionMenu(int calibracionMenu) {
        this.calibracionMenu.set(calibracionMenu);
    }
    
    public IntegerProperty calilbracionMenuProperty() {
        return calibracionMenu;
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
        return "Ajuste{" + "nombre=" + nombre.get() + ",  duracion=" + duracion.get() + '}';
    }    
    
    
    public String getSecuencia(){
        
        final String INICIO_ANALISIS = "ENT,4,1";
        final String FIN_ANALISIS = ",1,ENT,y";
        
        String paginas = "";
        for (int i = 1; i<getAnalisisPagina(); i++){ // Si esta en la primera pagina no se cuenta
            paginas += ",4";
        }
        
        String posicionMenu = "," + String.valueOf(getAnalisisMenu());        
        
        return INICIO_ANALISIS + paginas + posicionMenu + FIN_ANALISIS;
    }
    
    public String getSecuenciaAjusteCoeficientes(double termInd, double coefLin, double coefCuad){
        
        final String INICIO_CALIB = "ENT,4,2,ENT,2,ENT,2";
        final String PREVIO_REGRESION = ",1,ENT,3,2,5";
        String a0 = "," +String.valueOf(termInd) + ",ENT";
        String a1 = "," +String.valueOf(coefLin) + ",ENT";;
        String a2 = "," +String.valueOf(coefCuad) + ",ENT";;
        final String FIN_CALIB = ",6,4,4,1,4,y,7,4";
        
        String paginas = "";
        for (int i = 1; i<getCalibracionPagina(); i++){ // Si esta en la primera pagina no se cuenta
            paginas += ",4";
        }
        
        String posicionMenu = "," + String.valueOf(getCalibracionMenu());        
        
        System.out.println(INICIO_CALIB + paginas + posicionMenu + PREVIO_REGRESION + a0 + a1 + a2+ FIN_CALIB);
        return INICIO_CALIB + paginas + posicionMenu + PREVIO_REGRESION + a0 + a1 + a2+ FIN_CALIB;
        
    }
}
