package com.pasegados.labo.conexionesbbdd;

import java.sql.*;
import java.util.ArrayList;

import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Configuracion;
import com.pasegados.labo.modelos.Puerto;


/**
 * Esta clase permite crear conexiones a la tabla "Configuracion" de la BBDD
 *
 * @author Pedro A. Segado Solano
 */
public class ConexionesConfiguracion extends Conexion implements Cloneable {

    private Statement st;
    private ResultSet resultados;
    private PreparedStatement ps;
    private static final ConexionesConfiguracion INSTANCIA_CONFIGURACION = new ConexionesConfiguracion(); // Asignación constante Singleton

    /**
     * Contructor de objetos por defecto, para manejar la conexión a la BBDD relativa a la
     * Configuración
     */
    private ConexionesConfiguracion() {
        super();
    }
    
    // Singleton - Método estático Singleton
    public static ConexionesConfiguracion getINSTANCIA_CONFIGURACION () {
        return INSTANCIA_CONFIGURACION;        
    }
    
    // Singleton - Para evitar la clonación del objeto
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
   
    // TABLA CONFIGURACION
    /**
     * Este método carga los datos relativos al Puerto COM, mostrándolos en la pestaña
     * "Configuracion"
     *
     * @return Puerto con la configuración necesaria para enviar y recibir datos
     * @throws SQLException
     */
    public Puerto cargarDatosPuerto() throws SQLException {

        st = iniciarConexion().createStatement();
        resultados = st.executeQuery("Select * from Configuracion");
        Puerto p = new Puerto();

        if (resultados.next()) { // Solo hay un registro con toda la configuracion
            p = new Puerto(resultados.getString("puerto"), resultados.getInt("bps"), resultados.getInt("bdd"),
                    resultados.getString("par"), resultados.getString("bdp"));

        }
        // Cerramos los objetos implicados
        resultados.close();
        st.close();
        detenerConexion();

        return p;
    }

    /**
     * Este método carga los datos de configuración auxiliar del programa, que se muestran en la
     * pestaña "Configuracion" *
     *
     * @return Configuracion con los parámetros necesarios para el correcto funcionamiento
     * @throws SQLException
     */
    public Configuracion cargarDatosConfiguracion() throws SQLException {
        
        st = iniciarConexion().createStatement();
        resultados = st.executeQuery("Select * from Configuracion");
        Configuracion config = new Configuracion();

        if (resultados.next()) { // Solo vamos a tener un registro
            config = new Configuracion(resultados.getInt("pulsaciones"), resultados.getInt("preacondicionamiento"),
                    resultados.getInt("acondicionamiento"), resultados.getInt("preenergia"),
                    resultados.getInt("energia"), resultados.getInt("premedida"));
        }
        // Cerramos los objetos implicados
        resultados.close();
        st.close();
        detenerConexion();

        return config;
    }

    /**
     * Este método permite actualizar la parte relativa al Puerto incluida en la tabla
     * "Configuracion" de nuestra BBDD
     *
     * @param p Puerto con los atributos establecidos tal y como queremos guardarlos
     * @throws SQLException
     */
    public void actualizarDatosPuerto(Puerto p, String puertoViejo) throws SQLException {

        
        // Como hay un solo registro no uso WHERE, y hago UPDATE solo de los campos del puerto
        ps = iniciarConexion().prepareStatement("UPDATE Configuracion set puerto = ? , bps = ? , bdd = ? , par = ? , bdp = ? WHERE puerto = '" + puertoViejo + "';");

        ps.setString(1, p.getNombrePuerto());
        ps.setInt(2, p.getBps());
        ps.setInt(3, p.getBdd());
        ps.setString(4, p.getParidad());
        ps.setString(5, p.getBdp());

        ps.executeUpdate();
        // Cerramos los objetos implicados
        ps.close();
        detenerConexion();
    }

    /**
     * Este método permite actualizar la parte relativa a la Configuracion general del programa
     * incluida en la tabla "Configuracion" de nuestra BBDD
     *
     * @param c Configuracion con los atributos establecidos tal y como queremos guardarlos
     * @throws SQLException
     */
    public void actualizarDatosConfig(Configuracion c, String puertoViejo) throws SQLException {
        
        // Como hay un solo registro no uso WHERE, y hago UPDATE solo de los campos de configuracion
        ps = iniciarConexion().prepareStatement("UPDATE Configuracion set pulsaciones = ? , preacondicionamiento = ? ,"
                + " acondicionamiento = ? , preenergia = ? , energia = ? , premedida = ? WHERE puerto = '" + puertoViejo + "';");

        ps.setInt(1, c.getPulsaciones());
        ps.setInt(2, c.getPreAcondicionamiento());
        ps.setInt(3, c.getAcondicionamiento());
        ps.setInt(4, c.getPreEnergia());
        ps.setInt(5, c.getEnergia());
        ps.setInt(6, c.getPreMedida());

        ps.executeUpdate();
        // Cerramos los objetos implicados
        ps.close();
        detenerConexion();
    }

