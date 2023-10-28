package com.pasegados.labo.modelos;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

/**
 * Esta clase abstracta va a generar filtros para ser usados en los textfields que los necesiten
 * 
 * @author Pedro A. Segado Solano
 */
public abstract class Filtros {
    
    // Filtro entrada solo números para TextField nº de muestra, con máximo indicado por parámetro
    public static UnaryOperator<TextFormatter.Change> getNumeroFilter(int max) {
        return change -> {
            String nuevaEntrada = change.getControlNewText();
            if (nuevaEntrada.isEmpty()) {
                return change;
            }
            try {
                int newValue = Integer.parseInt(nuevaEntrada);
                if (newValue <= max) {
                    return change; // devuelve el cambio como valido
                } 
            } catch (NumberFormatException e) {
                //
            }
            return null; 
        };
    }
    
    // Filtro para un máximo de x caracteres pasados por parámetro, de cualquier tipo
    public static UnaryOperator<TextFormatter.Change> getMaxTamanioFilter(int maxTamanio) {
        return change -> {
            String newText = change.getControlNewText();
            if (newText.length() <= maxTamanio) {
                return change;
            }
            return null;
        };
    }

    // Filtro entrada solo números decimales, con maximo pasados por parametro
    public static UnaryOperator<TextFormatter.Change> getDecimalFilter(double max) {
        return change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            try {
                double newValue = Double.parseDouble(newText); // Evito que se pueda introducir la d de double o f de float
                if ((newValue <= max) & !(newText.contains("d")||newText.contains("f"))) {
                    return change;
                }
            } catch (NumberFormatException e) {
                //
            }
            return null;
        };
    }  
    
    // Filtro entrada para TextField, con un máximo de caracteres indicados por parámetro. Puede ser numericos pero
    // ademas permite algunos caracteres especiales que no son numeros que se reciben como un String que los incluye
    public static UnaryOperator<TextFormatter.Change> getNumeroEspecialFilter(int max, String caracteres) {
        
         return change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9"+caracteres+"]*") && newText.length() <= max) {
                return change;
            }
            return null;
        };
    }
}
