package com.pasegados.labo.modelos;

import java.util.ArrayList;

import com.pasegados.labo.App;

/**
 *
 * @author Pedro Antonio Segado Solano
 */
public class ColeccionResultados {

    public static ArrayList<Analisis> getColeccionResultados() {
        
        ArrayList<Analisis> coleccionResultados = new ArrayList<>();
        coleccionResultados.addAll(App.getControladorResultados().getListaInforme());
        return coleccionResultados;
    }
}