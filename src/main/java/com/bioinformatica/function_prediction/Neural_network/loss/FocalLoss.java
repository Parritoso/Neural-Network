package com.bioinformatica.function_prediction.Neural_network.loss;

import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;

public class FocalLoss implements LossFunction {
    private final double gamma;
    private final double alpha;
    private final double epsilon = 1e-9;

    public FocalLoss(double gamma, double alpha) {
        this.gamma = gamma;
        this.alpha = alpha;
    }

    @Override
    public double calculate(Matrix predicted, Matrix target) {
        float[][] y = target.toFloatMatrix();
        float[][] p = predicted.toFloatMatrix();
        double total = 0.0;

        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < y[0].length; j++) {
                double pt = y[i][j] == 1 ? p[i][j] : 1 - p[i][j];
//                if (Double.isNaN(pt) || pt <= 0.0 || pt >= 1.0) {
//                    System.out.printf("Valor inválido pt=%f para y=%f, p=%f%n", pt, y[i][j], p[i][j]);
//                }
                pt = Math.max(pt, epsilon);
                total += -alpha * Math.pow(1 - pt, gamma) * Math.log(pt);
            }
        }
        return total / (y.length * y[0].length);
    }

    @Override
    public Matrix derivative(Matrix predicted, Matrix target) {
        float[][] y = target.toFloatMatrix();
        float[][] p = predicted.toFloatMatrix();
        float[][] grad = new float[y.length][y[0].length];

        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < y[0].length; j++) {
                double pt = y[i][j] == 1 ? p[i][j] : 1 - p[i][j];
                pt = Math.max(pt, epsilon);
                double log_pt = Math.log(pt);
                double one_minus_pt = 1 - pt;

                if (y[i][j] == 1) {
                    grad[i][j] = (float)(-alpha * (
                            gamma * Math.pow(one_minus_pt, gamma - 1) * log_pt
                                    + Math.pow(one_minus_pt, gamma) / pt
                    ));
                } else {
                    grad[i][j] = (float)(alpha * (
                            gamma * Math.pow(pt, gamma - 1) * Math.log(1 - pt)
                                    + Math.pow(pt, gamma) / (1 - pt)
                    ));
                }
            }
        }

        return MatrixFactory.createMatrix(grad);
    }
}
