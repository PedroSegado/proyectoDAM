package com.pasegados.labo.modelos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Arrays;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;


/**
 *
 * @author paseg
 */
public class Ecuacion {
    
    private DoubleProperty coefCuadratico;
    private DoubleProperty coefLineal;
    private DoubleProperty terminoIndep;
    private DoubleProperty coefDeterminacion;
    
    public Ecuacion() {
        this.coefCuadratico = new SimpleDoubleProperty(0);
        this.coefLineal = new SimpleDoubleProperty(0);
        this.terminoIndep = new SimpleDoubleProperty(0);
        this.coefDeterminacion = new SimpleDoubleProperty(0); 
    }
    
    
    /**
     * Devuelve el coeficiente cuadrático de la ecuación de segundo grado, o 0 si es lineal
     * 
     * @return float con el valor del coef. cuadrático de la ecuación del Calibrado
     */
    public double getCoefCuadratico() {
        return coefCuadratico.get();
    }

    /**
     * Establece el coeficiente cuadrático de la ecuación de segundo grado, o 0 si es lineal
     * 
     * @param coefCuad double con el valor del coef. cuadrático de la ecuación del Calibrado (0 si es lineal)
     */
    public void setCoefCuadratico(double coefCuad) {
        this.coefCuadratico.set(coefCuad);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el label del Editor y el label de la pestaña de Calibraciones
     * 
     * @return DoubleProperty con el valor del coef. cuadrático de la ecuación del Calibrado
     */
    public DoubleProperty coefCuadraticoProperty() {
        return coefCuadratico;
    }

    /**
     * Devuelve el coeficiente lineal de la ecuación del Calibrado
     * 
     * @return double con el valor del coef. lineal de la ecuación del Calibrado
     */
    public double getCoefLineal() {
        return coefLineal.get();
    }

    /**
     * Establece el coeficiente lineal de la ecuación del Calibrado
     * 
     * @param coefLin double con el valor del coef. lineal de la ecuación del Calibrado
     */
    public void setCoefLineal(double coefLin) {
        this.coefLineal.set(coefLin);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el label del Editor y el label de la pestaña de Calibraciones
     * 
     * @return DoubleProperty con el valor del coef. lineal de la ecuación del Calibrado
     */
    public DoubleProperty coefLinealProperty() {
        return coefLineal;
    }

    /**
     * Establece el termino independiente de la ecuación del Calibrado
     * 
     * @return double con el valor del termino independiente de la ecuación del Calibrado
     */
    public double getTerminoIndep() {
        return terminoIndep.get();
    }

    /**
     * Devuelve el termino independiente de la ecuación del Calibrado
     * 
     * @param terminIndep double con el valor del termino independiente de la ecuación del Calibrado
     */
    public void setTerminoIndep(double terminIndep) {
        this.terminoIndep.set(terminIndep);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el label del Editor y el label de la pestaña de Calibraciones
     * 
     * @return DoubleProperty con el valor del coef. lineal de la ecuación del Calibrado
     */
    public DoubleProperty terminoIndepProperty() {
        return terminoIndep;
    }
    
    /**
     * Devuelve el coeficiente de determinación R2 obtenido de la ecuación del Calibrado y los puntos con los que se ha generado
     * 
     * @return double con el valor del coeficiente de determinacion R2
     */
    public double getCoefDeterminacion() {
        return coefDeterminacion.get();
    }

    /**
     * Establece el coeficiente de determinación R2 obtenido de la ecuación del Calibrado y los puntos con los que se ha generado
     * 
     * @param coefDeterminacion double con el valor del coeficiente de determinacion R2
     */
    public void setCoefDeterminacion(double coefDeterminacion) {
        this.coefDeterminacion.set(coefDeterminacion);
    }

    /**
     * Para hacer Binding entre el objeto Calibrado y el label del Editor y el label de la pestaña de Calibraciones
     * 
     * @return DoubleProperty con el valor del coeficiente de determinacion R2
     */
    public DoubleProperty coefDeterminacionProperty() {
        return coefDeterminacion;
    }
    
    
    
        
    // Calcula los coeficientes gracias a la librería apache.commons.math, devolviendolos como un array de tipo double
    public void calculaCoeficientes(ObservableList<Patron> listaPatrones, String ajuste, String tipoRegresion) {   
        boolean cero = false; //Controla si algun patron tiene cuentas=0 por no haber sido analizado
        int tamanio = listaPatrones.size(); // Cuantos tenemos
        
        if (tamanio > 0) { // Tenemos algun patrón en la lista            
            WeightedObservedPoints puntosObservados = new WeightedObservedPoints(); // Preparamos el objeto que almacenas los pares

            for (int i = 0; i < tamanio; i++) { 
                //usar nueva tabla de ajuste, patron, cuentas
                try {
                    listaPatrones.get(i).RecuperarCuentasAjuste(ajuste);
                    double x = listaPatrones.get(i).getCuentas();                 
                    if (x == 0) { // Si las cuentas son 0, el patrón no ha sido analizado
                        cero = true;
                    }
                    // Almacenamos a concentración del Patrón
                    double y = round(listaPatrones.get(i).getConcentracion(),4); //si no redondeo mete mas decimales en la conversion

                    //Agrego el par al objeto para luego calcular los coeficientes de la recta/curva
                    puntosObservados.add(x, y);
                } catch (SQLException ex) {
                    coeficientesCero();
                }
            }

            int degree = 0;  // almacena el grado de la ecuación
            if (tipoRegresion.equals("Lineal")) { // Ecuación primer grado
                degree = 1;
            } else if (tipoRegresion.equals("Cuadrática")) { // Ecuación de segundo grado
                degree = 2;
            }

            if (cero) { //Ya que un patron no se ha analizado, devolvemos regresión 0                 
                coeficientesCero();
                
            } else { //Todos los patrones tienen valor en cuentas distinto a 0, luego podemos calcular la regresión
                PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
                double[] coeficientes = fitter.fit(puntosObservados.toList()); // Genero los coef cuadrátivo, lineal y term. independiente
                double rSquared = calculateRSquared(coeficientes, puntosObservados); // Calculo el coef, de determinación R2

                double coef[] = Arrays.copyOf(coeficientes, coeficientes.length + 1); // Creo un nuevo array, con 1 posición mas
                coef[coef.length - 1] = rSquared; // Agrego el coef de determinación al array, en última posición
                if (coef.length==3){                    
                    coefCuadratico.set(0);
                    coefLineal.set(coef[1]);
                    terminoIndep.set(coef[0]);
                    coefDeterminacion.set(coef[2]);
                } else if (coef.length==4){                    
                    coefCuadratico.set(coef[2]);
                    coefLineal.set(coef[1]);
                    terminoIndep.set(coef[0]);
                    coefDeterminacion.set(coef[3]);
                }               
            }
        } else { //Si no hay patrones asignados, los coeficientes son todos 0            
            coeficientesCero();
        }
    }
    
    public void coeficientesCero(){
        coefCuadratico.set(0);
                coefLineal.set(0);
                terminoIndep.set(0);
                coefDeterminacion.set(0);
    }

    // Redondea a los decimales indicados
    private static double round(double numero, int decimales) {
        if (decimales < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(numero);
        bd = bd.setScale(decimales, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Calcula el coeficiente de determinación R2, en función de la ecuación y los puntos que la generan
    private static double calculateRSquared(double[] coefficients, WeightedObservedPoints obs) {
        double meanY = obs.toList().stream().mapToDouble(point -> point.getY()).average().orElse(0);
        double ssTotal = obs.toList().stream().mapToDouble(point -> Math.pow(point.getY() - meanY, 2)).sum();
        double ssResidual = obs.toList().stream()
                .mapToDouble(point -> {
                    double x = point.getX();
                    double y = point.getY();
                    double predictedY = evaluatePolynomial(coefficients, x);
                    return Math.pow(y - predictedY, 2);
                })
                .sum();
        return 1 - (ssResidual / ssTotal);
    }

    // Aux para "calculateRSquared"
    private static double evaluatePolynomial(double[] coefficients, double x) {
        double y = 0;
        for (int i = 0; i < coefficients.length; i++) {
            y += coefficients[i] * Math.pow(x, i);
        }
        return y;
    }
}