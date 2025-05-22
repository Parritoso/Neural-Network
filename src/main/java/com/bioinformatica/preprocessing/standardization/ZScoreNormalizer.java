package com.bioinformatica.preprocessing.standardization;

/**
 * Clase para la normalización Z-score de datos de expresión génica.
 */
public class ZScoreNormalizer {
    public static void normalizar(double[][] datos) {
        if(datos.length!=0){
            int filas = datos.length;
            int columnas = datos[0].length;

            for (int j = 0; j < columnas; j++) {
                double media = calcularMedia(datos, j);
                double desviacion = calcularDesviacionEstandar(datos, j, media);

                for (int i = 0; i < filas; i++) {
                    if (!Double.isNaN(datos[i][j]) && desviacion != 0) {
                        datos[i][j] = (datos[i][j] - media) / desviacion;
                    }
                }
            }
        }
    }

    private static double calcularMedia(double[][] datos, int columna) {
        double suma = 0;
        int contador = 0;
        for (double[] fila : datos) {
            if (!Double.isNaN(fila[columna])) {
                suma += fila[columna];
                contador++;
            }
        }
        return (contador > 0) ? suma / contador : 0;
    }

    private static double calcularDesviacionEstandar(double[][] datos, int columna, double media) {
        double suma = 0;
        int contador = 0;
        for (double[] fila : datos) {
            if (!Double.isNaN(fila[columna])) {
                suma += Math.pow(fila[columna] - media, 2);
                contador++;
            }
        }
        return (contador > 0) ? Math.sqrt(suma / contador) : 0;
    }
}
