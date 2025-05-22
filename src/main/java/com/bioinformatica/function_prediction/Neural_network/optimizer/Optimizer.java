package com.bioinformatica.function_prediction.Neural_network.optimizer;

import com.bioinformatica.function_prediction.Neural_network.layer.DenseLayer;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;

public interface Optimizer {
    void update(DenseLayer layer, Matrix weightGradient, Matrix biasGradient);
}
