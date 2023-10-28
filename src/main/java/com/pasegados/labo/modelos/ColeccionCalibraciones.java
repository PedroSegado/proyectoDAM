package com.pasegados.labo.modelos;

import java.util.ArrayList;
import com.pasegados.labo.App;


/**
 *
 * @author Pedro Antonio Segado Solano
 */
public class ColeccionCalibraciones {

    public static ArrayList<Calibrado> getColeccionCalibraciones() {
        
        ArrayList<Calibrado> coleccionCalibraciones = new ArrayList<>();
        coleccionCalibraciones.addAll(App.getControladorCalibrados().getListaInforme());
        return coleccionCalibraciones;
    }
}