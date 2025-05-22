package com.bioinformatica.preprocessing.structures;

import com.bioinformatica.preprocessing.integration.RPreprocessor;
import org.apache.commons.math3.stat.inference.OneWayAnova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase para la selección de características con ANOVA, Chi-cuadrado y modelos estadísticos.
 */
public class FeatureSelector {
    public static double[][] seleccionarConANOVA(double[][] datos, int[] etiquetas, double umbralP) {
        OneWayAnova anova = new OneWayAnova();
        List<double[]> datosFiltrados = new ArrayList<>();

        for (double[] gen : datos) {
            List<double[]> grupos = new ArrayList<>();
            Map<Integer, List<Double>> agrupados = new HashMap<>();

            for (int i = 0; i < etiquetas.length; i++) {
                agrupados.computeIfAbsent(etiquetas[i], k -> new ArrayList<>()).add(gen[i]);
            }

            for (List<Double> valores : agrupados.values()) {
                grupos.add(valores.stream().mapToDouble(Double::doubleValue).toArray());
            }

            double pValor = anova.anovaPValue(grupos);
            if (pValor < umbralP) {
                datosFiltrados.add(gen);
            }
        }

        return datosFiltrados.toArray(new double[0][]);
    }
    public static void seleccionarConChiCuadrado(String rutaScript, RPreprocessor rPreprocessor) {
        rPreprocessor.ejecutarScript(rutaScript);
    }
}
