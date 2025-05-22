package com.bioinformatica.exploratory.display;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

public class ScatterPlot {
    public static JFreeChart mostrarScatterPlot(double[][] datos, String titulo) {
        XYDataset dataset = crearDatasetScatter(datos);
        JFreeChart chart = ChartFactory.createScatterPlot(titulo, "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new XYDotRenderer());
        return chart;
    }

    private static XYDataset crearDatasetScatter(double[][] datos) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Datos", datos);
        return dataset;
    }
}
