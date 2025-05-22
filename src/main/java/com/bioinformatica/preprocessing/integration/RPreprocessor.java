package com.bioinformatica.preprocessing.integration;

import com.bioinformatica.Main;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


/**
 * Clase para la integración con R mediante Rserve, permitiendo ejecutar scripts avanzados en R
 * y utilizar paquetes de Bioconductor.
 */
public class RPreprocessor {
    private RConnection conexion;

    public RPreprocessor() {
        try {
            this.conexion = new RConnection();
            if(conexion.isConnected() || conexion==null){
                Main.R = false;
            }
        } catch (RserveException | NullPointerException e) {
            System.err.println("Error al conectar con Rserve: " + e.getMessage());
        }
    }

    /**
     * Ejecuta un script en R desde un archivo.
     * @param rutaScript Ruta del script R a ejecutar.
     */
    public void ejecutarScript(String rutaScript) {
        try {
            conexion.eval("source('" + rutaScript + "')");
            String error = conexion.getLastError();
            if(error!=null){
                System.err.println(error);
            }

        } catch (RserveException | NullPointerException e) {
            System.err.println("Error al ejecutar el script R: " + e.getMessage());
            Main.R = false;
        }
    }

    /**
     * Ejecuta un comando R directamente desde Java.
     * @param comando Código en R a ejecutar.
     */
    public void ejecutarComando(String comando) {
        try {
            conexion.eval(comando);
        } catch (RserveException | NullPointerException e) {
            System.err.println("Error al ejecutar el comando R: " + e.getMessage());
            Main.R = false;
        }
    }

    /**
     * Carga y usa un paquete de Bioconductor.
     * @param paquete Nombre del paquete de Bioconductor.
     */
    public void cargarPaqueteBioconductor(String paquete) {
        try {
            conexion.eval("if (!requireNamespace('" + paquete + "', quietly = TRUE)) {\n" +
                    "    BiocManager::install('" + paquete + "')\n" +
                    "} else {\n" +
                    "    library(" + paquete + ")\n" +
                    "}");
            System.out.println("Paquete Bioconductor cargado: " + paquete);
        } catch (RserveException | NullPointerException e) {
            System.err.println("Error al cargar el paquete Bioconductor: " + e.getMessage());
            Main.R = false;
        }
    }

    /**
     * Obtiene un valor desde R.
     * @param variable Nombre de la variable en R.
     * @return Valor como String o null si hay error.
     */
    public String obtenerValor(String variable) {
        try {
            return conexion.eval(variable).asString();
        } catch (RserveException | REXPMismatchException | NullPointerException e) {
            System.err.println("Error al obtener el valor de " + variable + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Cierra la conexión con Rserve.
     */
    public void cerrarConexion() {
        if (conexion != null) {
            conexion.close();
        }
    }
}
