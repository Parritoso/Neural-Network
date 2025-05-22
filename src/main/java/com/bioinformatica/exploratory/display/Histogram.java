package com.bioinformatica.exploratory.display;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

public class Histogram {
    public static JFreeChart mostrarHistograma(double[] datos, int bins, String titulo) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Datos", datos, bins);
        return ChartFactory.createHistogram(titulo, "Valores", "Frecuencia", dataset, PlotOrientation.VERTICAL, true, true, false);
    }
}