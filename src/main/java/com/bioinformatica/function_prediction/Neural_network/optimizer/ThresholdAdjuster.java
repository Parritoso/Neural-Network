package com.bioinformatica.function_prediction.Neural_network.optimizer;

import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;

public class ThresholdAdjuster {
    private double[] thresholds;

    public ThresholdAdjuster(double[] thresholds) {
        this.thresholds = thresholds;
    }

    public Matrix apply(Matrix probabilities) {
        float[][] probs = probabilities.toFloatMatrix();
        int rows = probabilities.rows();
        int cols = probabilities.cols();
        float[][] binarized = new float[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                binarized[i][j] = probs[i][j] > thresholds[j] ? 1.0f : 0.0f;
            }
        }
        return MatrixFactory.createMatrix(binarized);
    }

    public static ThresholdAdjuster fromValidation(Matrix probs, Matrix targets) {
        int numLabels = probs.cols();
        double[] bestThresholds = new double[numLabels];

        for (int j = 0; j < numLabels; j++) {
            double bestF1 = -1;
            double bestThresh = 0.5;
            for (double t = 0.01; t <= 0.99; t += 0.01) {
                int tp = 0, fp = 0, fn = 0;
                for (int i = 0; i < probs.rows(); i++) {
                    double p = probs.toFloatMatrix()[i][j];
                    double y = targets.toFloatMatrix()[i][j];
                    boolean pred = p > t;
                    if (pred && y == 1) tp++;
                    if (pred && y == 0) fp++;
                    if (!pred && y == 1) fn++;
                }
                double precision = tp / (double)(tp + fp + 1e-6);
                double recall = tp / (double)(tp + fn + 1e-6);
                double f1 = 2 * precision * recall / (precision + recall + 1e-6);
                if (f1 > bestF1) {
                    bestF1 = f1;
                    bestThresh = t;
                }
            }
            bestThresholds[j] = bestThresh;
        }
        return new ThresholdAdjuster(bestThresholds);
    }
}
