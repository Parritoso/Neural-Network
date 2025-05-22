package com.bioinformatica;

import com.bioinformatica.function_prediction.Neural_network.CudaChecker.CudaChecker;
import com.bioinformatica.function_prediction.Neural_network.facade.NeuralNetworkFacade;
import com.bioinformatica.function_prediction.Neural_network.matrix.GPUMatrixBackend;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;
import com.bioinformatica.function_prediction.Neural_network.model.NetworkSaverLoader;
import com.bioinformatica.function_prediction.Neural_network.networkAnalyzer.NetworkAnalysisFacade;
import com.bioinformatica.function_prediction.Neural_network.networkAnalyzer.NetworkAnalyzer;
import com.bioinformatica.function_prediction.Neural_network.networkAnalyzer.ReportGenerator;
import com.bioinformatica.function_prediction.Neural_network.optimizer.HyperparameterOptimizer;
import com.bioinformatica.function_prediction.Neural_network.optimizer.ThresholdAdjuster;
import com.bioinformatica.load.LoaderManager;
import com.bioinformatica.preprocessing.integration.EnvironmentChecker.REnvironmentChecker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static boolean R = true;
    public static void main(String[] args) throws Exception {
//        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
//        // to see how IntelliJ IDEA suggests fixing it.
//        System.out.printf("Hello and welcome!");
//
//        for (int i = 1; i <= 5; i++) {
//            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
//            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
//            System.out.println("i = " + i);
//        }

//        boolean CUDA = CudaChecker.checkCUDAAvailability();
//        MatrixFactory.setUseGPU(CUDA);
//        NeuralNetworkFacade facade = new NeuralNetworkFacade();
//        int inputSize = 10;
//        int outputSize = 14885;
        /*int numSamples = 1000;
        int epochs = 10;
        int batchSize = 32;

        // Generar datos de entrenamiento aleatorios para probar
        float[][] trainingData = new float[numSamples][inputSize];
        float[][] trainingLabels = new float[numSamples][outputSize];
        Random random = new Random();
        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < inputSize; j++) {
                trainingData[i][j] = random.nextFloat();
            }
            // Generar etiquetas binarias aleatorias
            for (int j = 0; j < outputSize; j++) {
                trainingLabels[i][j] = random.nextFloat() > 0.5 ? 1.0f : 0.0f;
            }
        }

        // Generar datos de prueba aleatorios
        int numTestSamples = 200;
        float[][] testData = new float[numTestSamples][inputSize];
        float[][] testLabels = new float[numTestSamples][outputSize];
        for (int i = 0; i < numTestSamples; i++) {
            for (int j = 0; j < inputSize; j++) {
                testData[i][j] = random.nextFloat();
            }
            for (int j = 0; j < outputSize; j++) {
                testLabels[i][j] = random.nextFloat() > 0.5 ? 1.0f : 0.0f;
            }
        }

        facade.buildNetwork(inputSize, outputSize);
        //facade.trainNetwork(trainingData, trainingLabels, epochs, batchSize);

        float[][] predictions = facade.predict(testData);
        System.out.println("Pérdida en el conjunto de prueba: " + facade.evaluateNetwork(testData, testLabels));

        // Puedes imprimir algunas predicciones para ver el formato
         System.out.println("Ejemplo de predicciones:");
         for (int i = 0; i < 5; i++) {
             System.out.println(Arrays.toString(predictions[i]));
         }

        facade.saveNetwork();*/
//        facade.buildNetwork(inputSize, outputSize);
//        float[] unaSolaMuestra = new float[10];
//        Random random = new Random();
//        for (int i = 0; i < 10; i++) {
//            unaSolaMuestra[i] = random.nextFloat(); // Genera un número aleatorio entre 0.0 y 1.0
//        }
//        System.out.println("Muestra generada: " + Arrays.toString(unaSolaMuestra));
//        // 2. Crea una matriz para esta única muestra (forma [numCaracteristicas, 1])
//        float[][] inputParaPrediccion = new float[10][1];
//        for (int i = 0; i < 10; i++) {
//            inputParaPrediccion[i][0] = unaSolaMuestra[i];
//        }
//        Matrix inputMatrixPrediccion = MatrixFactory.createMatrix(inputParaPrediccion);//new GPUMatrixBackend(inputParaPrediccion);
//
//        float[][] resultados = facade.predict((inputMatrixPrediccion/*.transpose()*/).toFloatMatrix());
//
//        System.out.println("Results: ["+resultados.length+", "+resultados[0].length+"]");
//        // Como solo predices una muestra, 'resultados' debería tener solo una fila.
//        if (resultados.length > 0) {
//            float[] predicciones = resultados[0];
//            for (int i = 0; i < predicciones.length; i++) {
//                System.out.println("Etiqueta " + i + ": " + predicciones[i]);
//            }
//        } else {
//            System.out.println("No se obtuvieron predicciones.");
//        }

        CrearCarpeta();

        boolean CUDA = CudaChecker.checkCUDAAvailability();
        REnvironmentChecker.checkEnvironment(false);
        MatrixFactory.setUseGPU(CUDA);
        NeuralNetworkFacade networkFacade = new NeuralNetworkFacade();
        NetworkAnalysisFacade analysisFacade = new NetworkAnalysisFacade("gene_function_map.csv"); // Asegúrate de tener este archivo (puede ser vacío para la prueba)
        LoaderManager loaderManager = new LoaderManager();
        Random random = new Random();
        int epochs = 10;
        int batchSize = 32;

        // --- 1. Configuración del Modelo ---
        int inputSize = 10;
        int outputSize = 14886;

        loaderManager.obtenerDatos("train_dataset.csv");
        float[][] trainingData = loaderManager.getTrainingData();
        float[][] trainingLabels = loaderManager.getTrainingLabels();
        loaderManager.obtenerDatos("test_dataset.csv");
        float[][] testData = loaderManager.getTrainingData();
        float[][] testLabels = loaderManager.getTrainingLabels();

//        HyperparameterOptimizer optimizer = new HyperparameterOptimizer(MatrixFactory.createMatrix(trainingData), MatrixFactory.createMatrix(trainingLabels), MatrixFactory.createMatrix(testData), MatrixFactory.createMatrix(testLabels), 10);
//        optimizer.search();
//
//        double bestLR = optimizer.getBestLearningRate();
//        double bestDropoutRate = optimizer.getBestDropoutRate();
//        double bestGradientClipThreshold = optimizer.getBestGradientClipThreshold();
//        double bestMaxNorm = optimizer.getBestMaxNorm();
//        int bestBS = optimizer.getBestBatchSize();
//        int bestEpochs = optimizer.getBestEpochs();

        double bestLR = 0.00726490839920298;
        double bestDropoutRate = 0.005363277515049891;
        double bestGradientClipThreshold = 0.8454536290841791;
        double bestMaxNorm = 3.9623102953009037;
        int bestEpochs = 5;
        int bestBS = 64;

        networkFacade.buildNetwork(inputSize, outputSize, bestLR,bestDropoutRate,bestGradientClipThreshold,bestMaxNorm);

        // --- 2. Generación de Datos de Entrenamiento Aleatorios ---
//        float[][] trainingData = new float[numSamples][inputSize];
//        float[][] trainingLabels = new float[numSamples][outputSize];
//        for (int i = 0; i < numSamples; i++) {
//            for (int j = 0; j < inputSize; j++) {
//                trainingData[i][j] = random.nextFloat();
//            }
//            // Generar etiquetas binarias aleatorias
//            for (int j = 0; j < outputSize; j++) {
//                trainingLabels[i][j] = random.nextFloat() > 0.5 ? 1.0f : 0.0f;
//            }
//        }
//
//        // Generar datos de prueba aleatorios
//        int numTestSamples = 200;
//        float[][] testData = new float[numTestSamples][inputSize];
//        float[][] testLabels = new float[numTestSamples][outputSize];
//        for (int i = 0; i < numTestSamples; i++) {
//            for (int j = 0; j < inputSize; j++) {
//                testData[i][j] = random.nextFloat();
//            }
//            for (int j = 0; j < outputSize; j++) {
//                testLabels[i][j] = random.nextFloat() > 0.5 ? 1.0f : 0.0f;
//            }
//        }

//        float[][] trainingData = loaderManager.obtenerTrainingData("train_dataset.csv");
//        float[][] trainingLabels = loaderManager.obtenerTrainingLabels("train_dataset.csv");
//        float[][] testData = loaderManager.obtenerTrainingData("test_dataset.csv");
//        float[][] testLabels = loaderManager.obtenerTrainingLabels("test_dataset.csv");


//        // --- 3. Entrenamiento Simulado ---
//
//        networkFacade.trainNetwork(trainingData, trainingLabels, /*epochs, batchSize*/ bestEpochs, bestBS);
//
//        String trainingReportPath = Paths.get("resources", "training_report_integration_test").toString();
//        analysisFacade.processTrainingResults(networkFacade.getLossHistory(), networkFacade.getTrainingTime(), networkFacade.getHyperparameters(), trainingReportPath, trainingData, trainingLabels);
//        System.out.println("Reporte de entrenamiento generado en: " + trainingReportPath + ".pdf y " + trainingReportPath + ".docx");
//
//        networkFacade.saveNetwork(true);

        // 3. Ajusta umbrales usando validación
//        ThresholdAdjuster adjuster = networkFacade.fitThresholds(testData, testLabels);
        // --- 4. Generación de Datos de Predicción y Etiquetas Verdaderas Aleatorios para Evaluación ---
        float[][] predictions = networkFacade.predict(testData);
//        adjuster.apply(MatrixFactory.createMatrix(predictions));

        String rocFilePath = Paths.get("resources","roc_curve_integration_test.png").toString();
        String rocMacroFilePath = Paths.get("resources","roc_curve_macro_integration_test.png").toString();
        String rocTopNFilePath = Paths.get("resources","roc_curve_topn_integration_test.png").toString();
        String AUCFilePath = Paths.get("resources","auc_heatmap_integration_test.png").toString();
        analysisFacade.evaluateModel(predictions, testLabels, rocFilePath,rocMacroFilePath,rocTopNFilePath,AUCFilePath);
        System.out.println("Reporte de evaluación generado en: evaluation_report_integration_test.pdf y roc_curve_integration_test.png");

        // --- 5. Predicción con una Sola Muestra Aleatoria ---
        float[] unaSolaMuestra = new float[inputSize];
        for (int i = 0; i < inputSize; i++) {
            unaSolaMuestra[i] = random.nextFloat();
        }
        Matrix inputMatrixPrediccion = MatrixFactory.createMatrix(new float[][]{unaSolaMuestra}).transpose();
        float[][] resultadosPrediccion = networkFacade.predictone(inputMatrixPrediccion.toFloatMatrix());

        System.out.println("\nPredicciones para la muestra: " + Arrays.toString(unaSolaMuestra));
        if (resultadosPrediccion.length > 0) {
            Map<String, Double> predictionsMap = analysisFacade.interpretPredictions(resultadosPrediccion[0]);
            System.out.println("Interpretación de las predicciones: " + predictionsMap);
            String predictionReportPath = Paths.get("resources","prediction_report_integration_test").toString();
            analysisFacade.savePredictionReports(predictionsMap, predictionReportPath);
            System.out.println("Reporte de predicciones guardado en: " + predictionReportPath + "_prediction.txt y " + predictionReportPath + "_table.pdf");
        } else {
            System.out.println("No se obtuvieron predicciones.");
        }

        System.out.println("\n--- Prueba de Integración Completa Finalizada ---");
    }

    public static void CrearCarpeta() {
        String nombreCarpeta = "resources"; // Define el nombre de la carpeta

        File directorio = new File(nombreCarpeta);

        if (!directorio.exists()) {
            if (directorio.mkdir()) {
                System.out.println("Carpeta creada exitosamente en: " + directorio.getAbsolutePath());
            } else {
                System.err.println("Error al intentar crear la carpeta.");
            }
        } else {
            System.out.println("La carpeta '" + nombreCarpeta + "' ya existe en: " + directorio.getAbsolutePath());
        }
    }
}