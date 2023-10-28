module com.pasegados.labo {
    
    requires javafx.graphics; //añadir transitive para evitar advertencias de error
    requires javafx.controls; //añadir transitive para evitar advertencias de error
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires java.sql;
    requires jasperreports;      
    requires commons.math3;
    requires com.fazecast.jSerialComm;    
    requires org.apache.logging.log4j;
                 
    opens com.pasegados.labo;    
    opens com.pasegados.labo.analisis;     
    opens com.pasegados.labo.calibraciones;
    opens com.pasegados.labo.conexionesbbdd;
    opens com.pasegados.labo.configuracion;
    opens com.pasegados.labo.modelos;
    opens com.pasegados.labo.resultados;
    opens com.pasegados.labo.utilidades; //to javafx.fxml, java.desktop;
    
    exports com.pasegados.labo;
    exports com.pasegados.labo.analisis;
    exports com.pasegados.labo.calibraciones;
    exports com.pasegados.labo.conexionesbbdd;
    exports com.pasegados.labo.configuracion;
    exports com.pasegados.labo.modelos;
    exports com.pasegados.labo.resultados;
    exports com.pasegados.labo.utilidades;             
}