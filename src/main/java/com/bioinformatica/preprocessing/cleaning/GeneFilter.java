package com.bioinformatica.preprocessing.cleaning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase para el filtrado de genes con baja expresión y selección de genes relevantes.
 */
public class GeneFilter {
    public static double[][] filtrarGenes(double[][] datos, double umbralExpresion, double umbralVarianza) {
        List<double[]> genesFiltrados = new ArrayList<>();

        for (double[] gen : datos) {
            double media = calcularMedia(gen);
            double varianza = calcularVarianza(gen, media);

            if (media > umbralExpresion && varianza > umbralVarianza) {
                genesFiltrados.add(gen);
            }
        }

        return genesFiltrados.toArray(new double[0][]);
    }

    private static double calcularMedia(double[] datos) {
        return Arrays.stream(datos).average().orElse(0.0);
    }

    private static double calcularVarianza(double[] datos, double media) {
        return Arrays.stream(datos).map(x -> Math.pow(x - media, 2)).average().orElse(0.0);
    }
}
