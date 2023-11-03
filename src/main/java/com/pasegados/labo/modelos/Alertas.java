package com.pasegados.labo.modelos;

import java.io.File;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Esta clase abstracta va a controlar todas las pantallas de "Alerta" que se puedan generar en el uso del programa
 *
 * @author Pedro A. Segado Solano
 */
public abstract class Alertas {

    private static Alert alert = new Alert(Alert.AlertType.NONE); // alerta comun a todos los metodos de la clase

    static { // Asigno el CSS a la alerta común, así todas las alertas tendrán el mismo aspecto
        alert.getDialogPane().getStylesheets().add(Alertas.class.getResource("aspecto.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert-dialog");
    }
    

/* ----- INICIO DE CARGA DE LA APP -----  */
     
    // falla algo al buscar los fxml
    public static void alertaCargaInicial(String error) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error en inicio de la aplicación");
        alert.setContentText("No se localiza algún FXML durante la carga de la aplicación" + "\n" + error);
        alert.showAndWait();
    }

    // Avisamos al usuario de fallo al recibir datos al analizar muestra/patron
    public static void alertaRecibirDatos() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error en la recepción de datos por el puerto COM");
        alert.setContentText("Un fallo ha impedido recibir los datos del análisis." + "\n" + "Análisis abortado");
        alert.showAndWait();
    }



    
/* ----- CREACION O BORRADO DE LA BBDD -----  */
    
    // Cuando se crea la BBDD por primera vez o tras un borrado de la misma
    public static boolean alertaCrearNuevaBBDD() {
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Crear nueva BBDD");
        alert.setHeaderText("Es necesario crear una nueva 'Base de Datos'");
        alert.setContentText("Por ser la primera ejecución o porque se ha borrado intencionadamente esta, no existe la 'Base de Datos'."
                + "\n\n" + "¿Desea crearla para empezar a trabajar?" + "\n\n");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    // Se ha creado la base de datos correctamente
    public static void alertaBBDDCreada() {
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Información BBDD");
        alert.setHeaderText("Información BBDD");
        alert.setContentText("La 'Base de Datos' se ha creado con exito." + "\n\n" + "Pulse aceptar para iniciar la aplicación" + "\n\n");
        alert.showAndWait();
    }
    
    // Ha fallado la creación de la base de datos
    public static void alertaBBDDErrorCrear() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error al crear BBDD");
        alert.setHeaderText("Error al crear BBDD");
        alert.setContentText("La 'Base de Datos' NO se ha podido crear con exito." + "\n\n" + "Pulse aceptar para cerrar la aplicación" + "\n\n");
        alert.showAndWait();
    }

    // Cuando se va a borrar, previamente solicitamos hacer copia de seguridad
    public static boolean alertaPreviaCrearCopia() {        
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Borrar Base de Datos");
        alert.setHeaderText("¿Realizar copia de seguridad antes de eliminar la 'Base de Datos'?");
        alert.setContentText("Es conveniente realizar una copia por si hay algún problema poder revertir la situación al estado anterior."
                        + "\n\n" + "¿Desea crear una copia de seguridad?" + "\n\n");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    // Confirmacion para borrar BBDD, condicional a si se ha realizado copia de seguridad y del exito de creacion de la copia
    public static boolean alertaBorradoBBDD(boolean copia, boolean exitoCopia) {
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Borrar Base de Datos");
        alert.setHeaderText("¿Seguro que quiere eliminar la 'Base de Datos'?");
        if (copia & exitoCopia) {
            alert.setContentText("El estado de la 'Base de Datos' se podrá revertir gracias a la a copia de seguridad creada en el paso anterior."
                    + "\n\n" + "¿Desea borrar por completo la 'Base de Datos'?" + "\n\n");
        } else if (copia & !exitoCopia) {
            alert.setContentText("El estado de la 'Base de Datos'  NO se podrá revertir porque ha fallado el proceso de creación de la copia de seguridad en el paso anterior."
                    + "\n\n" + "¿Desea borrar por completo la 'Base de Datos'?" + "\n\n");
        } else {
            alert.setContentText("Este es un proceso irreversible ya que no se ha creado copia de seguridad en el paso anterior."
                    + "\n\n" + "¿Desea borrar por completo la 'Base de Datos'?" + "\n\n");
        }

        //Desactiva boton aceptar por defecto        
        Button aceptar = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        aceptar.setDefaultButton(false);

        //Establece boton cancelar por defecto
        Button cancelar = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelar.setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }

    // Informa de que se ha borrado correctamente la bbdd y que se va a recargar la app
    public static void alertaBBDDBorradoExito() {
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Información BBDD");
        alert.setHeaderText("Base de Datos eliminada con éxito");
        alert.setContentText("La 'Base de Datos' se ha eliminado por completo. Se va a recargar el programa para generar una nueva 'Base de Datos'."
                + "\n\n" + "Pulse aceptar para reiniciar la aplicación" + "\n\n");
        alert.showAndWait();
    }

    // Informa de Error durante el borrado de la bbdd
    public static void alertaBBDDBorradoFracaso(String error) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Información BBDD");
        alert.setHeaderText("Error al intentar eliminar la Base de Datos");
        alert.setContentText("Ha surgido un problema durante el proceso de borrado. "
                + "No se garantiza que se haya completado el borrado: " + "\n\n" + error + "\n\n");
        alert.showAndWait();
    }

    
