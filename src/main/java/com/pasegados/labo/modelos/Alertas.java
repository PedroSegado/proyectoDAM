package com.pasegados.labo.modelos;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

/**
 * Esta clase abstracta va a controlar todas las pantallas de "Alerta" que se puedan 
 * generar en el uso del programa
 * 
 * @author Pedro A. Segado Solano
 */
public abstract class Alertas {
    
    // Avisamos al usuario de fallo al recibir datos al analizar muestra/patron
    public static void alertaRecibirDatos() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error en la recepción de datos por el puerto COM");
        alerta.setContentText("Un fallo ha impedido recibir los datos del análisis." + "\n" + "Análisis abortado");
        alerta.showAndWait();
    }
    
    
    
    
    

    public static boolean alertaCrearNuevaBBDD() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Crear nueva BBDD");
        alert.setHeaderText("Es necesario crear una nueva 'Base de Datos'");
        alert.setContentText("Por ser la primera ejecución o por algún tipo de problema no existe la 'Base de Datos'." +
                "\n\n" + "¿Desea crearla para empezar a trabajar?" + "\n\n");
         Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    // previa al borrado, solicita hacer copia
    public static boolean alertaPreviaCrearCopia() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Borrar Base de Datos");
        alert.setHeaderText("¿Realizar copia de seguridad antes de eliminar la 'Base de Datos'?");
        alert.setContentText("Es conveniente realizar una copia por si hay algún problema poder revertir la situación al estado anterior." +
                "\n\n" + "¿Desea crear una copia de seguridad?" + "\n\n");
         Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    // previa al crear copia de seguridad
    public static boolean alertaCrearCopia() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Copia de seguridad de la 'Base de Datos'");
        alert.setHeaderText("¿Deseá realizar una copia de seguridad del estado actual de la 'Base de Datos'?");
        alert.setContentText("La copia se guardará en la carpeta 'copiaSeguridad', en un directorio con la fecha actual en formato 'AAMMDD'." +
                "\n\n" + "¿Desea crear una copia de seguridad?"  + "\n\n");
         Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
     // confirmacion borrado BBDD
    public static boolean alertaBorradoBBDD(boolean copia, boolean exitoCopia) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Borrar Base de Datos");
        alert.setHeaderText("¿Seguro que quiere eliminar la 'Base de Datos'?");
        if (copia & exitoCopia) {
            alert.setContentText("El estado de la 'Base de Datos' se podrá revertir gracias a la a copia de seguridad creada en el paso anterior." +
                "\n\n" + "¿Desea borrar por completo la 'Base de Datos'?" + "\n\n");
        } else if (copia & !exitoCopia) {
            alert.setContentText("El estado de la 'Base de Datos'  NO se podrá revertir porque ha fallado el proceso de creación de la copia de seguridad en el paso anterior." +
                "\n\n" + "¿Desea borrar por completo la 'Base de Datos'?" + "\n\n");
        } else {
            alert.setContentText("Este es un proceso irreversible ya que no se ha creado copia de seguridad en el paso anterior." +
                "\n\n" + "¿Desea borrar por completo la 'Base de Datos'?" + "\n\n");
        }        
        
        //Desactiva boton aceptar por defecto        
        Button aceptar = (Button) alert.getDialogPane().lookupButton( ButtonType.OK );
         aceptar.setDefaultButton( false );

        //Establece boton cancelar por defecto
        Button cancelar = (Button) alert.getDialogPane().lookupButton( ButtonType.CANCEL );
        cancelar.setDefaultButton( true );        
        
         Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    
    
    
    public static void alertaBBDDCreada() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Información BBDD");
        alerta.setHeaderText("Información BBDD");        
        alerta.setContentText("La 'Base de Datos' se ha creado con exito." + "\n\n" + "Pulse aceptar para iniciar la aplicación");
        alerta.showAndWait();
    }
    
    public static void alertaBBDDErrorCrear() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error al crear BBDD");    
        alerta.setHeaderText("Error al crear BBDD");    
        alerta.setContentText("La 'Base de Datos' NO se ha podido crear con exito." + "\n\n" + "Pulse aceptar para cerrar la aplicación");
        alerta.showAndWait();
    }
    
    
    

    /**
     * Error al inciar el programa     
     */
    public static void alertaCargaInicial(String error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error en inicio de la aplicación");
        alerta.setContentText("No se localiza algún FXML durante la carga de la aplicación" + "\n" + error);
        alerta.showAndWait();
    }
        
    
    /**
     * Error de acceso a la BBDD
     * @param error String que informa del tipo de error
     */
    public static void alertaBBDD(String error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error en la BBDD");
        alerta.setHeaderText("Error accediendo");
        alerta.setContentText(error);
        alerta.showAndWait();
    }
    
    
     /**
     * Error de acceso al Puerto     
     */
    public static void alertaPuerto() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error acceso Puerto COM");
        alerta.setHeaderText("Error accediendo al puerto de comunicaciones");
        alerta.setContentText("No se ha podido abrír el puerto para enviar los datos necesarios");
        alerta.showAndWait();
    }
    
    /**
     * Violación de la Clave Primaria en la tabla Análisis de la BBDD
     */
    public static void alertaPKAnalisis() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error por violación PK en la BBDD");
        alerta.setHeaderText("Ya existe este registro en la BBDD");
        alerta.setContentText("La \"Clave Primaria\" está compuesta por la combinación del número de muestra, "
                + "la identificación y el calibrado usado." 
                + "\n\n"                
                + "Cambie el valor de al menos uno de estos campos");
        alerta.showAndWait();
    }

    /**
     * Confirmación del usuario de que se puede eliminar un objeto (Patron, Calibrado o Ajuste) 
     * @param tipo String que indica si es un "Ajuste", "Patron" o "Calibrado"
     * @param valor String que indica el nombre del objeto a borrar, tal y como lo ve el usuario en la lista
     * @return boolean indicando true si se pulsa "OK" o false si se "CANCELA" o "CIERRA"
     */
    public static boolean alertaEliminaObjeto(String tipo, String valor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar " + tipo);
        alert.setContentText("¿Estás seguro de eliminar el " + tipo + " " + valor + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    /**
     * Confirmación del usuario de que se puede eliminar un objeto (Patron, Calibrado o Ajuste) 
     * @param tipo String que indica si es un "Ajuste", "Patron" o "Calibrado"
     * @param valor String que indica el nombre del objeto a borrar, tal y como lo ve el usuario en la lista
     * @return boolean indicando true si se pulsa "OK" o false si se "CANCELA" o "CIERRA"
     */
    public static boolean alertaEliminaObjeto(String tipo, String valor, String loUsan) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar " + tipo);
        alert.setContentText("¿Estás seguro de eliminar el " + tipo + " " + valor + "?" + "\n\n" + valor + " se usa en: " + loUsan);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    
    
    /**
    * Intentando borrar un ajuste que está en uso en algun calibrado
    */
    public static void alertaAjusteUsadoCalibrado(String valor) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Eliminar Ajuste usado en calibrado");
        alert.setHeaderText("El ajuste " + valor + " se usa en al menos un calibrado");
        alert.setContentText("Revisar los calibrados afectados y eliminarlos o cambiar su ajuste previamente al borrado de este ajuste" + "\n");
                          
        alert.showAndWait();
    }
    
    
    /**
    * Tras eliminar un patron, hay calibrados que no tienen suficientes para mantener su tipo de regresión
    */
    public static void alertaCalibradoFaltanPatrones(String nombrePatron, String nombreCalibrado) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Revisar Calibrado");
        alert.setHeaderText("El borrado del patrón " + nombrePatron + " afecta al calibrado " + nombreCalibrado);
        alert.setContentText("Revisar el calibrado " + nombreCalibrado + " y ajustar el tipo de regresión y/o el número de patrones asignados a la misma.");
                          
        alert.showAndWait();
    }
    
    
    /**
    * Generando un informe jasperreports
    */
    public static void alertaInforme(String tipo, String nombre, String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Informe");
        alert.setHeaderText("No se ha podido generar el informe de " + tipo + " " + nombre);
        alert.setContentText("Error: " + error + "\n");
                          
        alert.showAndWait();
    }
    
    
       /**
    * Generando un informe jasperreports
    */
    public static void alertaInformeNoSeleccionado() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Informe");
        alert.setHeaderText("Error de selección");
        alert.setContentText("No se ha seleccionado un calibrado del que emitir informe");
                          
        alert.showAndWait();
    }
    
    
    

    /**
     * Alerta indicando que no se ha seleccionado ningún patron para asignar a un Calibrado, 
     * tras pulsar el botón "Asignar" en el editor de calibrados
     */
    public static void alertaAsignaPatron() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error al asignar patrón");
        alerta.setContentText("No hay ningún patrón seleccionado en la lista de patrones disponibles");
        alerta.showAndWait();
    }

    /**
     * Alerta indicando que no se ha seleccionado ningún patron para eliminar del Calibrado, 
     * tras pulsar el botón de "Eliminar" en el editor de calibrados
     */
    public static void alertaQuitarPatron() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error al eliminar patrón del calibrado");
        alerta.setContentText("No hay ningún patrón seleccionado en la lista de patrones asignados al calibrado");
        alerta.showAndWait();
    }

    /**
     * Alerta para indicar que un "Patrón" no tiene cuentas para un "Ajuste" determinado, lo cual
     * indica que no ha sido analizado en esas condiciones, y por tanto no se puede asignar a un
     * calibrado que use ese ajuste, hasta que no se analice previamente.
     * 
     * @param patron String con el nombre del patron afectado
     * @param ajuste String con el nombre del ajuste afectado
     */
    public static void alertaPatronNoAnalizado(String patron, String ajuste) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error al asignar patrón");
        alerta.setHeaderText("Error asignando patrón al calibrado");
        alerta.setContentText(patron + " aún no ha sido analizado en el equipo usando el ajuste " + ajuste + "." + "\n \n"
                + "Para asignarlo a la calibración introduzcalo en el equipo y pulse el botón 'Analizar'");
        alerta.showAndWait();
    }

    /**
     * Alerta que indica que se debe seleccionar un solo Patron para analizar al pulsar el botón
     * de "Analisis" de patron.
     * @param error int que toma el valor 0 si no se ha seleccionado ningún patron o >1 si se han
     * seleccionado varios patrones
     */
    public static void alertaAnalisisPatronSeleccionado(int error) {
        String mostrar="";
        if (error==0) mostrar = "Se debe selecciónar un patrón de la lista de 'Patrones disponibles' para proceder al análisis de sus cuentas.";
        else mostrar = "Se han seleccionado varios patrones. Solo se debe selecciónar un patrón de la lista de 'Patrones disponibles' para proceder al análisis de sus cuentas.";
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error de selección");
        alerta.setHeaderText("Error en la selección del patrón a analizar");
        alerta.setContentText(mostrar);
        alerta.showAndWait();
    }
    
    /**
     * Alerta que indica que no se puede calcular el tipo de regresión de un calibrado, por falta de
     * datos (al menos 2 para lineal y al menos 3 para cuadrática)
     * 
     * @param tipo String que indica si la regresión es "lineal" o "cuadratica"
     */
    public static void alertaRegresion(String tipo) {
        int patrones = 2;
        if (tipo.equals("Cuadrática")) {
            patrones = 3;
        }
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error de Regresión");
        alerta.setHeaderText("Error de cálculo para regresión " + tipo);
        alerta.setContentText("Se necesitan al menos " + patrones + " patrones para calcular los coeficientes");
        alerta.showAndWait();
    }
    
    /**
     * Alerta que recuerda al usuario que es obligatorio seleccionar el "Ajuste" de un nuevo calibrado
     * para que se puedan seleccionar las cuentas correspondientes a cada patron para ese ajuste
     */
    public static void alertaAjuste() {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error al asignar patrón");
        alerta.setHeaderText("Error asignando patrón al calibrado");
        alerta.setContentText("No has seleccionado un ajuste para visualizar correctamente las cuentas de cada patrón");
        alerta.showAndWait();
    }
    
        /**
     * Alerta que recuerda al usuario que es obligatorio seleccionar el "Ajuste" de un nuevo calibrado
     * para que se puedan seleccionar las cuentas correspondientes a cada patron para ese ajuste
     */
    public static void alertaAjusteAnalisis() {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error al analizar patrón");
        alerta.setHeaderText("Error al analizar Patrón sin Ajuste seleccionado");
        alerta.setContentText("No has seleccionado un ajuste para analizar correctamente las cuentas del patrón");
        alerta.showAndWait();
    }
    
    /**
     * Alerta que recuerda al usuario seleccionar un calibrado de la tabla, para poder ver si gráfica, al pulsar 
     * el botón de ver gráfica
     */
    public static void alertaGrafica() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error al abrir la gráfica");
        alerta.setContentText("Es obligatorio seleccionar una calibración de la tabla y el tipo de regresión que se desea para la gráfica");
        alerta.showAndWait();
    }

    /**
     * Alerta al no encontrar el archivo ../grafica/Grafica.fxml
     * 
     * @param error String que informa por qué se ha producido el error
     */
    public static void alertaIOException(String error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error en la gráfica");
        alerta.setContentText(error);
        alerta.showAndWait();
    }

    /**
     * Alerta para informar al usuario de que los campos número de muestra y el tipo de 
     * calibrado son obligatorios
     */
    public static void alertaMuestra() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error al empezar análisis");
        alerta.setContentText("Introduce el número de muestra y el calibrado a aplicar");
        alerta.showAndWait();
    }

    /**
     * Alerta para informar al usuario de que el campo nº de muestra debe ser de 9 dígitos
     */
    public static void alertaNumeroMuestra() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error al empezar análisis");
        alerta.setContentText("Introduce un número de muestra correcto (9 dígitos)");
        alerta.showAndWait();
    }

    /**
     * Alerta que informa al usuario de que para realizar una busqueda por fecha hay
     * que seleccionar una fecha en el calendario
     */
    public static void alarmaFechaBusqueda() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error al asignar fecha");
        alerta.setContentText("Selecciona una fecha para la busqueda");
        alerta.showAndWait();
    }
    
    /**
     * Alerta que informa al usuario de que para realizar una busqueda por fecha hay
     * que seleccionar una fecha en el calendario
     */
    public static void alarmaFechaRangoBusqueda() {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("Error al asignar las fechas");
        alerta.setContentText("Selecciona una fecha posterior a la inicial en el campo 'hasta'");
        alerta.showAndWait();
    }

    /**
     * Alerta para informar de que hay campos necesarios por completar
     * @param mensajeError
     */
    public static void alertaErrorFormulario(String mensajeError) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error en los campos");
        alerta.setHeaderText("Debe completar los campos obligatorios");
        alerta.setContentText(mensajeError);
        alerta.showAndWait();
    }

    /**
     * Alerta para indicar que no se puede eliminar un objeto porque no se ha seleccionado
     * ninguno de la lista o tabla correspondiente
     * 
     * @param tipo String que indica si es un "Patron", "Calibrado" o "Ajuste"
     */
    public static void alertaErrorBorrar(String tipo) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error de selección");        
        alerta.setHeaderText("No has seleccionado un " + tipo);                
        alerta.setContentText("Debe seleccionar un " + tipo + " de la lista para proceder a su eliminación.");        
        alerta.showAndWait();
    }

    /**
     * Alerta para indicar que no se puede modificar un objeto porque no se ha seleccionado
     * ninguno de la lista o tabla correspondiente
     * 
     * @param tipo String que indica si es un "Patron", "Calibrado" o "Ajuste"
     */
    public static void alertaErrorModificar(String tipo) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error de selección");        
        alerta.setHeaderText("No has seleccionado un " + tipo);                
        alerta.setContentText("Debe seleccionar un " + tipo + " de la lista para proceder a su modificación.");       
        alerta.showAndWait();
    }

    /**
     * Alerta que avisa de un fallo en la conexión en el puerto COM indicado
     * 
     * @param puerto String con el puerto COM en el que falla la comunicación
     */
    public static void alertaConexionPuerto(String puerto) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error de conexión");
        alerta.setHeaderText("Error al acceder al puerto " + puerto);
        alerta.setContentText("Se ha producido un error de comunicación entre el equipo y el software. Revise el cable y la configuración del puerto COM tanto en el equipo como en el software");
        alerta.showAndWait();
    }  
    
    public static void alertaAbrirEditor(String editor) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Error de formulario de edición");
        alerta.setHeaderText("Error al acceder al formulario " + editor);
        alerta.setContentText("Se ha producido un error al localizar e intentar abrir el editor de " + editor);
        alerta.showAndWait();
    }    
}