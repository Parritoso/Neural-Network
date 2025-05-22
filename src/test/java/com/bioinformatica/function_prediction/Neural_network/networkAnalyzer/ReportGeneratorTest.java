package com.bioinformatica.function_prediction.Neural_network.networkAnalyzer;

import com.bioinformatica.Main;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportGeneratorTest {
    private final ReportGenerator reportGenerator = new ReportGenerator();
    private final NetworkAnalyzer networkAnalyzer = new NetworkAnalyzer();
    private final Random random = new Random();

    private float[][] generateRandomFloatMatrix(int rows, int cols) {
        float[][] matrix = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextFloat();
            }
        }
        return matrix;
    }

    private float[][] generateBinaryFloatMatrix(int rows, int cols) {
        float[][] matrix = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(2);
            }
        }
        return matrix;
    }

//    @Test
//    void testGenerateTrainingReport_pdfFileCreated() throws IOException {
//        List<Double> lossHistory = Arrays.asList(0.5, 0.4, 0.3);
//        long trainingTime = 120000; // 2 minutos en milisegundos
//        Map<String, Object> hyperparameters = new HashMap<>();
//        hyperparameters.put("learningRate", 0.01f);
//        String filePath = "src/test/resources/test_training_report";
//
//        reportGenerator.generateTrainingReport(lossHistory, trainingTime, hyperparameters, filePath);
//
//        File pdfFile = new File(filePath + ".pdf");
//        assertTrue(pdfFile.exists());
//
//        // Limpiar el archivo después de la prueba
////        pdfFile.delete();
////        new File(filePath + ".docx").delete();
//    }
//
//    // Puedes escribir tests similares para verificar el contenido de los archivos
//    // usando las APIs de PDFBox y POI para leerlos.
//
//    @Test
//    void testGeneratePredictionTableReport_docxFileCreated() throws IOException {
//        Map<String, Double> predictions = new HashMap<>();
//        predictions.put("geneA", 0.8);
//        predictions.put("geneB", 0.2);
//        String filePath = "src/test/resources/test_prediction_table";
//
//        reportGenerator.generatePredictionTableReport(predictions, filePath);
//
//        File docxFile = new File(filePath + ".docx");
//        assertTrue(docxFile.exists());
//
//        // Limpiar el archivo después de la prueba
////        docxFile.delete();
////        new File(filePath + ".pdf").delete();
//    }
@Test
void testGenerateTrainingReport_pdfFileCreatedAndContainsData() throws IOException {
    List<Double> lossHistory = Arrays.asList(0.5, 0.4, 0.3);
    long trainingTime = 120000; // 2 minutos en milisegundos
    Map<String, Object> hyperparameters = new HashMap<>();
    hyperparameters.put("learningRate", 0.01f);
    String filePath = "src/test/resources/test_training_report";

    float[][] predictions = generateRandomFloatMatrix(100, 2);
    float[][] trueLabels = generateBinaryFloatMatrix(100, 2);
    reportGenerator.generateTrainingReport(lossHistory, trainingTime, hyperparameters, filePath,predictions,trueLabels);

    File pdfFile = new File(filePath + ".pdf");
    assertTrue(pdfFile.exists());

    // Limpiar el archivo después de la prueba
//    pdfFile.delete();
//    new File(filePath + ".docx").delete();
}

    @Test
    void testGenerateTrainingReport_docxFileCreatedAndContainsData() throws IOException {
        List<Double> lossHistory = Arrays.asList(0.5, 0.4, 0.3);
        long trainingTime = 120000; // 2 minutos en milisegundos
        Map<String, Object> hyperparameters = new HashMap<>();
        hyperparameters.put("learningRate", 0.01f);
        String filePath = "src/test/resources/test_training_report";
        float[][] predictions = generateRandomFloatMatrix(100, 2);
        float[][] trueLabels = generateBinaryFloatMatrix(100, 2);
        reportGenerator.generateTrainingReport(lossHistory, trainingTime, hyperparameters, filePath,predictions,trueLabels);

        File docxFile = new File(filePath + ".docx");
        assertTrue(docxFile.exists());


        // Limpiar el archivo después de la prueba
//        docxFile.delete();
//        new File(filePath + ".pdf").delete();
    }

    @Test
    void testGeneratePredictionTableReport_pdfFileCreatedAndContainsData() throws IOException {
        Map<String, Double> predictions = new HashMap<>();
        predictions.put("geneA", 0.8);
        predictions.put("geneB", 0.2);
        String filePath = "src/test/resources/test_prediction_table";

        reportGenerator.generatePredictionTableReport(predictions, filePath);

        File pdfFile = new File(filePath + ".pdf");
        assertTrue(pdfFile.exists());


        // Limpiar el archivo después de la prueba
//        pdfFile.delete();
//        new File(filePath + ".docx").delete();
    }

    @Test
    void testGeneratePredictionTableReport_docxFileCreatedAndContainsData() throws IOException {
        Map<String, Double> predictions = new HashMap<>();
        predictions.put("geneA", 0.8);
        predictions.put("geneB", 0.2);
        String filePath = "src/test/resources/test_prediction_table";

        reportGenerator.generatePredictionTableReport(predictions, filePath);

        File docxFile = new File(filePath + ".docx");
        assertTrue(docxFile.exists());


        // Limpiar el archivo después de la prueba
//        docxFile.delete();
//        new File(filePath + ".pdf").delete();
    }

    @Test
    void testGenerateEvaluationReport_pdfFileCreatedAndContainsData() throws IOException {
        double precision = 0.95;
        double recall = 0.90;
        double f1 = 0.92;
        double accuracy = 0.93;
        String filePath = "src/test/resources/test_evaluation_report";
        String rocFilePath = filePath + "_roc.png";
        String rocMacroFilePath = filePath + "_roc_macro.png";
        String rocTopNFilepath = filePath + "_roc_top.png";

        // Generar datos de predicción y etiquetas simulados para la curva ROC
        float[][] predictions = generateRandomFloatMatrix(100, 2);
        float[][] trueLabels = generateBinaryFloatMatrix(100, 2);

        // Generar la curva ROC y obtener la ruta del archivo
        String generatedRocFilePath = networkAnalyzer.generateRocCurve(predictions, trueLabels, rocFilePath);
        String generatedRocMacroFilePath = networkAnalyzer.generateMacroAverageRocCurve(predictions,trueLabels,rocMacroFilePath);
        String generatedRocTopFilePath = networkAnalyzer.generateTopNRocCurves(predictions,trueLabels,5,rocTopNFilepath);

        // Verificar que la generación de la curva ROC fue exitosa
        assertTrue(generatedRocFilePath != null && new File(generatedRocFilePath).exists());
        Main.R = false;

        reportGenerator.generateEvaluationReport(precision, recall, f1, accuracy, filePath,rocFilePath,rocMacroFilePath,rocTopNFilepath,"null");

        File pdfFile = new File(filePath + ".pdf");
        assertTrue(pdfFile.exists());

        // Limpiar el archivo después de la prueba
//        pdfFile.delete();
//        new File(filePath + ".docx").delete();
    }

    @Test
    void testGenerateEvaluationReport_docxFileCreatedAndContainsData() throws IOException {
        double precision = 0.95;
        double recall = 0.90;
        double f1 = 0.92;
        double accuracy = 0.93;
        String filePath = "src/test/resources/test_evaluation_report";
        String rocFilePath = filePath + "_roc.png";
        String rocMacroFilePath = filePath + "_roc_macro.png";
        String rocTopNFilepath = filePath + "_roc_top.png";

        // Generar datos de predicción y etiquetas simulados para la curva ROC
        float[][] predictions = generateRandomFloatMatrix(100, 2);
        float[][] trueLabels = generateBinaryFloatMatrix(100, 2);

        // Generar la curva ROC y obtener la ruta del archivo
        String generatedRocFilePath = networkAnalyzer.generateRocCurve(predictions, trueLabels, rocFilePath);
        String generatedRocMacroFilePath = networkAnalyzer.generateMacroAverageRocCurve(predictions,trueLabels,rocMacroFilePath);
        String generatedRocTopFilePath = networkAnalyzer.generateTopNRocCurves(predictions,trueLabels,5,rocTopNFilepath);

        // Verificar que la generación de la curva ROC fue exitosa
        assertTrue(generatedRocFilePath != null && new File(generatedRocFilePath).exists());
        Main.R = false;

        reportGenerator.generateEvaluationReport(precision, recall, f1, accuracy, filePath,rocFilePath,rocMacroFilePath,rocTopNFilepath,"null");

        File docxFile = new File(filePath + ".docx");
        assertTrue(docxFile.exists());

        // Limpiar el archivo después de la prueba
//        docxFile.delete();
//        new File(filePath + ".pdf").delete();
    }
}
