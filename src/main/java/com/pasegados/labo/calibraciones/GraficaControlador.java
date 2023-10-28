package com.pasegados.labo.calibraciones;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import com.pasegados.labo.modelos.Calibrado;
import java.sql.SQLException;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Esta clase representa al controlador de la gráfica de un Calibrado, al que se accede cada vez que se pulsa el botón
 * correspondiente desde la pestaña Calibrados o desde el editor de estos
 *
 * @author Pedro Antonio Segado Solano
 */
public class GraficaControlador {

    // Contenedor
    @FXML
    private AnchorPane anchorPane;

    // Etiqueta ecuacion
    @FXML
    private Label lbEcuacion;

    // Grafica
    @FXML
    private LineChart<Number, Number> lcGrafico;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private NumberAxis xAxis;

    // Otras variables necesarias
    private Calibrado calibrado;
    private Scene scene;
    private static final Logger LOGGER = LogManager.getLogger(TabCalibracionesControlador.class); // Logger del controlador que lo llama, siempre el de calibraciones


    /**
     * Este método genera la gráfica de un calibrado.
     */
    public void dibuja() {
        //Cargamos las cuentas de cada patrón adecuadas al ajuste del equipo usado en el calibrado
        calibrado.getListaPatrones().forEach(p -> {
            try {
                p.RecuperarCuentasAjuste(calibrado.getAjuste().getNombre());
            } catch (SQLException ex) {
                LOGGER.fatal("GRAFICA: Error al recuperar cuentas del patron " + p.getNombre() + " bajo el ajuste " + calibrado.getAjuste().getNombre());
            }
        });

        // Preparo las dos series, una para los puntos de nuestros patrones y otra para la recta/curva de la ecuación
        XYChart.Series<Number, Number> seriesPatrones = new XYChart.Series<>(); 
        XYChart.Series<Number, Number> seriesRecta = new XYChart.Series<>(); // 
        double[] xs = new double[calibrado.getListaPatrones().size()]; // Array para almacenar la concentración de cada patrón
        double[] ys = new double[calibrado.getListaPatrones().size()]; // Array para almacenar las cuentas de cada patron frente a la concentración
        
        double min = 100000; // Buscaremos el minimo de cuentas desde el maximo (el equipo nunca va a llegar a 100000 cps)
        double max = 0; // Buscaremos el máximo de cuentas desde el mínimo (el equipo siempre dará algo de lectura, nunca 0 cvps)

        // Para cada patrón, generamos su punto para la serie
        for (int i = 0; i < calibrado.getListaPatrones().size(); i++) {            
            xs[i] = calibrado.getListaPatrones().get(i).getCuentas(); // cuentas en eje X
            ys[i] = calibrado.getListaPatrones().get(i).getConcentracion(); // Concentración de azufre en eje Y
            if (xs[i] < min) { // busco los valores máximo y minimo de cuentas de entre todos los patrones
                min = xs[i];
            }
            if (xs[i] > max) {
                max = xs[i];
            }
        }
        // Añado los puntos a la serie de los patrones
        for (int i = 0; i < xs.length; i++) {
            seriesPatrones.getData().add(new XYChart.Data<>(xs[i], ys[i]));
        }
        
        // Para generar la recta-curva voy a calcular una resolución que me diga cuantos puntos necesito para que en
        // caso de ser una curva se vea suavizada                
        double resolucion = 0; 
        double rango = max - min; // rango de cuentas del calibrado

        if (rango <= 2500) { // hasta 2500 cuentas            
            resolucion = 50; // puntos en ecuacion de 50 en 50 cuentas
        } else if (rango <= 15000) { // hasta 1.5%            
            resolucion = 500; // puntos en ecuacion de 500 en 500 cuentas
        } else if (rango > 15000) { // desde 1.5%            
            resolucion = 1000; // puntos en ecuacion de 1000 en 1000 cuentas
        }

        // Creo la recta/curva empezando en el mínino - resolucion y acabando en el maximo + resolucion
        for (double x = min - resolucion; x <= max + resolucion; x += resolucion) {
            double y = calculaConcentracion(x, calibrado);
            while (y < 0) { // Evitamos grafica con concetración negativa generando un nuevo punto algo mayor
                x = x + 5; // aumentamos 5 cuentas y calculamos la concentración
                y = calculaConcentracion(x, calibrado);
            }
            seriesRecta.getData().add(new XYChart.Data<>(x, y)); // Añadimos el punto a la serie de la recta-curva
        }
        
        // Ajustes necesarios para los ejes y la gráfica, y añadimos las dos series creadas anteriormente
        xAxis.setForceZeroInRange(false); // No hay que iniciar en 0 el eje X si no es necesario
        xAxis.setLabel("Cuentas (cps)"); 
        yAxis.setForceZeroInRange(false); // No hay que iniciar en 0 el eje Y si no es necesario
        yAxis.setLabel("Azufre (%)");
        lcGrafico.setLegendVisible(false); // Esconde etiqueta de la leyenda inferior (por defecto muestra un 0)
        lcGrafico.getData().add(seriesRecta); //Muestra los puntos de los patrones y la recta o curva calculada
        lcGrafico.getData().add(seriesPatrones); //Muestra los puntos de los patrones y la recta o curva calculada

        generaEtiqueta(calibrado); // genera la etiqueta con la ecuación y coeficiente de determinación
    }

