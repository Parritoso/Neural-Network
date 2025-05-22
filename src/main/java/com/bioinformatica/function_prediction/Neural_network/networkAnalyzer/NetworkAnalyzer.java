package com.bioinformatica.function_prediction.Neural_network.networkAnalyzer;

import com.bioinformatica.preprocessing.integration.RPreprocessor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NetworkAnalyzer {
    private static final Logger logger = Logger.getLogger("MacroROCCurveLogger");
    public void calculateLossHistory(List<Double> lossHistory) {
        System.out.println("--- Historial de Pérdida ---");
        if (lossHistory != null && !lossHistory.isEmpty()) {
            System.out.println("Pérdida en la primera época: " + String.format("%.9f", lossHistory.get(0)));
            System.out.println("Pérdida en la última época: " + String.format("%.9f", lossHistory.get(lossHistory.size() - 1)));
            // Puedes añadir más análisis aquí, como la tendencia general de la pérdida.
        } else {
            System.out.println("No hay historial de pérdida disponible.");
        }
        System.out.println("----------------------------");
    }

    public void calculateTrainingTime(long trainingTimeMillis) {
        long seconds = (trainingTimeMillis / 1000) % 60;
        long minutes = (trainingTimeMillis / (1000 * 60)) % 60;
        long hours = trainingTimeMillis / (1000 * 60 * 60);
        System.out.println("--- Tiempo de Entrenamiento ---");
        System.out.println("Tiempo total de entrenamiento: " + hours + " horas, " + minutes + " minutos, " + seconds + " segundos.");
        System.out.println("-------------------------------");
    }

    public void displayHyperparameters(Map<String, Object> hyperparameters) {
        System.out.println("--- Hiperparámetros de la Red ---");
        if (hyperparameters != null && !hyperparameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : hyperparameters.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } else {
            System.out.println("No se proporcionaron hiperparámetros.");
        }
        System.out.println("----------------------------------");
    }

    public void analyzeTrainingData(float[][] trainingData, float[][] trainingLabels) {
        System.out.println("--- Análisis de Datos de Entrenamiento ---");
        if (trainingData != null) {
            System.out.println("Número de muestras de entrenamiento: " + trainingData.length);
            if (trainingData.length > 0) {
                System.out.println("Número de características por muestra: " + trainingData[0].length);
            }
        } else {
            System.out.println("No hay datos de entrenamiento disponibles.");
        }

        if (trainingLabels != null) {
            System.out.println("Número de etiquetas de entrenamiento: " + trainingLabels.length);
            // Aquí podrías añadir análisis sobre la distribución de las etiquetas si es relevante para tu tarea.
        } else {
            System.out.println("No hay etiquetas de entrenamiento disponibles.");
        }
        System.out.println("-----------------------------------------");
    }

    // Asumiendo un problema de clasificación binaria para la ROC (una única salida)
    public String generateRocCurve(float[][] predictions, float[][] trueLabels, String filePath) {
//        if (predictions == null || trueLabels == null || predictions.length != trueLabels.length || predictions[0].length != trueLabels[0].length) {
//            System.err.println("Error: Formato de datos incorrecto para la curva ROC (las dimensiones de las predicciones y las etiquetas deben coincidir).");
//            return null;
//        }
//
//        int numLabels = predictions[0].length;
//        XYSeriesCollection overallDataset = new XYSeriesCollection();
//
//        for (int i = 0; i < numLabels; i++) {
//            List<ScoreLabelPair> scoreLabelPairs = new ArrayList<>();
//            for (int j = 0; j < predictions.length; j++) {
//                scoreLabelPairs.add(new ScoreLabelPair(predictions[j][i], (int) trueLabels[j][i]));
//            }
//
//            Collections.sort(scoreLabelPairs, Collections.reverseOrder());
//
//            int nPositive = 0;
//            int nNegative = 0;
//            for (ScoreLabelPair pair : scoreLabelPairs) {
//                if (pair.label == 1) {
//                    nPositive++;
//                } else {
//                    nNegative++;
//                }
//            }
//
//            XYSeries series = new XYSeries("Label " + i);
//            series.add(0.0, 0.0); // Punto inicial
//
//            int tp = 0;
//            int fp = 0;
//
//            for (ScoreLabelPair pair : scoreLabelPairs) {
//                if (pair.label == 1) {
//                    tp++;
//                } else {
//                    fp++;
//                }
//                double tpr = nPositive == 0 ? 0 : (double) tp / nPositive;
//                double fpr = nNegative == 0 ? 0 : (double) fp / nNegative;
//                series.add(fpr, tpr);
//            }
//            series.add(1.0, 1.0); // Punto final
//            overallDataset.addSeries(series);
//        }
//
//        JFreeChart chart = ChartFactory.createXYLineChart(
//                "Curvas ROC por Etiqueta",
//                "Tasa de Falsos Positivos (FPR)",
//                "Tasa de Verdaderos Positivos (TPR)",
//                overallDataset,
//                PlotOrientation.VERTICAL,
//                true,
//                true,
//                false
//        );
//
//        File rocFile = new File(filePath);
//        try {
//            ChartUtils.saveChartAsPNG(rocFile, chart, 800, 600);
//            System.out.println("Curvas ROC por etiqueta guardadas en: " + filePath);
//            return filePath;
//        } catch (IOException e) {
//            System.err.println("Error al guardar las curvas ROC: " + e.getMessage());
//            return null;
//        }
        if (predictions == null || trueLabels == null || predictions.length != trueLabels.length || predictions[0].length != trueLabels[0].length) {
            System.err.println("Error: Formato de datos incorrecto para la curva ROC.");
            return null;
        }

        int numSamples = predictions.length;
        int numLabels = predictions[0].length;
        List<ScoreLabelPair> allScoreLabelPairs = new ArrayList<>();

        // Agrupar todas las predicciones y etiquetas
        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < numLabels; j++) {
                allScoreLabelPairs.add(new ScoreLabelPair(predictions[i][j], (int) trueLabels[i][j]));
            }
        }

        Collections.sort(allScoreLabelPairs, Collections.reverseOrder());

        int nPositive = 0;
        int nNegative = 0;
        for (ScoreLabelPair pair : allScoreLabelPairs) {
            if (pair.label == 1) {
                nPositive++;
            } else {
                nNegative++;
            }
        }

        XYSeries microAverageSeries = new XYSeries("Micro-Average ROC");
        microAverageSeries.add(0.0, 0.0); // Punto inicial

        int tp = 0;
        int fp = 0;

        for (ScoreLabelPair pair : allScoreLabelPairs) {
            if (pair.label == 1) {
                tp++;
            } else {
                fp++;
            }
            double tpr = nPositive == 0 ? 0 : (double) tp / nPositive;
            double fpr = nNegative == 0 ? 0 : (double) fp / nNegative;
            microAverageSeries.add(fpr, tpr);
        }
        microAverageSeries.add(1.0, 1.0); // Punto final

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(microAverageSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Curva ROC (Micro-Promedio)",
                "Tasa de Falsos Positivos (FPR)",
                "Tasa de Verdaderos Positivos (TPR)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        File rocFile = new File(filePath);
        try {
            ChartUtils.saveChartAsPNG(rocFile, chart, 800, 600);
            System.out.println("Curva ROC (micro-promedio) guardada en: " + filePath);
            return filePath;
        } catch (IOException e) {
            System.err.println("Error al guardar la curva ROC (micro-promedio): " + e.getMessage());
            return null;
        }
    }

