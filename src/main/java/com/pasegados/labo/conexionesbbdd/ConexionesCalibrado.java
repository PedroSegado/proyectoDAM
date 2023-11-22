package com.pasegados.labo.conexionesbbdd;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import com.pasegados.labo.App;
import com.pasegados.labo.modelos.Ajuste;
import com.pasegados.labo.modelos.Calibrado;
import com.pasegados.labo.modelos.Patron;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


/**
 * Esta clase permite crear conexiones a la tabla "Calibrado" de la BBDD
 *
 * @author Pedro Antonio Segado Solano
 */
public class ConexionesCalibrado extends Conexion implements Cloneable {
        
    private Statement st;
    private ResultSet resultados1, resultados2;
    private PreparedStatement ps;
    private static final ConexionesCalibrado INSTANCIA_CALIBRADO = new ConexionesCalibrado(); // Asignación constante Singleton

    // Singleton - Constructor privado para no poder crear instancias
    private ConexionesCalibrado() {   
        super();
    }
    
    // Singleton - Método estático Singleton
    public static ConexionesCalibrado getINSTANCIA_CALIBRADO () {
        return INSTANCIA_CALIBRADO;        
    }
    
    // Singleton - Para evitar la clonación del objeto
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    // TABLA CALIBRADOS
    
    /**
     * Esta metodo prepara una lista con todos los Calibrados almacenados en la BBDD
     *
     * @return ArrayList de objetos Calibrado
     * @throws SQLException
     */
    public ArrayList<Calibrado> cargarCalibrados() throws SQLException {

        ArrayList<Calibrado> listaCalibrados = new ArrayList<>();
        

        st = iniciarConexion().createStatement();
        resultados1 = st.executeQuery("Select * from Calibracion");

        while (resultados1.next()) {

            String calibrado = resultados1.getString("nombre");
            LocalDate fecha = (resultados1.getDate("fecha") == null) ? null : resultados1.getDate("fecha").toLocalDate();            
            boolean activo = resultados1.getBoolean("activo");
            String ajuste = resultados1.getString("ajuste"); // Hemos guardado solo el nombre del objeto Ajuste
            String tipoRegresion = resultados1.getString("tipoRegresion");

            Ajuste aj = null; // Vamos a buscar en la lista de objetos Ajuste, cual tiene el nombre del usado en este Calibrado
            for (Ajuste a : App.getControladorPrincipal().getListaAjustes()) {
                if (a.getNombre().equals(ajuste)) {
                    aj = a;
                }
            }

            // Ahora vamos a crear la lista de Patrones del Calibrado, registrada en la tabla "CalibradoPatron"
            resultados2 = st.executeQuery("Select * from CalibracionPatron "
                                         + "WHERE nombreCalibrado = '" + calibrado + "'");

            ObservableList<Patron> listaPat = FXCollections.observableArrayList();

            // Mientras tengamos resultados, tenemos patrones para agregar
            while (resultados2.next()) {                
                String nombrePatron = resultados2.getString("nombrePatron");
                
                //Uso la lista de patrones, para leer de ella los patrones que se usan    
                App.getControladorPrincipal().getListaPatrones().forEach(p -> {
                    if (p.getNombre().equals(nombrePatron)) {                        
                        listaPat.add(p);
                    }
                });
            }
            resultados2.close();

            // Contruyo mi objeto Calibrado y lo añado a mi ArrayList
            Calibrado c = new Calibrado(calibrado, fecha, aj, activo, listaPat, tipoRegresion);
            listaCalibrados.add(c);
        }

        resultados1.close(); // Cerramos busquedas y conexiones
        st.close();
        detenerConexion();
        
        //Una vez terminada la conexion, podemos actualizar los coeficientes, ya que para ello usa la conexión nuevamente
        // y si lo hacemos antes, cierra la conexión y solo hace una iteracion del resulset1
        for (Calibrado c:listaCalibrados){
            c.ecuacionProperty().get().calculaCoeficientes(c.getListaPatrones(),c.getAjuste().getNombre(),c.getTipoRegresion());
        }

        return listaCalibrados;
    }

    /**
     * Este método permite insertar un nuevo registro de tipo Calibrado en la BBDD
     *
     * @param c Calibrado a insertar 
     * @throws SQLException
     */
    public void insertarCalibrado(Calibrado c) throws SQLException {

        ps = iniciarConexion().prepareStatement("INSERT INTO Calibracion (nombre, fecha, activo, ajuste, tipoRegresion) "
                                 + "VALUES (?,?,?,?,?);");
        // Preparo cada campo del registro por su posición en el INSERT declarado anteriormente
        ps.setString(1, c.getNombre());
        ps.setDate(2, (c.getFecha()!=null)?Date.valueOf(c.getFecha()):null);
        ps.setBoolean(3, c.isActivo());
        ps.setString(4, c.getAjuste().getNombre()); // Del objeto ajuste solo guardo el nombre, por ser PK, y aquí FK
        ps.setString(5, c.getTipoRegresion());
        // Ejecutamos el INSERT y cerramos las conexiones de todos los objetos involucrados en el proceso
        ps.executeUpdate();
        ps.close();
        detenerConexion();
    }