    // Calcula concetración correspondiente a un numero de cuentas, en funcion de los coeficientes y tipo de regresión indicados
    private double calculaConcentracion(double cuentas, Calibrado calibrado) {
        double concentracion;
        if (calibrado.getTipoRegresion().equals("Cuadrática")) {
            concentracion = (calibrado.getCoefCuadratico() * Math.pow(cuentas, 2)) + (calibrado.getCoefLineal() * cuentas) + calibrado.getTerminoIndep();
        } else { // Lineal
            concentracion = (calibrado.getCoefLineal() * cuentas) + calibrado.getTerminoIndep();
        }
        return concentracion;
    }

    // Genera la etiqueta que se muestra en la gráfica, con la ecuación y el coeficiente de determinacion
    private void generaEtiqueta(Calibrado calibrado) {
        String etiq;

        if (calibrado.getTipoRegresion().equals("Cuadrática")) {
            etiq = "y = " + calibrado.getCoefCuadratico() + "x\u00B2 + " + calibrado.getCoefLineal() + "x + " + String.format("%.5f", calibrado.getTerminoIndep())
                    + "\n" + "R\u00B2" + " = " + String.format("%.6f", calibrado.getCoefDeterminacion());
        } else {
            etiq = calibrado.getCoefLineal() + "x + " + String.format("%.5f", calibrado.getTerminoIndep())
                    + "\n" + "R\u00B2" + " = " + String.format("%.6f", calibrado.getCoefDeterminacion());
        }

        etiq = etiq.replace("+ -", "- "); // para que la ecuación quede con los signos bien representadios, como siempre suma, eliminamos el + en caso de restar
        lbEcuacion.setText(etiq);
        lbEcuacion.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
    }

    /**
     * Este método recibe un objeto Calibrado, para utilizar sus atributos para realizar la gráfica     *
     * @param calibrado Calibrado con el que trabajar
     */
    public void setCalibrado(Calibrado calibrado) {
        this.calibrado = calibrado;
    }

    /**
     * Este método establece la escena sobre la que trabajar     *
     * @param scene Scene con la escena
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Este método crea una imagen PNG de la gráfica del calibrado, para usarla al generar un informe
     *
     * @param lineChart Scene que contiene la gráfica
     * @param path String con la ruta donde se guardará la imagen PNG
     */
    public void guardaImagenPNG(Scene lineChart, String path) {
        anchorPane.setPrefWidth(640);
        anchorPane.setPrefHeight(480);
        lcGrafico.lookup(".chart").setStyle("-fx-background-color: white;");
        lcGrafico.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        lbEcuacion.setVisible(false);
        WritableImage image = scene.snapshot(null);
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            LOGGER.fatal("GRAFICA: Error al generar la imagen PNG" + "\n" + e.getMessage());
        }
    }
}
