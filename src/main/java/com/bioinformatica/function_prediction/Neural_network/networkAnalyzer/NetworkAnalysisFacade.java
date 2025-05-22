package com.bioinformatica.function_prediction.Neural_network.networkAnalyzer;

import com.bioinformatica.Main;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class NetworkAnalysisFacade {
    private final NetworkAnalyzer analyzer;
    private final ReportGenerator reporter;
    private final PredictionInterpreter interpreter;
    private String reportPath;

    public NetworkAnalysisFacade(String geneFunctionMapFile) {
        this.analyzer = new NetworkAnalyzer();
        this.reporter = new ReportGenerator();
        this.interpreter = new PredictionInterpreter(Paths.get("resources",geneFunctionMapFile).toString());
        this.reportPath = Paths.get("resources","evaluation_report_integration_test").toString();
    }

    public void processTrainingResults(List<Double> lossHistory, long trainingTime, Map<String, Object> hyperparameters, String reportPath,float[][] trainingData, float[][] trainingLabels) {
        this.reportPath = reportPath;
        analyzer.calculateLossHistory(lossHistory);
        analyzer.calculateTrainingTime(trainingTime);
        analyzer.displayHyperparameters(hyperparameters);
        reporter.generateTrainingReport(lossHistory, trainingTime, hyperparameters, reportPath,trainingData,trainingLabels);
    }

    public void evaluateModel(float[][] predictions, float[][] trueLabels, String rocFilePath,String rocMacroFilePath, String topNRocFilePath ,String AUCFilePath) {
//        rocFilePath = analyzer.generateRocCurve(predictions, trueLabels, rocFilePath);
//        if(Main.R){
//            analyzer.generateAucHeatmap(predictions,trueLabels,AUCFilePath);
//        }
        System.out.println("Se procede a iniciar la creación del Macro");
        rocMacroFilePath = analyzer.generateMacroAverageRocCurve(predictions,trueLabels,rocMacroFilePath);
        System.out.println("Se procede a iniciar la creación de las topN");
        topNRocFilePath = analyzer.generateTopNRocCurves(predictions,trueLabels,5,topNRocFilePath);

        System.out.println("Se procede a iniciar la creación de las curvas por Label");
        analyzer.generateRocCurvesPerLabel(predictions,trueLabels,Paths.get("resources","RocCurvesPerLabel").toString());
        double precision = analyzer.calculatePrecision(predictions, trueLabels);
        double recall = analyzer.calculateRecall(predictions, trueLabels);
        double f1 = analyzer.calculateF1Score(predictions, trueLabels);
        double accuracy = analyzer.calculateAccuracy(predictions, trueLabels);

        System.out.println("--- Evaluación del Modelo ---");
        System.out.println("Precisión: " + String.format("%.4f", precision));
        System.out.println("Recall: " + String.format("%.4f", recall));
        System.out.println("F1-Score: " + String.format("%.4f", f1));
        System.out.println("Exactitud: " + String.format("%.4f", accuracy));
        System.out.println("----------------------------");

        // Podrías pasar estas métricas al ReportGenerator para incluirlas en un informe.
        reporter.generateEvaluationReport(precision, recall, f1, accuracy, reportPath,rocFilePath, rocMacroFilePath, topNRocFilePath,AUCFilePath);
    }

    public Map<String, Double> interpretPredictions(float[] predictionArray) {
        return interpreter.interpretPrediction(predictionArray);
    }

    public void savePredictionReports(Map<String, Double> predictions, String baseFilePath) {
        interpreter.savePredictionReport(predictions, baseFilePath);
        reporter.generatePredictionTableReport(predictions, baseFilePath + "_table.pdf"); // Ejemplo de guardar la tabla en PDF
    }

    public void setPredictionThreshold(double threshold) {
        interpreter.setProbabilityThreshold(threshold);
    }

    public void generateEvaluationReport(double precision, double recall, double f1, double accuracy, String reportPath,String rocFilePath,String rocMacroFilePath,String topNFilePath,String AUCFilePath) {
        reporter.generateEvaluationReport(precision, recall, f1, accuracy, reportPath,rocFilePath,rocMacroFilePath,topNFilePath,AUCFilePath);
    }
}
