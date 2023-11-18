package com.pasegados.labo.modelos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Arrays;
import javafx.collections.ObservableList;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;


/**
 *
 * @author paseg
 */
public class Ecuacion {
    
    private ObservableList<Patron> listaPatrones;
    private String tipoRegresion;
    private String ajuste;

    public Ecuacion(ObservableList<Patron> listaPatrones, String tipoRegresion, String ajuste) {
        this.listaPatrones = listaPatrones;
        this.tipoRegresion = tipoRegresion;
        this.ajuste = ajuste;
    }
        
    // Calcula los coeficientes gracias a la librería apache.commons.math, devolviendolos como un array de tipo double
    public double[] calculaCoeficientes() {
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
                    System.out.println("Error al rescatar las cuentas del patron para el ajuste");
                    double[] nohay = {0, 0, 0}; // devuelvo 0 en los coeficientes
                    return nohay;
                }
            }

            int degree = 0;  // almacena el grado de la ecuación
            if (tipoRegresion.equals("Lineal")) { // Ecuación primer grado
                degree = 1;
            } else if (tipoRegresion.equals("Cuadrática")) { // Ecuación de segundo grado
                degree = 2;
            }

            if (cero) { //Ya que un patron no se ha analizado, devolvemos regresión 0
                double[] nohay = {0, 0, 0};
                return nohay;
            } else { //Todos los patrones tienen valor en cuentas distinto a 0, luego podemos calcular la regresión

                PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
                double[] coeficientes = fitter.fit(puntosObservados.toList()); // Genero los coef cuadrátivo, lineal y term. independiente
                double rSquared = calculateRSquared(coeficientes, puntosObservados); // Calculo el coef, de determinación R2

                double coef[] = Arrays.copyOf(coeficientes, coeficientes.length + 1); // Creo un nuevo array, con 1 posición mas
                coef[coef.length - 1] = rSquared; // Agrego el coef de determinación al array, en última posición
             
                return coef;
            }
        } //Si no hay patrones asignados, devolveremos cero para el calculo de regeasión.
        double[] nohay = {0, 0, 0};
        return nohay;
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