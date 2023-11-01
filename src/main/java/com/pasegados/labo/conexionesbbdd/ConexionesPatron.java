package com.pasegados.labo.conexionesbbdd;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

import com.pasegados.labo.modelos.Patron;


/**
 * Esta clase permite crear conexiones a la tabla "Patron" de la BBDD
 * 
 * @author Pedro Antonio Segado Solano
 */
public class ConexionesPatron extends Conexion implements Cloneable {
    
    private Statement st;
    private ResultSet res;
    private PreparedStatement ps;
    private static final ConexionesPatron INSTANCIA_PATRON = new ConexionesPatron(); // Asignación constante Singleton

    /**
     * Contructor de objetos por defecto, para manejar la conexión a la BBDD relativa a Patrones
     */
    private ConexionesPatron() {
        super();        
    }
    
    // Singleton - Método estático Singleton
    public static ConexionesPatron getINSTANCIA_PATRON() {
        return INSTANCIA_PATRON;        
    }
    
    // Singleton - Para evitar la clonación del objeto
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
 
    /**
     * Este metodo prepara una lista con todos los Patrones almacenados en la BBDD
     *
     * @return ArrayList con los Patrones existentes
     * @throws SQLException
     */
    public ArrayList<Patron> cargarPatrones() throws SQLException {

        ArrayList<Patron> listaPatrones = new ArrayList<>();

        st = iniciarConexion().createStatement();
        res = st.executeQuery("Select * from Patron");

        while (res.next()) { // Tendremos varios patrones
            //Si no se guardó fecha del patron, el atributo sera null, sino haremos conversion
            //a LocaDate desde tipo Date de SQL
            LocalDate fecha = (res.getDate("fecha") == null) ? null : res.getDate("fecha").toLocalDate();
            Patron p = new Patron(res.getString("nombre"), fecha, res.getFloat("concentracion"));
            listaPatrones.add(p);
        }
        // Cerramos los objetos implicados
        res.close();
        st.close();
        detenerConexion();

        return listaPatrones;
    }

    /**
     * Este método permite insertar un nuevo registro en la tabla "Patron" en la BBDD
     * 
     * @param p Patron con los atributos tal y como deseamos introducir
     * @throws SQLException
     */
    public void insertar(Patron p) throws SQLException {

        // Si no hemos asignado fecha al patron, el Date de SQL será null. 
        // En caso contrario, hacemos conversión a Date SQL desde el LocalDate del patron
        Date fecha = (p.getFecha() == null) ? null : Date.valueOf(p.getFecha());

        ps = iniciarConexion().prepareStatement("INSERT INTO Patron (nombre, fecha, concentracion) VALUES (?,?,?);");
        ps.setString(1, p.getNombre());
        ps.setDate(2, fecha);
        ps.setFloat(3, p.getConcentracion());
        ps.executeUpdate();
        // Cerramos objetos implicados
        ps.close();
        detenerConexion();
    }

    /**
     * Este método permite actualizar un registro de la tabla "Patrones" de la BBDD en el cual el 
     * nombre (PK) coincida con el recibido por parámetro
     * 
     * @param p Patron con los atributos tal y como deseamos actualizar
     * @param nombreOriginal String con el nombre original del patron (por si hemos cambiado el nombre)
     * @throws SQLException
     */
    public void actualizarPatron(Patron p, String nombreOriginal) throws SQLException {
        
        // Preparamos objeto Date SQL a partir del atributo fecha de tipo LocalDate
        Date fecha = (p.getFecha() == null) ? null : Date.valueOf(p.getFecha());

        ps = iniciarConexion().prepareStatement("Update Patron set nombre = ?, fecha = ?, concentracion = ?   where nombre = '" + nombreOriginal + "';");
        ps.setString(1, p.getNombre());
        ps.setDate(2, fecha);
        ps.setFloat(3, p.getConcentracion());

        ps.executeUpdate();
        // Cerramos objetos implicados
        ps.close();
        detenerConexion();
    }

    /**
     * Esté método permite eliminar un registro de la tabla "Patrones" de la BBDD en el cual 
     * coindica el nombre (PK) con el indicado como parámetro
     * 
     * @param nombre String con el nomnbre del objeto Patron a eliminar
     * @throws SQLException
     */
    public void eliminarPatron(String nombre) throws SQLException {
                
        st = iniciarConexion().createStatement();
        // Al actuar con la PK solo se va a borrar 1 registro
        st.executeUpdate("Delete from Patron where nombre =  '" + nombre + "';");
        // Cerramos los objetos implicados
        st.close();
        detenerConexion();
    }
}
