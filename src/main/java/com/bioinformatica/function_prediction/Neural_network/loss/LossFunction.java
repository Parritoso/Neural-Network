package com.bioinformatica.function_prediction.Neural_network.loss;

import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;

public interface LossFunction {
    double calculate(Matrix predicted, Matrix target);
    Matrix derivative(Matrix predicted, Matrix target);
}
