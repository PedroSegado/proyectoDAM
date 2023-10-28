
package com.pasegados.labo.modelos;

import javafx.util.converter.BooleanStringConverter;

/**
 * Esta clase permite crear un objeto BooleanStringConverter personalizado,
 * para mostrar SI en vez de true, y NO en vez de false
 *
 * @author Pedro Antonio Segado Solano
 */
public class BooleanStringConverterPerso extends BooleanStringConverter {
    
    @Override
    public String toString(Boolean value) {
        return value ? "Sí" : "No";
    }

    @Override
    public Boolean fromString(String value) {
        return "SI".equalsIgnoreCase(value);
    }
}