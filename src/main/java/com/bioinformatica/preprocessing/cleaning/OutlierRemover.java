package com.bioinformatica.preprocessing.cleaning;

import java.util.Arrays;

/**
 * Clase para la eliminación de valores atípicos en datos de expresión génica.
 * Se basa en el método de Tukey (IQR) y desviación estándar.
 */
public class OutlierRemover {
    public static double[][] eliminarAtipicos(double[][] datos) {
        if(datos.length!=0){
            int filas = datos.length;
            int columnas = datos[0].length;
            double[][] datosFiltrados = new double[filas][columnas];

            for (int j = 0; j < columnas; j++) {
                double[] columna = new double[filas];
                for (int i = 0; i < filas; i++) {
                    columna[i] = datos[i][j];
                }
                double q1 = calcularPercentil(columna, 25);
                double q3 = calcularPercentil(columna, 75);
                double iqr = q3 - q1;
                double limiteInferior = q1 - 1.5 * iqr;
                double limiteSuperior = q3 + 1.5 * iqr;

                for (int i = 0; i < filas; i++) {
                    datosFiltrados[i][j] = (columna[i] >= limiteInferior && columna[i] <= limiteSuperior) ? columna[i] : Double.NaN;
                }
            }
            return datosFiltrados;
        } else {
            return datos;
        }
    }

    private static double calcularPercentil(double[] datos, int percentil) {
        double[] datos_aux = datos.clone();
        Arrays.sort(datos_aux);
        int index = (int) Math.ceil(percentil / 100.0 * datos_aux.length) - 1;
        return datos_aux[index];
    }
}