    /**
     * Este método permite actualizar un registro relativo a un Calibrado de la BBDD
     *
     * @param c Calibrado sobre el se va a actualizar
     * @param nombre String con el nombre del Calibrado a actualizar (PK). Si se le actualizase el
     * nombre, este sería el nombre antiguo
     * @throws SQLException
     */
    public void actualizarCalibrado(Calibrado c, String nombre) throws SQLException {

        ps = iniciarConexion().prepareStatement("Update Calibracion set nombre = ? , Fecha = ?, activo = ?,"
                                + " ajuste = ? , tipoRegresion = ? where nombre = '" + nombre + "';");
        // Preparo cada campo del UPDATE, y uso el parametro recibido como nombre para hacer el WHERE que limita a un solo registro, ya que el nombre es el PK
        ps.setString(1, c.getNombre());
        ps.setDate(2, (c.getFecha()!=null)?Date.valueOf(c.getFecha()):null);
        ps.setBoolean(3, c.isActivo());
        ps.setString(4, c.getAjuste().getNombre()); // Del objeto ajuste solo guardamos su nombre, por ser el PK de la tabla de estos objeots, y aqui FK
        ps.setString(5, c.getTipoRegresion());
        // Ejecutamos el UPDATE y cerramos todas las conexiones de todos los objetos involucrados en el proceso
        ps.executeUpdate();
        ps.close();
        detenerConexion();
    }

    /**
     * Este método permite eliminar un registro de tipo Calibrado de la BBDD
     *
     * @param nombre String con el nombre del Calibrado, por ser esta la PK
     * @throws SQLException
     */
    public void eliminarCalibrado(String nombre) throws SQLException {
        
        st = iniciarConexion().createStatement();

        st.executeUpdate("Delete from Calibracion where nombre =  '" + nombre + "';");
        // Tras lanzar el DELETE cerramos las conexiones de los objetos involucrados en el proceso
        st.close();
        detenerConexion();
    }

    // TABLA PATRONAJUSTE (cuentas que corresponden a un patrón analizado bajo un ajuste determinado)
    
    /**
     * Este método permite saber las cuentas de lectura que corresponden a un Patrón analizado bajo un
     * Ajuste determinado
     *
     * @param patron String con el nombre del objeto Patrón, por ser la PK de estos, y aqui FK
     * @param ajuste String con el nombre del objeto Ajuste, por ser la PK de estos, y aquí FK
     * @return int con las cuentas correspondientes
     * @throws SQLException
     */
    public int cargarCuentasPatron(String patron, String ajuste) throws SQLException {

        int cuentas = 0;

        st = iniciarConexion().createStatement();
        resultados1 = st.executeQuery("Select cuentas from PatronAjuste where nombrePatron = '" + patron 
                                  + "' AND nombreAjuste ='" + ajuste + "';");
        if (resultados1.next()) { // Habrá un solo resultado
            cuentas = resultados1.getInt("cuentas");
        }
        
        st.close();
        detenerConexion();
        return cuentas;
    }

    /**
     * Este método permite actualizar las cuentas de lectura correspondientes a un Patrón analizado
     * en un Ajuste determinado
     *
     * @param patron String con el nombre del Patron, por ser la PK de este
     * @param ajuste String con el nombre del Ajuste, por ser la PK de este
     * @param cuentas int con el valor de las cuentas a actualizar
     * @throws SQLException
     */
    public void actualizarCuentasPatron(String patron, String ajuste, int cuentas) throws SQLException {

        ps = iniciarConexion().prepareStatement("Update PatronAjuste set cuentas = ? where nombrePatron = '" + patron 
                               + "' AND nombreAjuste ='" + ajuste + "';");
        ps.setInt(1, cuentas);

        int actualizados = ps.executeUpdate();
        ps.close();
        detenerConexion();

        if (actualizados == 0) { // Si no hay resgistros actualizados es que no existe luego tenemos que hace un INSERT        
            insertarCuentasPatron(patron, ajuste, cuentas);
        }
    }

    // Este método es auxiliar al anterior, y se encarga de realizar el INSERT si el UPDATE no ha devuelto registros afectados
    // ya que sería por tanto un nuevo registro
    private void insertarCuentasPatron(String patron, String ajuste, int cuentas) throws SQLException {

        ps = iniciarConexion().prepareStatement("INSERT INTO PatronAjuste (nombrePatron, nombreAjuste, cuentas) VALUES (?,?,?);");
        ps.setString(1, patron);
        ps.setString(2, ajuste);
        ps.setInt(3, cuentas);

        ps.executeUpdate();
        ps.close();
        detenerConexion();
    }

    // TABLA CALIBRADOPATRON (Patrones que incluye un Calibrado)
    /**
     * Este método permite insertar un Patrón asociado a un Calibrado, usando los nombres de ambos,
     * por ser PK para ellos
     *
     * @param calibrado String con el nombre del Calibrado
     * @param patron String con el nombre del Patron
     */
    public void insertarListaPatronesCalibrado(String calibrado, String patron) throws SQLException {

        ps = iniciarConexion().prepareStatement("INSERT INTO CalibracionPatron (nombreCalibrado, nombrePatron) VALUES (?,?);");
        ps.setString(1, calibrado);
        ps.setString(2, patron);

        ps.executeUpdate();
        ps.close();
        detenerConexion();
    }

    /**
     * Este método permite eliminar un Patron que está asociado a un Calibrado, usando los nombres
     * de ambos, por ser PK para ellos
     *
     * @param calibrado String con el nombre del Calibrado
     * @param patron String con el nombre del Patron
     */
    public void eliminarListaPatronesCalibrado(String calibrado, String patron) throws SQLException {

        st = iniciarConexion().createStatement();

        st.executeUpdate("DELETE FROM CalibracionPatron WHERE nombreCalibrado = '" + calibrado + "' "
                       + "AND nombrePatron = '" + patron + "';");
        st.close();
        detenerConexion();
    }
}
