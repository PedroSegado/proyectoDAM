package com.pasegados.labo.conexionesbbdd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.pasegados.labo.modelos.Analisis;


/**
 * Esta clase permite crear conexiones a la tabla "Analisis" de la BBDD
 * 
 * @author Pedro A. Segado Solano
 */
public class ConexionesResultados extends Conexion implements Cloneable {
  
    private Statement st;
    private ResultSet res;
    private PreparedStatement ps;
    private static final ConexionesResultados INSTANCIA_RESULTADOS = new ConexionesResultados(); // Asignación constante Singleton

    /**
     * Contructor de objetos por defecto, para manejar la conexión a la BBDD relativa a Resultados
     */
    private ConexionesResultados() {
        super();
    }
    
    // Singleton - Método estático Singleton
    public static ConexionesResultados getINSTANCIA_RESULTADOS () {
        return INSTANCIA_RESULTADOS;        
    }
    
    // Singleton - Para evitar la clonación del objeto
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Este metodo prepara una lista con los 25 últimos registros almacenados en la tabla "Analisis"
     * 
     * @return ArrayList de objetos Analisis con los úlitmos 25 registros insertados
     * @throws SQLException
     */
    public ArrayList<Analisis> cargarResultados() throws SQLException {

        ArrayList<Analisis> listaAnalisis = new ArrayList<>();
        
        // Creamos nuestra declaración y ejecutamos la busqueda
        st = iniciarConexion().createStatement();
        res = st.executeQuery("Select * from Analisis ORDER BY fecha DESC LIMIT 0, 25;");

        while (res.next()) { // Tendré un máximo de 25 iteraciones            
                Analisis a = new Analisis(res.getInt("numMuestra"), res.getString("identificacion"), res.getDate("fecha"), res.getFloat("resultado"), res.getString("calibrado"), res.getInt("cuentas"));
                listaAnalisis.add(a);           
        }
        // Cerramos los objetos implicados
        res.close();
        st.close();
        detenerConexion();

        return listaAnalisis;
    }

    /**
     * Inserta un nuevo registro en la tabla "Analisis"
     * 
     * @param a Analisis con los atributos definidos tal y como deseamos insertar
     * @throws SQLException
     */
    public void insertar(Analisis a) throws SQLException {
        
        // Preparamos la declaración y ejecutamos el insert
        ps = iniciarConexion().prepareStatement("INSERT INTO Analisis (numMuestra, identificacion, fecha, resultado, calibrado, cuentas) VALUES (?,?,?,?,?,?);");
        ps.setInt(1, a.getMuestra());
        ps.setString(2, a.getIdentificacion());
        ps.setDate(3, a.getFecha());
        ps.setFloat(4, a.getResultado());
        ps.setString(5, a.getCalibrado());
        ps.setInt(6, a.getCuentas());
        ps.executeUpdate();
        // Cerramos los objetos implicados
        ps.close();
        detenerConexion();
    }

    /**
     * Este método prepara una lista de Analisis que cumplen una/s condicion/es realizando una 
     * busqueda entre todos los registros existentes en la tabla "Analisis"
     * 
     * @param busqueda String con la declaración que limita nuestra busqueda
     * @return ArrayList de objetos Analisis que cumplen las condiciones indicadas por parámetro
     * @throws SQLException
     */
    public ArrayList<Analisis> buscarResultados(String busqueda) throws SQLException {
        ArrayList<Analisis> listaAnalisis = new ArrayList<>();
        
        // Creamos nuestra declaración y ejecutamos la busqueda
        st = iniciarConexion().createStatement();
        
        //Si buscamos calibrados ELIMINADOS, la tabla será AnalisisOLD:
        if (busqueda.contains("ELIMINADOS")){            
            busqueda = busqueda.replace("= 'ELIMINADOS'","LIKE '%_BORRADO'"); // modificamos la busqueda para que saque todos los calibrados eliminados                
            res = st.executeQuery("Select * from AnalisisOLD " + busqueda + " ORDER BY fecha DESC");
        } else {
            res = st.executeQuery("Select * from Analisis " + busqueda + " ORDER BY fecha DESC");
        }
        
        while (res.next()) { // Tendremos 0, 1 o varias iteraciones            
                Analisis a = new Analisis(res.getInt("numMuestra"), res.getString("identificacion"), res.getDate("fecha"), res.getFloat("resultado"), res.getString("calibrado"), res.getInt("cuentas"));
                listaAnalisis.add(a);            
        }
        // Cerramos los objetos implicados
        res.close();
        st.close();
        detenerConexion();

        return listaAnalisis;
    }
    
    /**
     * Este método comprueba si existe en la tabla "Analisis" algún registro en el que el nº de
     * muestra e identificacion (pareja PK) coinciden con los pasados por parámetros
     * 
     * @param numMuestra int con los 9 dígitos el numero de muestra
     * @param identificacion String con la identificación de la muestra
     * @return boolean true cuando existe el registro o false si no existe
     * @throws SQLException
     */
    public boolean existePK(int numMuestra, String identificacion, String calibrado) throws SQLException {
        
        // Creamos nuestra declaración y ejecutamos la busqueda
        st = iniciarConexion().createStatement();
        res = st.executeQuery("Select * from Analisis WHERE numMuestra=" + numMuestra + " AND identificacion='" + identificacion + "'" +
                              " AND calibrado='" + calibrado + "';"); // ` por ser varias palabras 

        if (res.next()) { // Si existe algun resultado es que la PK ya existe
            return true;
        }
        // Cerramos los objetos implicados
        res.close();
        st.close();
        detenerConexion();

        return false; // Si llegamos aquí, no existen resultado, luego esa PK no existe
    }
    
    /**
     * Este método elimina el contenido de la tablas Analisis y AnalisisOLD de la BBDD.
     */
    public void eliminaAnalisis() throws SQLException {        
        // Creamos nuestra declaración y ejecutamos la busqueda
        st = iniciarConexion().createStatement();
        res = st.executeQuery("TRUNCATE TABLE Analisis;"); 
        res = st.executeQuery("TRUNCATE TABLE AnalisisOLD;"); 
        res.close();
        st.close();
        detenerConexion(); 
    }
    
}
