package com.bioinformatica.function_prediction.Neural_network.model;

import com.bioinformatica.function_prediction.Neural_network.layer.Activation;
import com.bioinformatica.function_prediction.Neural_network.layer.DenseLayer;
import com.bioinformatica.function_prediction.Neural_network.loss.BinaryCrossEntropy;
import com.bioinformatica.function_prediction.Neural_network.loss.LossFunction;
import com.bioinformatica.function_prediction.Neural_network.matrix.GPUMatrixBackend;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;
import com.bioinformatica.function_prediction.Neural_network.optimizer.Adam;
import com.bioinformatica.function_prediction.Neural_network.optimizer.Optimizer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NetworkSaverLoader {

    private static final String FILENAME = Paths.get("resources","neural_network_data.txt").toString();
    private static final String METADATA_FILENAME = Paths.get("resources","neural_network_metadata.txt").toString();

    public static void saveNetwork(NeuralNetwork network, long trainingTime, List<Double> lossHistory, int epochs, int batchSize){
        try(BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(METADATA_FILENAME))){
            // --- Guardar Metadatos ---
            metadataWriter.write(String.valueOf(trainingTime));
            metadataWriter.newLine();
            metadataWriter.write(String.valueOf(epochs));
            metadataWriter.newLine();
            metadataWriter.write(String.valueOf(batchSize));
            metadataWriter.newLine();
            metadataWriter.write(String.valueOf(lossHistory.size())); // Número de elementos en el historial
            metadataWriter.newLine();
            for (Double loss : lossHistory) {
                metadataWriter.write(String.valueOf(loss));
                metadataWriter.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar los metadatos: " + e.getMessage());
        }
        saveNetwork(network);
    }
    public static void saveNetwork(NeuralNetwork network) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            List<DenseLayer> layers = network.getLayers();
            writer.write(String.valueOf(layers.size())); // Guardar el número de capas
            writer.newLine();
            for (DenseLayer layer : layers) {
//                GPUMatrixBackend weightsBackend = (GPUMatrixBackend) layer.getWeights();
//                INDArray weightsData = weightsBackend.getRawData();
//                GPUMatrixBackend biasesBackend = (GPUMatrixBackend) layer.getBiases();
//                INDArray biasesData = biasesBackend.getRawData();
//
//                // Guardar dimensiones de los pesos
//                writer.write(weightsData.rows() + "," + weightsData.columns());
//                writer.newLine();
//                // Guardar los pesos
//                for (int i = 0; i < weightsData.rows(); i++) {
//                    for (int j = 0; j < weightsData.columns(); j++) {
//                        writer.write(String.valueOf(weightsData.getFloat(i, j)));
//                        if (j < weightsData.columns() - 1) {
//                            writer.write(",");
//                        }
//                    }
//                    writer.newLine();
//                }
//                // Guardar las dimensiones de los biases
//                writer.write(biasesData.rows() + "," + biasesData.columns());
//                writer.newLine();
//                // Guardar los biases
//                for (int i = 0; i < biasesData.rows(); i++) {
//                    writer.write(String.valueOf(biasesData.getFloat(i, 0)));
//                    if (i < biasesData.rows() - 1) {
//                        writer.write(",");
//                    }
//                }
//                writer.newLine();
                Matrix weights = layer.getWeights();
                Matrix biases = layer.getBiases();

                // Guardar dimensiones de los pesos
                writer.write(weights.rows() + "," + weights.cols());
                writer.newLine();
                // Guardar los pesos
                float[][] weightsData = weights.toFloatMatrix();
                for (int i = 0; i < weights.rows(); i++) {
                    for (int j = 0; j < weights.cols(); j++) {
                        writer.write(String.valueOf(weightsData[i][j]));
                        if (j < weights.cols() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.newLine();
                }
                // Guardar las dimensiones de los biases
                writer.write(biases.rows() + "," + biases.cols());
                writer.newLine();
                // Guardar los biases
                float[][] biasesData = biases.toFloatMatrix();
                for (int i = 0; i < biases.rows(); i++) {
                    writer.write(String.valueOf(biasesData[i][0]));
                    if (i < biases.rows() - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }
            System.out.println("¡Red neuronal guardada en " + FILENAME + "!");
        } catch (IOException e) {
            System.err.println("Error al guardar la red neuronal: " + e.getMessage());
        }
    }

    public static NeuralNetwork loadNetwork(int inputSize, int outputSize) {
        File file = new File(FILENAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
                int numLayers = Integer.parseInt(reader.readLine());
                List<DenseLayer> layers = new ArrayList<>();
                int currentInputSize = inputSize;
                Optimizer optimizer = new Adam(0.001); // Necesitamos un optimizador para la red cargada
                LossFunction lossFunction = new BinaryCrossEntropy(); // Necesitamos la función de pérdida

                for (int i = 0; i < numLayers; i++) {
                    // Leer dimensiones de los pesos
                    String[] weightsShapeStr = reader.readLine().split(",");
                    int rowsWeights = Integer.parseInt(weightsShapeStr[0]);
                    int colsWeights = Integer.parseInt(weightsShapeStr[1]);
                    float[][] weightsData = new float[rowsWeights][colsWeights];
                    for (int r = 0; r < rowsWeights; r++) {
                        String[] rowValues = reader.readLine().split(",");
                        for (int c = 0; c < colsWeights; c++) {
                            weightsData[r][c] = Float.parseFloat(rowValues[c]);
                        }
                    }
                    Matrix weights = MatrixFactory.createMatrix(weightsData);//new GPUMatrixBackend(weightsData);

                    // Leer dimensiones de los biases
                    String[] biasesShapeStr = reader.readLine().split(",");
                    int rowsBiases = Integer.parseInt(biasesShapeStr[0]);
                    int colsBiases = Integer.parseInt(biasesShapeStr[1]);
                    float[][] biasesData = new float[rowsBiases][colsBiases];
                    String[] biasValues = reader.readLine().split(",");
                    for (int r = 0; r < rowsBiases; r++) {
                        biasesData[r][0] = Float.parseFloat(biasValues[r]);
                    }
                    Matrix biases = MatrixFactory.createMatrix(biasesData);//new GPUMatrixBackend(biasesData);

                    Activation activation;
                    if (i < numLayers - 1) {
                        activation = Activation.RELU;
                    } else {
                        activation = Activation.SIGMOID;
                    }

                    DenseLayer layer = new DenseLayer(currentInputSize, rowsWeights, activation);
                    layer.setWeights(weights);
                    layer.setBiases(biases);
                    layers.add(layer);
                    currentInputSize = rowsWeights;
                }

                return new NeuralNetwork(layers, lossFunction, optimizer, 0.001); // Devuelve la red cargada
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error al cargar la red neuronal: " + e.getMessage());
                return null; // Devuelve null si hay un error
            }
        } else {
            return null; // Devuelve null si el archivo no existe
        }
    }

    public static LoadResult loadMetadata(){
        File file = new File(METADATA_FILENAME);
        if(file.exists()){
            try(BufferedReader metadataReader = new BufferedReader(new FileReader(METADATA_FILENAME))){
                // --- Cargar Metadatos ---
                long trainingTime = Long.parseLong(metadataReader.readLine());
                int epochs = Integer.parseInt(metadataReader.readLine());
                int batchSize = Integer.parseInt(metadataReader.readLine());
                int lossHistorySize = Integer.parseInt(metadataReader.readLine());
                List<Double> lossHistory = new ArrayList<>();
                for (int i = 0; i < lossHistorySize; i++) {
                    lossHistory.add(Double.parseDouble(metadataReader.readLine()));
                }
                return new LoadResult(trainingTime,lossHistory,epochs,batchSize);
            } catch (IOException e) {
                System.err.println("Error al cargar los metadatos de la red neuronal: " + e.getMessage());
            }
        }
        return null;
    }

    public static class LoadResult {
        private final long trainingTime;
        private final List<Double> lossHistory;
        private final int epochs;
        private final int batchSize;

        public LoadResult(long trainingTime, List<Double> lossHistory, int epochs, int batchSize) {
            this.trainingTime = trainingTime;
            this.lossHistory = lossHistory;
            this.epochs = epochs;
            this.batchSize = batchSize;
        }

        public long getTrainingTime() {
            return trainingTime;
        }

        public List<Double> getLossHistory() {
            return lossHistory;
        }

        public int getEpochs() {
            return epochs;
        }

        public int getBatchSize() {
            return batchSize;
        }
    }
}
