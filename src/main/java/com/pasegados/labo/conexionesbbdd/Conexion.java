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
                                                + "tiempo INT CHECK (tiempo IS NOT NULL AND tiempo BETWEEN 5 AND 300))";
        st.execute(tablaAjuste);
                
        String tablaCalibraciones = "CREATE TABLE Calibracion (nombre VARCHAR(32) PRIMARY KEY, "
                                                               + "fecha DATE NULL, "
                                                               + "activo BOOLEAN NOT NULL, "
                                                               + "ajuste VARCHAR(32) UNIQUE NULL, "
                                                               + "tipoRegresion VARCHAR(16) NULL, "
                                                               + "FOREIGN KEY (ajuste) REFERENCES Ajuste(nombre) ON UPDATE CASCADE ON DELETE SET NULL)";
        st.execute(tablaCalibraciones);
                
        String tablaAnalisis = "CREATE TABLE Analisis (numMuestra INT, "
                                          + "identificacion VARCHAR(32), "
                                          + "fecha DATE NOT NULL, "
                                          + "resultado DECIMAL(5,4) NOT NULL, "
                                          + "calibrado VARCHAR(32), "
                                          + "cuentas INT NOT NULL, " 
                                          + "PRIMARY KEY (numMuestra,identificacion,calibrado)," // Trigger para copiar a otra tabla antes de borrar por FK
                                          + "FOREIGN KEY (calibrado) REFERENCES Calibracion(nombre) ON UPDATE CASCADE ON DELETE CASCADE)";         
        st.execute(tablaAnalisis);
        
        String tablaPatrones = "CREATE TABLE Patron (nombre VARCHAR(32) PRIMARY KEY, "
                                          + "fecha DATE NULL, "
                                          + "concentracion DECIMAL(5,4) NOT NULL)";
        st.execute(tablaPatrones);
        
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
        st.execute(tablaConfiguracion);
        
        String tablaCalibradoPatron = "CREATE TABLE CalibracionPatron (nombreCalibrado VARCHAR(32), "
                                                                  + "nombrePatron VARCHAR(32), "                
                                                                  + "PRIMARY KEY (nombreCalibrado,nombrePatron), "
                                                                  + "FOREIGN KEY (nombreCalibrado) REFERENCES Calibracion(nombre) ON UPDATE CASCADE ON DELETE CASCADE, "
                                                                  + "FOREIGN KEY (nombrePatron) REFERENCES Patron(nombre) ON UPDATE CASCADE ON DELETE CASCADE)";
        st.execute(tablaCalibradoPatron);
        
        String tablaPatronAjuste = "CREATE TABLE PatronAjuste (nombrePatron VARCHAR(32), "
                                                            + "nombreAjuste VARCHAR(32), "
                                                            + "cuentas INT NOT NULL, " 
                                                            + "PRIMARY KEY (nombrePatron,nombreAjuste), "
                                                            + "FOREIGN KEY (nombrePatron) REFERENCES Patron(nombre) ON UPDATE CASCADE ON DELETE CASCADE, "
                                                            + "FOREIGN KEY (nombreAjuste) REFERENCES Ajuste(nombre) ON UPDATE CASCADE ON DELETE CASCADE)";
        st.execute(tablaPatronAjuste);
        
        String tablaAnalisisOLD = "CREATE TABLE AnalisisOLD (numMuestra INT, "
                                                          + "identificacion VARCHAR(32), "
                                                          + "fecha DATE NOT NULL, "
                                                          + "resultado DECIMAL(5,4) NOT NULL, "
                                                          + "calibrado VARCHAR(32), "
                                                          + "cuentas INT NOT NULL, " 
                                                          + "PRIMARY KEY (numMuestra,identificacion,calibrado))";                                                                
        st.execute(tablaAnalisisOLD);      
        
        
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
        st.execute(triggerUpdCal);
        
        // Al borrar un calibrado, esto provoca que en la tabla Analisis se borren los registros que usan ese calibrado.
        // Tras borrar un registro de Analisis, vamos a copiar los datos a una tabla llamada AnalisisOLD en la que el campo Calibrado
        // no va a estar referenciado a Calibraciones.nombre, ya que estos calibrados ya no existen y daría error de integridad        
        String triggerdelAna =   "CREATE TRIGGER delAna " +
                                  "AFTER DELETE ON Analisis " +
                                  "REFERENCING OLD ROW AS oldrow FOR EACH ROW " +                                  
                                  "BEGIN ATOMIC " +                              
                                  "INSERT INTO AnalisisOLD VALUES (oldrow.numMuestra, oldrow.identificacion, oldrow.fecha, oldrow.resultado, oldrow.calibrado+'_BORRADO', oldrow.cuentas); " +
                                  "END";                                    
        st.execute(triggerdelAna);
        
        //CREAMOS AJUSTES POR DEFECTO
        st.execute("INSERT INTO Ajuste VALUES ('LZMET048',0,0,1,1,200)"); //los 3 primeros no estan activados en el menú de analisis, luego 0,0 en los valores de pagina y menu de analisis
        st.execute("INSERT INTO Ajuste VALUES ('LZMET049',0,0,2,1,200)");
        st.execute("INSERT INTO Ajuste VALUES ('LZMET050',0,0,3,1,100)");
        st.execute("INSERT INTO Ajuste VALUES ('AZUFRE BAJO',1,1,1,2,20)");
        st.execute("INSERT INTO Ajuste VALUES ('AZUFRE MEDIO',1,2,2,2,15)");
        st.execute("INSERT INTO Ajuste VALUES ('AZUFRE ALTO',2,1,3,2,10)");
        
        //CREAMOS PATRONES DE PRUEBA
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 0%','2023-11-26',0.0002)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 250ppm','2023-11-26',0.0255)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 500ppm','2023-11-26',0.0498)");        
        
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 0.1%','2023-11-27',0.1003)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 0.25%','2023-11-27',0.2491)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 0.5%','2023-11-27',0.5007)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 0.75%','2023-11-27',0.7491)");
        
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 1%','2023-11-28',1.0001)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 2%','2023-11-28',2.0006)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 3%','2023-11-28',2.9998)");
        st.execute("INSERT INTO Patron VALUES ('Patr\u00f3n 5%','2023-11-28',5.0006)");
        
        //CREAMOS LOS CALIBRADOS DE PRUEBA
        st.execute("INSERT INTO CALIBRACION VALUES('AZUFRE BAJO','2023-11-26',FALSE,'AZUFRE BAJO','Seleccionar...')");
        st.execute("INSERT INTO CALIBRACION VALUES('AZUFRE MEDIO','2023-11-27',TRUE,'AZUFRE MEDIO','Lineal')");
        st.execute("INSERT INTO CALIBRACION VALUES('AZUFRE ALTO','2023-11-28',TRUE,'AZUFRE ALTO','Cuadr\u00e1tica')");
        
        //Y LOS DATOS DE LOS PATRONES QUE TIENE ASIGNADA CADA CALIBRACION
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE BAJO','Patr\u00f3n 0%')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE BAJO','Patr\u00f3n 250ppm')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE BAJO','Patr\u00f3n 500ppm')");
        
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE MEDIO','Patr\u00f3n 0.1%')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE MEDIO','Patr\u00f3n 0.25%')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE MEDIO','Patr\u00f3n 0.5%')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE MEDIO','Patr\u00f3n 0.75%')");
        
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE ALTO','Patr\u00f3n 1%')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE ALTO','Patr\u00f3n 2%')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE ALTO','Patr\u00f3n 3%')");
        st.execute("INSERT INTO CALIBRACIONPATRON VALUES('AZUFRE ALTO','Patr\u00f3n 5%')");

        //Y LAS CUENTAS PARA CADA PATRON BAJO ESE AJUSTE DE MEDIDA
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 0%','AZUFRE BAJO',2750)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 250ppm','AZUFRE BAJO',3040)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 500ppm','AZUFRE BAJO',3305)");
        
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 0.1%','AZUFRE MEDIO',4300)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 0.25%','AZUFRE MEDIO',6152)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 0.5%','AZUFRE MEDIO',9100)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 0.75%','AZUFRE MEDIO',11850)");
        
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 1%','AZUFRE ALTO',14112)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 2%','AZUFRE ALTO',19325)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 3%','AZUFRE ALTO',24401)");
        st.execute("INSERT INTO PATRONAJUSTE VALUES('Patr\u00f3n 5%','AZUFRE ALTO',34085)");
        
        // Puerto y Config por defecto
        st.execute("INSERT INTO Configuracion VALUES ('COM1','2400','8','Ninguna','1',500,550,10,750,5,10)");
               
        detenerConexion();
    }
}