/* ----- CREAR COPIA DE SEGURIDAD DE LA BBDD -----  */

    // Confirmación previa a crear copia de seguridad
    public static boolean alertaCrearCopia() {
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Copia de seguridad de la 'Base de Datos'");
        alert.setHeaderText("¿Deseá realizar una copia de seguridad del estado actual de la 'Base de Datos'?");
        alert.setContentText("La copia se guardará en la carpeta 'copiaSeguridad', en un directorio con la fecha actual en formato 'AAMMDD'."
                + "\n\n" + "¿Desea crear una copia de seguridad?" + "\n\n");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
    
    // Informa de que se ha creado correctamente la copia de la bbdd
    public static void alertaCopiaCreadaExito(String directorio) {
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Información BBDD");
        alert.setHeaderText("Copia realizada correctamente");
        alert.setContentText("Se ha guardado la copia en " + directorio + "." + "\n\n");                
        alert.showAndWait();
    }

    // Informa de Error durante el borrado de la bbdd
    public static void alertaCopiaCreadaFracaso(String error) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Información BBDD");
        alert.setHeaderText("Error al intentar crear la copia de seguridad");
        alert.setContentText("Ha surgido un problema durante el proceso de creación de la copia de seguridad. "
                + "No se garantiza que se haya completado correctamente: " + "\n\n" + error + "\n\n");                
        alert.showAndWait();
    }
    
