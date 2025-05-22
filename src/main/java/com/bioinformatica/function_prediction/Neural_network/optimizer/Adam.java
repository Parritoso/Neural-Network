package com.bioinformatica.function_prediction.Neural_network.optimizer;

import com.bioinformatica.function_prediction.Neural_network.matrix.GPUMatrixBackend;
import com.bioinformatica.function_prediction.Neural_network.layer.DenseLayer;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;

public class Adam implements Optimizer {
    private double learningRate;
    private double beta1 = 0.9;
    private double beta2 = 0.999;
    private double epsilon = 1e-8;
    private Matrix m_w, v_w, m_b, v_b;
    private int t = 0;

    public Adam(double learningRate) {
        this.learningRate = learningRate;
    }

    @Override
    public void update(DenseLayer layer, Matrix weightGradient, Matrix biasGradient) {
        t++;
        Matrix weights = layer.getWeights();
        Matrix biases = layer.getBiases();

        // Inicialización de los momentos si es la primera actualización
        if (m_w == null) {
            /*
            m_w = GPUMatrixBackend.zeros(weights.shape()[0], weights.shape()[1]);
            v_w = GPUMatrixBackend.zeros(weights.shape()[0], weights.shape()[1]);
            m_b = GPUMatrixBackend.zeros(biases.shape()[0], biases.shape()[1]);
            v_b = GPUMatrixBackend.zeros(biases.shape()[0], biases.shape()[1]);*/
            m_w = MatrixFactory.zeros(weights.shape()[0], weights.shape()[1]);
            v_w = MatrixFactory.zeros(weights.shape()[0], weights.shape()[1]);
            m_b = MatrixFactory.zeros(biases.shape()[0], biases.shape()[1]);
            v_b = MatrixFactory.zeros(biases.shape()[0], biases.shape()[1]);
        }

        // Actualización de los momentos para pesos
        m_w = m_w.multiply(beta1).add(weightGradient.multiply(1 - beta1));
        v_w = v_w.multiply(beta2).add(weightGradient.copy().multiply(weightGradient).multiply(1 - beta2));

        // Corrección del sesgo para pesos
        Matrix m_w_corrected = m_w.divide(1 - Math.pow(beta1, t));
        Matrix v_w_corrected = v_w.divide(1 - Math.pow(beta2, t));

        // Actualización de los pesos
        weights.subtract(m_w_corrected.divide(v_w_corrected.applyFunction(Math::sqrt).add(epsilon)).multiply(learningRate));

        // Actualización de los momentos para biases
        m_b = m_b.multiply(beta1).add(biasGradient.multiply(1 - beta1));
        v_b = v_b.multiply(beta2).add(biasGradient.copy().multiply(biasGradient).multiply(1 - beta2));

        // Corrección del sesgo para biases
        Matrix m_b_corrected = m_b.divide(1 - Math.pow(beta1, t));
        Matrix v_b_corrected = v_b.divide(1 - Math.pow(beta2, t));

        // Actualización de los biases
        biases.subtract(m_b_corrected.divide(v_b_corrected.applyFunction(Math::sqrt).add(epsilon)).multiply(learningRate));
    }
}
