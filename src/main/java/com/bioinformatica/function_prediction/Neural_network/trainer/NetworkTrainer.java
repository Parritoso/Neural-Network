package com.bioinformatica.function_prediction.Neural_network.trainer;

import com.bioinformatica.function_prediction.Neural_network.layer.DenseLayer;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;
import com.bioinformatica.function_prediction.Neural_network.model.NeuralNetwork;
import com.bioinformatica.function_prediction.Neural_network.optimizer.ThresholdAdjuster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class NetworkTrainer {
    private NeuralNetwork network;
    private List<Double> lossHistory;
    private int epochs;
    private int batchSize;

    public NetworkTrainer(NeuralNetwork network) {
        this.network = network;
        this.lossHistory = new ArrayList<>();
    }
    public void train(Matrix trainingData, Matrix trainingLabels, int epochs, int batchSize) {
        int numSamples = trainingData.shape()[0];
        lossHistory.clear();

        for (DenseLayer layer : network.getLayers()) {
            layer.setTraining(true);
            layer.setDropoutRate(0.5);
        }

        for (int epoch = 1; epoch <= epochs; epoch++) {
            double totalLoss = 0;
            for (int i = 0; i < numSamples; i += batchSize) {
                int end = Math.min(i + batchSize, numSamples);
                Matrix batchInput = trainingData.slice(i, end).transpose();
                Matrix batchLabels = trainingLabels.slice(i, end).transpose();

//                System.out.println("Shape of batchInput: " + Arrays.toString(batchInput.shape()));
//                System.out.println("Shape of batchLabels: " + Arrays.toString(batchLabels.shape()));

                double loss = network.trainStep(batchInput, batchLabels);
                totalLoss += loss;

                if (Double.isNaN(loss)) {
                    System.out.println("NaN detected in batchLoss at epoch " + epoch + ", batch " + i);
                }
            }
            double averageLoss = (totalLoss / (numSamples / (double)batchSize));
            System.out.println("Epoch " + epoch + ", Average Loss = " + averageLoss);
            lossHistory.add(averageLoss);
        }
        this.epochs = epochs;
        this.batchSize = batchSize;
    }

    public Matrix predict(Matrix input) {
        return network.predict(input.transpose());
    }
    public Matrix predictone(Matrix input) {
        for (DenseLayer layer : network.getLayers()) {
            layer.setTraining(false);
        }
        return network.predict(input/*.transpose()*/);
    }

    public double evaluate(Matrix testData, Matrix testLabels) {
        for (DenseLayer layer : network.getLayers()) {
            layer.setTraining(false);
        }

        Matrix predictions = predict(testData).transpose();
        //System.out.println("Forma de predictions al inicio de evaluate: " + Arrays.toString(predictions.shape()));
        // Aquí implementarías métricas de evaluación para clasificación multietiqueta
        // como la precisión promedio, F1 score, etc.
        // Por ahora, devolvemos una pérdida promedio aproximada.
        double totalLoss = 0;
        int numSamples = testData.shape()[0];
        for (int i = 0; i < numSamples; i++) {
            Matrix singlePrediction = predictions.slice(i, i + 1).transpose();
            Matrix singleLabel = testLabels.slice(i, i + 1).transpose();
            // Asegurémonos de que tengan la misma cantidad de filas (etiquetas)
            if (singlePrediction.shape()[0] != singleLabel.shape()[0]) {
                System.err.println("¡Ojo! Las formas de la predicción y la etiqueta no coinciden para la muestra " + i);
                System.err.println("Forma de la predicción: " + java.util.Arrays.toString(singlePrediction.shape()));
                System.err.println("Forma de la etiqueta: " + java.util.Arrays.toString(singleLabel.shape()));
                continue; // Saltar esta muestra para evitar errores
            }

            //System.out.println("Forma de singlePrediction: " + Arrays.toString(singlePrediction.shape()));
            //System.out.println("Forma de singleLabel: " + Arrays.toString(singleLabel.shape()));
            totalLoss += network.getLossFunction().calculate(singlePrediction, singleLabel);
        }
        return totalLoss / numSamples;
    }

    public ThresholdAdjuster fitThresholds(Matrix validationData, Matrix validationLabels) {
        Matrix rawPredictions = predict(validationData).transpose(); // Probabilidades sigmoid
        return ThresholdAdjuster.fromValidation(rawPredictions, validationLabels);
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

    public void setLossHistory(List<Double> lossHistory) {
        this.lossHistory = lossHistory;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