/* ----- RESTAURAR COPIA DE SEGURIDAD DE LA BBDD -----  */

    // Confirmación previa a restaurar copia de seguridad
    public static File alertaRestaurarCopia() {
        File directorioCopia = null;
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restaurar Copia de seguridad de la 'Base de Datos'");
        alert.setHeaderText("¿Deséa restaurar la 'Base de Datos' a un estado anterior?");
        alert.setContentText("Selecciona la copia que deseas restaurar pulsando el botón 'Seleccionar'" + "\n\n"); 
                
        // Agregar un botón extra
        ButtonType customButton = new ButtonType("Seleccionar");
        alert.getButtonTypes().add(customButton);

        boolean eleccion = false;
        while (!eleccion){ // Bucle se repite hasta que se pulse aceptar o cancelar
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                eleccion = true;
            }
            else if (result.get() == ButtonType.CANCEL) {
                directorioCopia = null;
                eleccion = true;
            }
            else if (result.get() == customButton) {
                directorioCopia = seleccionardirectorio();
                alert.setContentText(!(directorioCopia==null)? ("Ha seleccionado la copia '" + directorioCopia.getName() + "'" + "\n\n" + 
                        "Pulse aceptar para restaurarla o seleccione otra copia ..." + "\n\n"):("No ha seleccionado ninguna copia." + "\n\n" + 
                        "Debe Seleccionar una o bien cancelar el proceso de restauración ..." + "\n\n"));
            } else{
                alert.getButtonTypes().remove(customButton); // elimino el boton para que no salga en los demás alerts
            }
        }
        alert.getButtonTypes().remove(customButton); // elimino el boton para que no salga en los demás alerts
        return directorioCopia;
    }
    
    //DirectoryChooser
    private static File seleccionardirectorio(){
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("./copiaSeguridad/"));
        directoryChooser.setTitle("Selecciona el directorio con la copia a restaurar");
        File directorioCopia = directoryChooser.showDialog(stage);
        return directorioCopia;    
    }
        
    
    
    
    
    
    
    

    /**
     * Error de acceso a la BBDD
     *
     * @param error String que informa del tipo de error
     */
    public static void alertaBBDD(String error) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error en la BBDD");
        alert.setHeaderText("Error accediendo");
        alert.setContentText(error);
        alert.showAndWait();
    }

    /**
     * Error de acceso al Puerto
     */
    public static void alertaPuerto() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error acceso Puerto COM");
        alert.setHeaderText("Error accediendo al puerto de comunicaciones");
        alert.setContentText("No se ha podido abrír el puerto para enviar los datos necesarios");
        alert.showAndWait();
    }

    /**
     * Violación de la Clave Primaria en la tabla Análisis de la BBDD
     */
    public static void alertaPKAnalisis() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error por violación PK en la BBDD");
        alert.setHeaderText("Ya existe este registro en la BBDD");
        alert.setContentText("La \"Clave Primaria\" está compuesta por la combinación del número de muestra, "
                + "la identificación y el calibrado usado."
                + "\n\n"
                + "Cambie el valor de al menos uno de estos campos");
        alert.showAndWait();
    }

    /**
     * Confirmación del usuario de que se puede eliminar un objeto (Patron, Calibrado o Ajuste)
     *
     * @param tipo String que indica si es un "Ajuste", "Patron" o "Calibrado"
     * @param valor String que indica el nombre del objeto a borrar, tal y como lo ve el usuario en la lista
     * @return boolean indicando true si se pulsa "OK" o false si se "CANCELA" o "CIERRA"
     */
    public static boolean alertaEliminaObjeto(String tipo, String valor) {
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
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
     *
     * @param tipo String que indica si es un "Ajuste", "Patron" o "Calibrado"
     * @param valor String que indica el nombre del objeto a borrar, tal y como lo ve el usuario en la lista
     * @return boolean indicando true si se pulsa "OK" o false si se "CANCELA" o "CIERRA"
     */
    public static boolean alertaEliminaObjeto(String tipo, String valor, String loUsan) {
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
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
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Eliminar Ajuste usado en calibrado");
        alert.setHeaderText("El ajuste " + valor + " se usa en al menos un calibrado");
        alert.setContentText("Revisar los calibrados afectados y eliminarlos o cambiar su ajuste previamente al borrado de este ajuste" + "\n");

        alert.showAndWait();
    }

    /**
     * Tras eliminar un patron, hay calibrados que no tienen suficientes para mantener su tipo de regresión
     */
    public static void alertaCalibradoFaltanPatrones(String nombrePatron, String nombreCalibrado) {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Revisar Calibrado");
        alert.setHeaderText("El borrado del patrón " + nombrePatron + " afecta al calibrado " + nombreCalibrado);
        alert.setContentText("Revisar el calibrado " + nombreCalibrado + " y ajustar el tipo de regresión y/o el número de patrones asignados a la misma.");

        alert.showAndWait();
    }

    /**
     * Generando un informe jasperreports
     */
    public static void alertaInforme(String tipo, String nombre, String error) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Informe");
        alert.setHeaderText("No se ha podido generar el informe de " + tipo + " " + nombre);
        alert.setContentText("Error: " + error + "\n");

        alert.showAndWait();
    }

    /**
     * Generando un informe jasperreports
     */
    public static void alertaInformeNoSeleccionado() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Informe");
        alert.setHeaderText("Error de selección");
        alert.setContentText("No se ha seleccionado un calibrado del que emitir informe");

        alert.showAndWait();
    }

    /**
     * Alerta indicando que no se ha seleccionado ningún patron para asignar a un Calibrado, tras pulsar el botón
     * "Asignar" en el editor de calibrados
     */
    public static void alertaAsignaPatron() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al asignar patrón");
        alert.setContentText("No hay ningún patrón seleccionado en la lista de patrones disponibles");
        alert.showAndWait();
    }

    /**
     * Alerta indicando que no se ha seleccionado ningún patron para eliminar del Calibrado, tras pulsar el botón de
     * "Eliminar" en el editor de calibrados
     */
    public static void alertaQuitarPatron() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al eliminar patrón del calibrado");
        alert.setContentText("No hay ningún patrón seleccionado en la lista de patrones asignados al calibrado");
        alert.showAndWait();
    }

    /**
     * Alerta para indicar que un "Patrón" no tiene cuentas para un "Ajuste" determinado, lo cual indica que no ha sido
     * analizado en esas condiciones, y por tanto no se puede asignar a un calibrado que use ese ajuste, hasta que no se
     * analice previamente.
     *
     * @param patron String con el nombre del patron afectado
     * @param ajuste String con el nombre del ajuste afectado
     */
    public static void alertaPatronNoAnalizado(String patron, String ajuste) {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error al asignar patrón");
        alert.setHeaderText("Error asignando patrón al calibrado");
        alert.setContentText(patron + " aún no ha sido analizado en el equipo usando el ajuste " + ajuste + "." + "\n \n"
                + "Para asignarlo a la calibración introduzcalo en el equipo y pulse el botón 'Analizar'");
        alert.showAndWait();
    }

    /**
     * Alerta que indica que se debe seleccionar un solo Patron para analizar al pulsar el botón de "Analisis" de
     * patron.
     *
     * @param error int que toma el valor 0 si no se ha seleccionado ningún patron o >1 si se han seleccionado varios
     * patrones
     */
    public static void alertaAnalisisPatronSeleccionado(int error) {
        String mostrar = "";
        if (error == 0) {
            mostrar = "Se debe selecciónar un patrón de la lista de 'Patrones disponibles' para proceder al análisis de sus cuentas.";
        } else {
            mostrar = "Se han seleccionado varios patrones. Solo se debe selecciónar un patrón de la lista de 'Patrones disponibles' para proceder al análisis de sus cuentas.";
        }
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error de selección");
        alert.setHeaderText("Error en la selección del patrón a analizar");
        alert.setContentText(mostrar);
        alert.showAndWait();
    }

    /**
     * Alerta que indica que no se puede calcular el tipo de regresión de un calibrado, por falta de datos (al menos 2
     * para lineal y al menos 3 para cuadrática)
     *
     * @param tipo String que indica si la regresión es "lineal" o "cuadratica"
     */
    public static void alertaRegresion(String tipo) {
        int patrones = 2;
        if (tipo.equals("Cuadrática")) {
            patrones = 3;
        }
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error de Regresión");
        alert.setHeaderText("Error de cálculo para regresión " + tipo);
        alert.setContentText("Se necesitan al menos " + patrones + " patrones para calcular los coeficientes");
        alert.showAndWait();
    }

    /**
     * Alerta que recuerda al usuario que es obligatorio seleccionar el "Ajuste" de un nuevo calibrado para que se
     * puedan seleccionar las cuentas correspondientes a cada patron para ese ajuste
     */
    public static void alertaAjuste() {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error al asignar patrón");
        alert.setHeaderText("Error asignando patrón al calibrado");
        alert.setContentText("No has seleccionado un ajuste para visualizar correctamente las cuentas de cada patrón");
        alert.showAndWait();
    }

    /**
     * Alerta que recuerda al usuario que es obligatorio seleccionar el "Ajuste" de un nuevo calibrado para que se
     * puedan seleccionar las cuentas correspondientes a cada patron para ese ajuste
     */
    public static void alertaAjusteAnalisis() {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error al analizar patrón");
        alert.setHeaderText("Error al analizar Patrón sin Ajuste seleccionado");
        alert.setContentText("No has seleccionado un ajuste para analizar correctamente las cuentas del patrón");
        alert.showAndWait();
    }

    /**
     * Alerta que recuerda al usuario seleccionar un calibrado de la tabla, para poder ver si gráfica, al pulsar el
     * botón de ver gráfica
     */
    public static void alertaGrafica() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al abrir la gráfica");
        alert.setContentText("Es obligatorio seleccionar una calibración de la tabla y el tipo de regresión que se desea para la gráfica");
        alert.showAndWait();
    }

    /**
     * Alerta al no encontrar el archivo ../grafica/Grafica.fxml
     *
     * @param error String que informa por qué se ha producido el error
     */
    public static void alertaIOException(String error) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error en la gráfica");
        alert.setContentText(error);
        alert.showAndWait();
    }

    /**
     * Alerta para informar al usuario de que los campos número de muestra y el tipo de calibrado son obligatorios
     */
    public static void alertaMuestra() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al empezar análisis");
        alert.setContentText("Introduce el número de muestra y el calibrado a aplicar");
        alert.showAndWait();
    }

    /**
     * Alerta para informar al usuario de que el campo nº de muestra debe ser de 9 dígitos
     */
    public static void alertaNumeroMuestra() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al empezar análisis");
        alert.setContentText("Introduce un número de muestra correcto (9 dígitos)");
        alert.showAndWait();
    }

    /**
     * Alerta que informa al usuario de que para realizar una busqueda por fecha hay que seleccionar una fecha en el
     * calendario
     */
    public static void alarmaFechaBusqueda() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al asignar fecha");
        alert.setContentText("Selecciona una fecha para la busqueda");
        alert.showAndWait();
    }

    /**
     * Alerta que informa al usuario de que para realizar una busqueda por fecha hay que seleccionar una fecha en el
     * calendario
     */
    public static void alarmaFechaRangoBusqueda() {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al asignar las fechas");
        alert.setContentText("Selecciona una fecha posterior a la inicial en el campo 'hasta'");
        alert.showAndWait();
    }

    /**
     * Alerta para informar de que hay campos necesarios por completar
     *
     * @param mensajeError
     */
    public static void alertaErrorFormulario(String mensajeError) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error en los campos");
        alert.setHeaderText("Debe completar los campos obligatorios");
        alert.setContentText(mensajeError);
        alert.showAndWait();
    }

    /**
     * Alerta para indicar que no se puede eliminar un objeto porque no se ha seleccionado ninguno de la lista o tabla
     * correspondiente
     *
     * @param tipo String que indica si es un "Patron", "Calibrado" o "Ajuste"
     */
    public static void alertaErrorBorrar(String tipo) {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error de selección");
        alert.setHeaderText("No has seleccionado un " + tipo);
        alert.setContentText("Debe seleccionar un " + tipo + " de la lista para proceder a su eliminación.");
        alert.showAndWait();
    }

    /**
     * Alerta para indicar que no se puede modificar un objeto porque no se ha seleccionado ninguno de la lista o tabla
     * correspondiente
     *
     * @param tipo String que indica si es un "Patron", "Calibrado" o "Ajuste"
     */
    public static void alertaErrorModificar(String tipo) {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error de selección");
        alert.setHeaderText("No has seleccionado un " + tipo);
        alert.setContentText("Debe seleccionar un " + tipo + " de la lista para proceder a su modificación.");
        alert.showAndWait();
    }

    /**
     * Alerta que avisa de un fallo en la conexión en el puerto COM indicado
     *
     * @param puerto String con el puerto COM en el que falla la comunicación
     */
    public static void alertaConexionPuerto(String puerto) {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error de conexión");
        alert.setHeaderText("Error al acceder al puerto " + puerto);
        alert.setContentText("Se ha producido un error de comunicación entre el equipo y el software. Revise el cable y la configuración del puerto COM tanto en el equipo como en el software");
        alert.showAndWait();
    }

    public static void alertaAbrirEditor(String editor) {
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.setTitle("Error de formulario de edición");
        alert.setHeaderText("Error al acceder al formulario " + editor);
        alert.setContentText("Se ha producido un error al localizar e intentar abrir el editor de " + editor);
        alert.showAndWait();
    }
}