    // TABLA AJUSTES
    /**
     * Este metodo prepara una lista con todos los Ajustes almacenados en la BBDD
     *
     * @return ArrayList con los Ajustes existentes
     * @throws SQLException
     */
    public ArrayList<Ajuste> cargarAjustes() throws SQLException {

        ArrayList<Ajuste> listaAjustes = new ArrayList<>();

        st = iniciarConexion().createStatement();
        resultados = st.executeQuery("Select * from Ajuste");
        
        while (resultados.next()) { // Podemos tener varios ajustes, por defecto existen 3
            Ajuste a = new Ajuste(resultados.getString("nombre"), resultados.getInt("analisisPagina"),
                    resultados.getInt("analisisMenu"), resultados.getInt("calibracionPagina"), 
                    resultados.getInt("calibracionMenu"),resultados.getInt("tiempo"));
            listaAjustes.add(a); // Voy añadiendo objetos Ajuste en las iteraciones            
        }
          
        // Cerramos los objetos implicados
        resultados.close();
        st.close();
        detenerConexion();

        return listaAjustes;
    }

    /**
     * Este método recibe un objeto Ajuste y almacena sus características en la BBDD
     *
     * @param a Ajuste con los atributos deseados
     * @throws SQLException
     */
    public void insertarAjuste(Ajuste a) throws SQLException {

        ps = iniciarConexion().prepareStatement("INSERT INTO Ajuste (nombre, analisisPagina, analisisMenu,"
                + " calibracionPagina, calibracionMenu, tiempo) VALUES (?,?,?,?,?,?);");
        ps.setString(1, a.getNombre());
        ps.setInt(2, a.getAnalisisPagina());
        ps.setInt(3, a.getAnalisisMenu());
        ps.setInt(4, a.getCalibracionPagina());
        ps.setInt(5, a.getCalibracionMenu());
        ps.setInt(6, a.getDuracion());          

        ps.executeUpdate();
        // Cierro los objetos implicados
        ps.close();
        detenerConexion();
    }

    /**
     * Este método actualiza un registro de la tabla "Ajuste", coincidente con el nombre recibido
     * por ser esta la PK
     *
     * @param a Ajuste con los atributos tal y como deseamos actualizarlos
     * @param nombreOriginal String con el nombre original del Ajuste (por si lo renombramos)
     * @throws SQLException
     */
    public void actualizarAjuste(Ajuste a, String nombreOriginal) throws SQLException {

        ps = iniciarConexion().prepareStatement("Update Ajuste set nombre = ?, analisisPagina = ?,"
                + "analisisMenu = ?, calibracionPagina = ?, calibracionMenu = ?, tiempo = ?   where nombre = '" + nombreOriginal + "';");
        ps.setString(1, a.getNombre());
        ps.setInt(2, a.getAnalisisPagina());
        ps.setInt(3, a.getAnalisisMenu());
        ps.setInt(4, a.getCalibracionPagina());
        ps.setInt(5, a.getCalibracionMenu());
        ps.setInt(6, a.getDuracion());
        // Realizamos el update tal y como lo hemos configurado anterioremente
        ps.executeUpdate();
        // Cerramos los objetos implicados
        ps.close();
        detenerConexion();
    }

    /**
     * Este método permite eliminar un registro de la tabla "Ajuste" que coincide con el nombre
     * recibido como parámetro por ser esta la PK
     *
     * @param nombreOriginal String con el nombre original del Ajuste (por si lo renombramos)
     * @throws SQLException
     */
    public void eliminarAjuste(String nombreOriginal) throws SQLException {
                
        st = iniciarConexion().createStatement();
        // Realizamos el delete del registro cuya PK sea el nombre recibido
        st.executeUpdate("Delete from Ajuste where nombre =  '" + nombreOriginal + "';");
        // Cerramos los objetos implicados
        st.close();
        detenerConexion();
    }    
}
