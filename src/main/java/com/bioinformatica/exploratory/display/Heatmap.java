package com.bioinformatica.exploratory.display;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultHeatMapDataset;
import org.jfree.data.general.HeatMapDataset;


public class Heatmap {
    public static JFreeChart mostrarHeatmap(double[][] datos, String titulo) {
        HeatMapDataset dataset = crearDatasetHeatmap(datos);
        return ChartFactory.createXYLineChart(titulo, "X", "Y", null);
    }

    private static HeatMapDataset crearDatasetHeatmap(double[][] datos) {
        int filas = datos.length;
        int columnas = datos[0].length;
        DefaultHeatMapDataset dataset = new DefaultHeatMapDataset(filas, columnas, 0, filas, 0, columnas);
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                dataset.setZValue(i, j, datos[i][j]);
            }
        }
        return dataset;
    }
}
