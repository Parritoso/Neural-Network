package com.bioinformatica.preprocessing.cleaning;

/**
 * Clase para la imputación de datos faltantes en matrices de expresión génica.
 */
public class MissingDataHandler {
    public static void imputarDatosFaltantes(double[][] datos) {
        if(datos.length!=0) {
            int filas = datos.length;
            int columnas = datos[0].length;

            for (int j = 0; j < columnas; j++) {
                double suma = 0;
                int contador = 0;
                for (int i = 0; i < filas; i++) {
                    if (!Double.isNaN(datos[i][j])) {
                        suma += datos[i][j];
                        contador++;
                    }
                }
                double media = (contador > 0) ? suma / contador : 0;
                for (int i = 0; i < filas; i++) {
                    if (Double.isNaN(datos[i][j])) {
                        datos[i][j] = media;
                    }
                }
            }
        }
    }
}