//    public String generateMacroAverageRocCurve(float[][] predictions, float[][] trueLabels, String filePath) {
//        if (predictions == null || trueLabels == null || predictions.length != trueLabels.length || predictions[0].length != trueLabels[0].length) {
//            System.err.println("Error: Formato de datos incorrecto para la curva ROC (macro-promedio).");
//            return null;
//        }
//
//        int numLabels = predictions[0].length;
//        XYSeriesCollection overallDataset = new XYSeriesCollection();
//        List<XYSeries> rocCurves = new ArrayList<>();
//
//        for (int i = 0; i < numLabels; i++) {
//            List<ScoreLabelPair> scoreLabelPairs = new ArrayList<>();
//            for (int j = 0; j < predictions.length; j++) {
//                scoreLabelPairs.add(new ScoreLabelPair(predictions[j][i], (int) trueLabels[j][i]));
//            }
//            Collections.sort(scoreLabelPairs, Collections.reverseOrder());
//
//            int nPositive = 0;
//            int nNegative = 0;
//            for (ScoreLabelPair pair : scoreLabelPairs) {
//                if (pair.label == 1) {
//                    nPositive++;
//                } else {
//                    nNegative++;
//                }
//            }
//
//            XYSeries series = new XYSeries("Label " + i);
//            series.add(0.0, 0.0);
//            int tp = 0;
//            int fp = 0;
//            for (ScoreLabelPair pair : scoreLabelPairs) {
//                if (pair.label == 1) {
//                    tp++;
//                } else {
//                    fp++;
//                }
//                double tpr = nPositive == 0 ? 0 : (double) tp / nPositive;
//                double fpr = nNegative == 0 ? 0 : (double) fp / nNegative;
//                series.add(fpr, tpr);
//            }
//            series.add(1.0, 1.0);
//            rocCurves.add(series);
//        }
//
//        // Calcular el macro-promedio
//        XYSeries macroAverageSeries = new XYSeries("Macro-Average ROC");
//        List<Double> fprPoints = new ArrayList<>();
//        int j = 0;
//        for (XYSeries series : rocCurves) {
//            System.out.println("j: "+j +"/"+rocCurves.size());
//            for (int i = 0; i < series.getItemCount(); i++) {
//                double fpr = series.getX(i).doubleValue();
//                if (!fprPoints.contains(fpr)) {
//                    fprPoints.add(fpr);
//                }
//            }
//            j++;
//        }
//        Collections.sort(fprPoints);
//
//        j=0;
//        for (double fpr : fprPoints) {
//            System.out.println("j2: "+j+"/"+fprPoints.size());
//            double sumTpr = 0;
//            int count = 0;
//            for (XYSeries series : rocCurves) {
//                double tprAtFpr = 0;
//                for (int i = 0; i < series.getItemCount() - 1; i++) {
//                    double fpr1 = series.getX(i).doubleValue();
//                    double tpr1 = series.getY(i).doubleValue();
//                    double fpr2 = series.getX(i + 1).doubleValue();
//                    double tpr2 = series.getY(i + 1).doubleValue();
//
//                    if (fpr >= fpr1 && fpr <= fpr2) {
//                        if (fpr1 == fpr2) {
//                            tprAtFpr = tpr1;
//                        } else {
//                            tprAtFpr = tpr1 + (fpr - fpr1) * (tpr2 - tpr1) / (fpr2 - fpr1);
//                        }
//                        sumTpr += tprAtFpr;
//                        count++;
//                        break;
//                    }
//                }
//                if (series.getX(series.getItemCount() - 1).doubleValue() == fpr) {
//                    sumTpr += series.getY(series.getItemCount() - 1).doubleValue();
//                    count++;
//                }
//            }
//            if (count > 0) {
//                macroAverageSeries.add(fpr, sumTpr / count);
//            }
//            j++;
//        }
//
//        overallDataset.addSeries(macroAverageSeries);
//
//        JFreeChart chart = ChartFactory.createXYLineChart(
//                "Curva ROC (Macro-Promedio)",
//                "Tasa de Falsos Positivos (FPR)",
//                "Tasa de Verdaderos Positivos (TPR)",
//                overallDataset,
//                PlotOrientation.VERTICAL,
//                true,
//                true,
//                false
//        );
//
//        File rocFile = new File(filePath);
//        try {
//            ChartUtils.saveChartAsPNG(rocFile, chart, 800, 600);
//            System.out.println("Curva ROC (macro-promedio) guardada en: " + filePath);
//            return filePath;
//        } catch (IOException e) {
//            System.err.println("Error al guardar la curva ROC (macro-promedio): " + e.getMessage());
//            return null;
//        }
//    }

    public String generateMacroAverageRocCurve(float[][] predictions, float[][] trueLabels, String filePath) {
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);

        if (predictions == null || trueLabels == null ||
                predictions.length != trueLabels.length ||
                predictions[0].length != trueLabels[0].length) {
            System.err.println("Error: Formato de datos incorrecto para la curva ROC (macro-promedio).");
            return null;
        }

        int numLabels = predictions[0].length;
        List<XYSeries> rocCurves = Collections.synchronizedList(new ArrayList<>());

        // Obtener número de hilos disponibles
        int availableThreads = Math.max(1, (Runtime.getRuntime().availableProcessors() -
                ManagementFactory.getThreadMXBean().getThreadCount()) / 2);
        ExecutorService executor = Executors.newFixedThreadPool(availableThreads);

        AtomicInteger curveCounter = new AtomicInteger(0);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < numLabels; i++) {
            final int labelIdx = i;
            tasks.add(() -> {
                List<ScoreLabelPair> scoreLabelPairs = new ArrayList<>();
                for (int j = 0; j < predictions.length; j++) {
                    scoreLabelPairs.add(new ScoreLabelPair(predictions[j][labelIdx], (int) trueLabels[j][labelIdx]));
                }
                scoreLabelPairs.sort(Collections.reverseOrder());

                int nPositive = 0;
                int nNegative = 0;
                for (ScoreLabelPair pair : scoreLabelPairs) {
                    if (pair.label == 1) nPositive++;
                    else nNegative++;
                }

                XYSeries series = new XYSeries("Label " + labelIdx);
                series.add(0.0, 0.0);
                int tp = 0, fp = 0;

                for (ScoreLabelPair pair : scoreLabelPairs) {
                    if (pair.label == 1) tp++;
                    else fp++;
                    double tpr = nPositive == 0 ? 0 : (double) tp / nPositive;
                    double fpr = nNegative == 0 ? 0 : (double) fp / nNegative;
                    series.add(fpr, tpr);
                }
                series.add(1.0, 1.0);
                rocCurves.add(series);
                int count = curveCounter.incrementAndGet();
                logger.info(String.format("Curva ROC calculada: %d/%d", count, numLabels));
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error de ejecución de hilos: " + e.getMessage());
            return null;
        }

        // Recolectar todos los puntos FPR únicos
        Set<Double> fprSet = ConcurrentHashMap.newKeySet();
        rocCurves.parallelStream().forEach(series -> {
            for (int i = 0; i < series.getItemCount(); i++) {
                fprSet.add(series.getX(i).doubleValue());
            }
        });

        List<Double> fprPoints = fprSet.stream().sorted().collect(Collectors.toList());
        XYSeries macroAverageSeries = new XYSeries("Macro-Average ROC");

        // Calcular macro-promedio paralelo
        ExecutorService avgExecutor = Executors.newFixedThreadPool(availableThreads);
        AtomicInteger fprCounter = new AtomicInteger(0);
        List<Future<Map.Entry<Double, Double>>> avgTasks = new ArrayList<>();

        for (double fpr : fprPoints) {
            final double currentFpr = fpr;
            avgTasks.add(avgExecutor.submit(() -> {
                double sumTpr = 0;
                int count = 0;
                for (XYSeries series : rocCurves) {
                    double tprAtFpr = 0;
                    for (int i = 0; i < series.getItemCount() - 1; i++) {
                        double fpr1 = series.getX(i).doubleValue();
                        double tpr1 = series.getY(i).doubleValue();
                        double fpr2 = series.getX(i + 1).doubleValue();
                        double tpr2 = series.getY(i + 1).doubleValue();

                        if (currentFpr >= fpr1 && currentFpr <= fpr2) {
                            tprAtFpr = (fpr1 == fpr2) ? tpr1 :
                                    tpr1 + (currentFpr - fpr1) * (tpr2 - tpr1) / (fpr2 - fpr1);
                            sumTpr += tprAtFpr;
                            count++;
                            break;
                        }
                    }
                    if (series.getX(series.getItemCount() - 1).doubleValue() == currentFpr) {
                        sumTpr += series.getY(series.getItemCount() - 1).doubleValue();
                        count++;
                    }
                }
                int done = fprCounter.incrementAndGet();
                logger.info(String.format("TPR promediado en FPR: %d/%d", done, fprPoints.size()));
                return Map.entry(currentFpr, count > 0 ? sumTpr / count : 0.0);
            }));
        }

        avgExecutor.shutdown();
        for (Future<Map.Entry<Double, Double>> future : avgTasks) {
            try {
                Map.Entry<Double, Double> entry = future.get();
                macroAverageSeries.add(entry.getKey(), entry.getValue());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error calculando el macro-promedio: " + e.getMessage());
                return null;
            }
        }

        XYSeriesCollection overallDataset = new XYSeriesCollection();
        overallDataset.addSeries(macroAverageSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Curva ROC (Macro-Promedio)",
                "Tasa de Falsos Positivos (FPR)",
                "Tasa de Verdaderos Positivos (TPR)",
                overallDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        File rocFile = new File(filePath);
        try {
            ChartUtils.saveChartAsPNG(rocFile, chart, 800, 600);
            return filePath;
        } catch (IOException e) {
            System.err.println("Error al guardar la curva ROC (macro-promedio): " + e.getMessage());
            return null;
        }
    }

    public String generateRocCurvesPerLabel(float[][] predictions, float[][] trueLabels, String outputDir) {
        if (predictions == null || trueLabels == null || predictions.length != trueLabels.length || predictions[0].length != trueLabels[0].length) {
            System.err.println("Error: Formato de datos incorrecto para las curvas ROC por etiqueta.");
            return null;
        }

        File dir = new File(outputDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Error al crear el directorio de salida: " + outputDir);
                return null;
            }
        }

        int numLabels = predictions[0].length;
        for (int i = 0; i < numLabels; i++) {
            List<ScoreLabelPair> scoreLabelPairs = new ArrayList<>();
            for (int j = 0; j < predictions.length; j++) {
                scoreLabelPairs.add(new ScoreLabelPair(predictions[j][i], (int) trueLabels[j][i]));
            }
            Collections.sort(scoreLabelPairs, Collections.reverseOrder());

            int nPositive = 0;
            int nNegative = 0;
            for (ScoreLabelPair pair : scoreLabelPairs) {
                if (pair.label == 1) {
                    nPositive++;
                } else {
                    nNegative++;
                }
            }

            XYSeries series = new XYSeries("Label " + i);
            series.add(0.0, 0.0);
            int tp = 0;
            int fp = 0;
            for (ScoreLabelPair pair : scoreLabelPairs) {
                if (pair.label == 1) {
                    tp++;
                } else {
                    fp++;
                }
                double tpr = nPositive == 0 ? 0 : (double) tp / nPositive;
                double fpr = nNegative == 0 ? 0 : (double) fp / nNegative;
                series.add(fpr, tpr);
            }
            series.add(1.0, 1.0);

            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(series);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Curva ROC - Etiqueta " + i,
                    "Tasa de Falsos Positivos (FPR)",
                    "Tasa de Verdaderos Positivos (TPR)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            File rocFile = new File(outputDir, "roc_label_" + i + ".png");
            try {
                ChartUtils.saveChartAsPNG(rocFile, chart, 800, 600);
//                System.out.println("Curva ROC para Etiqueta " + i + " guardada en: " + rocFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error al guardar la curva ROC para la Etiqueta " + i + ": " + e.getMessage());
            }
        }
        return outputDir;
    }

    public String generateTopNRocCurves(float[][] predictions, float[][] trueLabels, int n, String filePath) {
        if (predictions == null || trueLabels == null || predictions.length != trueLabels.length || predictions[0].length != trueLabels[0].length) {
            System.err.println("Error: Formato de datos incorrecto para las curvas ROC de las N etiquetas principales.");
            return null;
        }

        int numLabels = predictions[0].length;
        Map<Integer, Integer> positiveCounts = new HashMap<>();
        for (int i = 0; i < numLabels; i++) {
            int count = 0;
            for (int j = 0; j < trueLabels.length; j++) {
                if ((int) trueLabels[j][i] == 1) {
                    count++;
                }
            }
            positiveCounts.put(i, count);
        }

        List<Map.Entry<Integer, Integer>> sortedLabels = positiveCounts.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());

        XYSeriesCollection overallDataset = new XYSeriesCollection();

        for (Map.Entry<Integer, Integer> labelEntry : sortedLabels) {
            int labelIndex = labelEntry.getKey();
            List<ScoreLabelPair> scoreLabelPairs = new ArrayList<>();
            for (int j = 0; j < predictions.length; j++) {
                scoreLabelPairs.add(new ScoreLabelPair(predictions[j][labelIndex], (int) trueLabels[j][labelIndex]));
            }
            Collections.sort(scoreLabelPairs, Collections.reverseOrder());

            int nPositive = 0;
            int nNegative = 0;
            for (ScoreLabelPair pair : scoreLabelPairs) {
                if (pair.label == 1) {
                    nPositive++;
                } else {
                    nNegative++;
                }
            }

            XYSeries series = new XYSeries("Label " + labelIndex);
            series.add(0.0, 0.0);
            int tp = 0;
            int fp = 0;
            for (ScoreLabelPair pair : scoreLabelPairs) {
                if (pair.label == 1) {
                    tp++;
                } else {
                    fp++;
                }
                double tpr = nPositive == 0 ? 0 : (double) tp / nPositive;
                double fpr = nNegative == 0 ? 0 : (double) fp / nNegative;
                series.add(fpr, tpr);
            }
            series.add(1.0, 1.0);
            overallDataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Curvas ROC de las " + n + " Etiquetas Principales",
                "Tasa de Falsos Positivos (FPR)",
                "Tasa de Verdaderos Positivos (TPR)",
                overallDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        File rocFile = new File(filePath);
        try {
            ChartUtils.saveChartAsPNG(rocFile, chart, 800, 600);
            System.out.println("Curvas ROC de las " + n + " etiquetas principales guardadas en: " + filePath);
            return filePath;
        } catch (IOException e) {
            System.err.println("Error al guardar las curvas ROC de las " + n + " etiquetas principales: " + e.getMessage());
            return null;
        }
    }

    public double calculatePrecision(float[][] predictions, float[][] trueLabels) {
        if (!areValidDimensions(predictions, trueLabels)) return 0.0;
        int truePositives = 0;
        int falsePositives = 0;
        for (int i = 0; i < predictions.length; i++) {
            for (int j = 0; j < predictions[i].length; j++) {
                if (predictions[i][j] > 0.5 && trueLabels[i][j] == 1) {
                    truePositives++;
                } else if (predictions[i][j] > 0.5 && trueLabels[i][j] == 0) {
                    falsePositives++;
                }
            }
        }
        return (double) truePositives / (truePositives + falsePositives + 1e-9); // Evitar división por cero
    }

    public double calculateRecall(float[][] predictions, float[][] trueLabels) {
        if (!areValidDimensions(predictions, trueLabels)) return 0.0;
        int truePositives = 0;
        int falseNegatives = 0;
        for (int i = 0; i < predictions.length; i++) {
            for (int j = 0; j < predictions[i].length; j++) {
                if (predictions[i][j] > 0.5 && trueLabels[i][j] == 1) {
                    truePositives++;
                } else if (predictions[i][j] <= 0.5 && trueLabels[i][j] == 1) {
                    falseNegatives++;
                }
            }
        }
        return (double) truePositives / (truePositives + falseNegatives + 1e-9); // Evitar división por cero
    }

    public double calculateF1Score(float[][] predictions, float[][] trueLabels) {
        double precision = calculatePrecision(predictions, trueLabels);
        double recall = calculateRecall(predictions, trueLabels);
        return 2 * (precision * recall) / (precision + recall + 1e-9); // Evitar división por cero
    }

    public double calculateAccuracy(float[][] predictions, float[][] trueLabels) {
        if (!areValidDimensions(predictions, trueLabels)) return 0.0;
        int correctPredictions = 0;
        int totalPredictions = 0;
        for (int i = 0; i < predictions.length; i++) {
            for (int j = 0; j < predictions[i].length; j++) {
                totalPredictions++;
                // Consideramos una predicción correcta si coincide con la etiqueta (aplicando un umbral de 0.5)
                boolean predictedPositive = predictions[i][j] > 0.5;
                boolean actualPositive = trueLabels[i][j] == 1;
                if (predictedPositive == actualPositive) {
                    correctPredictions++;
                }
            }
        }
        return (double) correctPredictions / totalPredictions;
    }

    private boolean areValidDimensions(float[][] predictions, float[][] trueLabels) {
        if (predictions == null || trueLabels == null || predictions.length != trueLabels.length || (predictions.length > 0 && predictions[0].length != trueLabels[0].length)) {
            System.err.println("Error: Dimensiones de las predicciones y las etiquetas no válidas.");
            return false;
        }
        return true;
    }

    // Clase interna para ayudar con el cálculo de la ROC
    private static class ScoreLabelPair implements Comparable<ScoreLabelPair> {
        double score;
        int label;

        public ScoreLabelPair(double score, int label) {
            this.score = score;
            this.label = label;
        }

        @Override
        public int compareTo(ScoreLabelPair other) {
            return Double.compare(this.score, other.score);
        }
    }

    public String generateAucHeatmap(float[][] predictions, float[][] trueLabels, String filePath) {
        if (predictions == null || trueLabels == null || predictions.length != trueLabels.length || predictions[0].length != trueLabels[0].length) {
            System.err.println("Error: Formato de datos incorrecto para el mapa de calor AUC.");
            return null;
        }

        int numLabels = predictions[0].length;
        double[] aucValues = new double[numLabels];

        System.out.println("--- AUC por Etiqueta ---");
        for (int i = 0; i < numLabels; i++) {
            List<ScoreLabelPair> scoreLabelPairs = new ArrayList<>();
            for (int j = 0; j < predictions.length; j++) {
                scoreLabelPairs.add(new ScoreLabelPair(predictions[j][i], (int) trueLabels[j][i]));
            }

            Collections.sort(scoreLabelPairs, Collections.reverseOrder());

            double auc = calculateAuc(scoreLabelPairs);
            aucValues[i] = auc;
//            System.out.println("Etiqueta " + i + ": AUC = " + String.format("%.4f", auc));
        }
        RPreprocessor rPreprocessor = new RPreprocessor();
        // Enviar los valores AUC a R
        rPreprocessor.ejecutarComando("auc_values <- c(" + Arrays.toString(aucValues).replaceAll("[\\[\\]]", "") + ")");
        rPreprocessor.ejecutarComando("auc_values");
        rPreprocessor.ejecutarComando("num_labels <- " + numLabels);
        rPreprocessor.ejecutarComando("file_path <- '" + filePath.replace("\\", "/") + "'"); // Adaptar la ruta para R
        rPreprocessor.ejecutarComando("getwd()");
        rPreprocessor.ejecutarComando("setwd(\""+(new File("").getAbsolutePath().replace("\\","/"))+"\")");
        rPreprocessor.ejecutarComando("getwd()");

        // Ejecutar un script R para generar el mapa de calor
        rPreprocessor.ejecutarScript("src/main/resources/scripts_r/auc_heatmap_script.R");

        rPreprocessor.cerrarConexion();
        System.out.println("Mapa de calor AUC generado en R y guardado en: " + filePath);
        return filePath;
    }
    private double calculateAuc(List<ScoreLabelPair> scoreLabelPairs) {
        double auc = 0.0;
        int tp = 0;
        int fp = 0;
        int nPositive = 0;
        int nNegative = 0;

        for (ScoreLabelPair pair : scoreLabelPairs) {
            if (pair.label == 1) {
                nPositive++;
            } else {
                nNegative++;
            }
        }

        if (nPositive > 0 && nNegative > 0) {
            double prevFpr = 0.0;
            double prevTpr = 0.0;

            for (int i = 0; i < scoreLabelPairs.size(); i++) {
                if (i > 0 && scoreLabelPairs.get(i).score != scoreLabelPairs.get(i - 1).score) {
                    double currentFpr = (double) fp / nNegative;
                    double currentTpr = (double) tp / nPositive;
                    auc += (currentFpr - prevFpr) * (prevTpr + currentTpr) / 2.0;
                    prevFpr = currentFpr;
                    prevTpr = currentTpr;
                }
                if (scoreLabelPairs.get(i).label == 1) {
                    tp++;
                } else {
                    fp++;
                }
            }
            auc += (1.0 - prevFpr) * (prevTpr + 1.0) / 2.0; // Añadir el último segmento hasta (1,1) - Corrección
        }

        return auc;
    }
}
