package com.pasegados.labo.conexionesbbdd;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Esta clase representa la conexión principal a la BBDD, con un conector de tipo HSQLDB, que genera y conecta a una
 * BBDD localizada como archivos en la carpeta /bbdd del proyecto, sin requerir servidor externo.
 
 * @author Pedro Antonio Segado Solano
 */
public class Conexion implements Cloneable {
    
    private final static String urlConexion = "jdbc:hsqldb:bbdd/datos"; // verificar si se necesita ;ifexists=false ó ;ifexists=true
    private Connection con;
    private Statement st;
    private static final Conexion INSTANCIA = new Conexion(); // Asignación constante Singleton

    // Singleton - Constructor protected para que solo las clases que heredan puedan llamar al método super()
    protected Conexion() {        
    }
    
    // Singleton - Método estático Singleton
    public static Conexion getINSTANCIA () {
        return INSTANCIA;
    }
    
    // Singleton - Para evitar la clonación del objeto
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Este método inicia la conexión a la BBDD
     * @return Conecction que gestiona la conexión establecida
     * @throws SQLException si hay error al conectar
     */
    public Connection iniciarConexion() throws SQLException {        
        con =  DriverManager.getConnection(urlConexion);    
        return con;        
    }
        
    /**
     * Este método detiene la conexión con la BBDD
     * @throws SQLException si hay error al desconectar
     */
    public void detenerConexion() throws SQLException {
        con.close();
    }
    
    /**
     * Este método permite saber si la comunicación con la BBDD está cerrada
     * @return Boolean true si esta cerrada, false si esta abierta
     * @throws SQLException si hay un error al verificar la conexión
     */
    public boolean isCerrado() throws SQLException{
        return con.isClosed();
    }
    
    public void cerrarBase() throws SQLException{
        Statement statement = con.createStatement();
        statement.execute("SHUTDOWN");
        statement.close();

    }
    
    /**
     * Este método verifica si existe la BBDD 
     * @return Boolean true si existe, false si no se ha creado aún
     */
    public boolean existeBBDD(){
        File archivo = new File("bbdd/datos.script");
        return archivo.exists(); // Si existe retorna true y si no false
    }
    
