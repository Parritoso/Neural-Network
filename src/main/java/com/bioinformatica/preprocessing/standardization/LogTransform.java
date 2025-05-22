package com.bioinformatica.preprocessing.standardization;

/**
 * Clase para aplicar transformación logarítmica a datos de expresión génica.
 */
public class LogTransform {
    public static void transformar(double[][] datos) {
        if(datos.length!=0){
            int filas = datos.length;
            int columnas = datos[0].length;

            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < columnas; j++) {
                    if (!Double.isNaN(datos[i][j])) {
                        if (datos[i][j] > 0) {
                            datos[i][j] = Math.log1p(datos[i][j]);
                        } else if (datos[i][j] == 0) {
                            datos[i][j] = Double.NEGATIVE_INFINITY;
                        }
                    }
                }
            }
        }
    }
}
