package com.bioinformatica.exploratory.display;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.util.List;

public class BoxPlot {
    public static JFreeChart mostrarBoxPlot(List<Double> datos, String titulo) {
        BoxAndWhiskerCategoryDataset dataset = crearDatasetBoxPlot(datos);
        return ChartFactory.createBoxAndWhiskerChart(titulo, "Grupo", "Valores", dataset, true);
    }

    private static BoxAndWhiskerCategoryDataset crearDatasetBoxPlot(List<Double> datos) {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(datos), "Grupo 1", "");
        return dataset;
    }
}
