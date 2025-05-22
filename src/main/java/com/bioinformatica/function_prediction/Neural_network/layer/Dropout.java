package com.bioinformatica.function_prediction.Neural_network.layer;

import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;

import java.util.Random;

public class Dropout {
    private double rate;
    private boolean training;

    public Dropout(double rate) {
        this.rate = rate;
        this.training = true;
    }

    public void setTraining(boolean training) {
        this.training = training;
    }

    public Matrix apply(Matrix input) {
        if (!training) return input;
        float[][] data = input.toFloatMatrix();
        Random rand = new Random();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (rand.nextDouble() < rate) {
                    data[i][j] = 0.0f;
                } else {
                    data[i][j] /= (1.0 - rate);
                }
            }
        }
        return MatrixFactory.createMatrix(data);
    }
}