    /**
     * Este método crea la estructura de tablas necesarias para empezar a trabajar
     * @throws SQLException si no se puede generar la BBDD
     */
    public void crearEstructura() throws SQLException{
        iniciarConexion();
        st = con.createStatement(); 
        
    // --- TABLAS --- //
    
        String tablaAjuste = "CREATE TABLE Ajuste (nombre VARCHAR(32) PRIMARY KEY, "
                                                + "analisisPagina INT NOT NULL, "
                                                + "analisisMenu INT NOT NULL, "
                                                + "calibracionPagina INT NOT NULL, "
                                                + "calibracionMenu INT NOT NULL, "
                                                + "tiempo INT CONSTRAINT Chk_Aj_NotNullAndRange CHECK (tiempo IS NOT NULL AND tiempo BETWEEN 10 AND 300))";
        st.executeUpdate(tablaAjuste);
                
        String tablaCalibraciones = "CREATE TABLE Calibracion (nombre VARCHAR(32) PRIMARY KEY, "
                                                               + "fecha DATE NULL, "
                                                               + "activo BOOLEAN NOT NULL, "
                                                               + "ajuste VARCHAR(32) NULL, "
                                                               + "tipoRegresion VARCHAR(16) NULL, "
                                                               + "FOREIGN KEY (ajuste) REFERENCES Ajuste(nombre) ON UPDATE CASCADE ON DELETE SET NULL)";
        st.executeUpdate(tablaCalibraciones);
                
        String tablaAnalisis = "CREATE TABLE Analisis (numMuestra INT, "
                                          + "identificacion VARCHAR(32), "
                                          + "fecha DATE NOT NULL, "
                                          + "resultado DECIMAL(5,4) NOT NULL, "
                                          + "calibrado VARCHAR(32), "
                                          + "cuentas INT NOT NULL, " 
                                          + "PRIMARY KEY (numMuestra,identificacion,calibrado)," // Trigger para copiar a otra tabla antes de borrar por FK
                                          + "FOREIGN KEY (calibrado) REFERENCES Calibracion(nombre) ON UPDATE CASCADE ON DELETE CASCADE)";         
        st.executeUpdate(tablaAnalisis);
        
        String tablaPatrones = "CREATE TABLE Patron (nombre VARCHAR(32) PRIMARY KEY, "
                                          + "fecha DATE NULL, "
                                          + "concentracion DECIMAL(5,4) NOT NULL)";
        st.executeUpdate(tablaPatrones);
        
        String tablaConfiguracion = "CREATE TABLE Configuracion (puerto VARCHAR(16) PRIMARY KEY, "
                                                              + "bps VARCHAR(16) NOT NULL, "
                                                              + "bdd VARCHAR(16) NOT NULL, "
                                                              + "par VARCHAR(16) NOT NULL, "
                                                              + "bdp VARCHAR(16) NOT NULL, "  
                                                              + "pulsaciones INT NOT NULL, "
                                                              + "preacondicionamiento INT NOT NULL, "
                                                              + "acondicionamiento INT NOT NULL, "
                                                              + "preenergia INT NOT NULL, "
                                                              + "energia INT NOT NULL, "
                                                              + "premedida INT NOT NULL)";
        st.executeUpdate(tablaConfiguracion);
        
        String tablaCalibradoPatron = "CREATE TABLE CalibracionPatron (nombreCalibrado VARCHAR(32), "
                                                                  + "nombrePatron VARCHAR(32), "                
                                                                  + "PRIMARY KEY (nombreCalibrado,nombrePatron), "
                                                                  + "FOREIGN KEY (nombreCalibrado) REFERENCES Calibracion(nombre) ON UPDATE CASCADE ON DELETE CASCADE, "
                                                                  + "FOREIGN KEY (nombrePatron) REFERENCES Patron(nombre) ON UPDATE CASCADE ON DELETE CASCADE)";
        st.executeUpdate(tablaCalibradoPatron);
        
        String tablaPatronAjuste = "CREATE TABLE PatronAjuste (nombrePatron VARCHAR(32), "
                                                            + "nombreAjuste VARCHAR(32), "
                                                            + "cuentas INT NOT NULL, " 
                                                            + "PRIMARY KEY (nombrePatron,nombreAjuste), "
                                                            + "FOREIGN KEY (nombrePatron) REFERENCES Patron(nombre) ON UPDATE CASCADE ON DELETE CASCADE, "
                                                            + "FOREIGN KEY (nombreAjuste) REFERENCES Ajuste(nombre) ON UPDATE CASCADE ON DELETE CASCADE)";
        st.executeUpdate(tablaPatronAjuste);
        
        String tablaAnalisisOLD = "CREATE TABLE AnalisisOLD (numMuestra INT, "
                                                          + "identificacion VARCHAR(32), "
                                                          + "fecha DATE NOT NULL, "
                                                          + "resultado DECIMAL(5,4) NOT NULL, "
                                                          + "calibrado VARCHAR(32), "
                                                          + "cuentas INT NOT NULL, " 
                                                          + "PRIMARY KEY (numMuestra,identificacion,calibrado))";                                                                
        st.executeUpdate(tablaAnalisisOLD);      
        
        
        // --- TRIGGERS --- //
        
        // Al borrar un ajuste, este hace SET null del campo 'ajuste' en la tabla Calibraciones, en aquellos registros que usaban este ajuste borrado.
        // Vamos previamente al UPDATE de Calibraciones, verificar si el UPDATE es por un Ajuste a NULL, y en ese caso, cambiaremos el campo activo a FALSE
        // para desactivar el Calibrado en la aplicación, y que no se pueda usar hasta que el usuario lo revise y asigne otro ajuste de trabajo. Ademas 
        // vamos a cambiar la regresion a "Seleccionar..." para que el usuario escoja de nuevo.
        String triggerUpdCal = "CREATE TRIGGER updCal " +
                               "BEFORE UPDATE ON Calibracion " +
                               "REFERENCING NEW AS newrow FOR EACH ROW " +                                     
                               "BEGIN ATOMIC " +                              
                               "IF newrow.ajuste IS NULL THEN " +
                               "SET newrow.activo = FALSE, newrow.tipoRegresion = 'Seleccionar...'; " +
                               "END IF; " +
                               "END";
        st.executeUpdate(triggerUpdCal);
        
        // Al borrar un calibrado, esto provoca que en la tabla Analisis se borren los registros que usan ese calibrado.
        // Tras borrar un registro de Analisis, vamos a copiar los datos a una tabla llamada AnalisisOLD en la que el campo Calibrado
        // no va a estar referenciado a Calibraciones.nombre, ya que estos calibrados ya no existen y daría error de integridad        
        String triggerdelAna =   "CREATE TRIGGER delAna " +
                                  "AFTER DELETE ON Analisis " +
                                  "REFERENCING OLD ROW AS oldrow FOR EACH ROW " +                                  
                                  "BEGIN ATOMIC " +                              
                                  "INSERT INTO AnalisisOLD VALUES (oldrow.numMuestra, oldrow.identificacion, oldrow.fecha, oldrow.resultado, oldrow.calibrado+'_BORRADO', oldrow.cuentas); " +
                                  "END";                                    
        st.executeUpdate(triggerdelAna);
        
        //CREAMOS AJUSTES POR DEFECTO
        st.executeUpdate("INSERT INTO Ajuste VALUES ('LZMET048',1,1,1,1,200)");
        st.executeUpdate("INSERT INTO Ajuste VALUES ('LZMET049',1,1,1,1,200)");
        st.executeUpdate("INSERT INTO Ajuste VALUES ('LZMET050',1,1,1,1,99)");
        
        // Puerto y Config por defecto
        st.executeUpdate("INSERT INTO Configuracion VALUES ('COM1','2400','8','Ninguna','1',300,13,10,36,10,66)");
        // Ajuste por defecto, sera INBORRABLE
      /*  st.executeUpdate("INSERT INTO Ajuste VALUES ('Seleccionar...','ESC',1)");*/
        
        
        System.out.println("Tablas creadas con éxito.");
        detenerConexion();
    }
    

}
