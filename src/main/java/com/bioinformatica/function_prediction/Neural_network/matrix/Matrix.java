package com.bioinformatica.function_prediction.Neural_network.matrix;

import java.io.Serializable;
import java.util.function.Function;

public interface Matrix extends Serializable {
    // Operaciones básicas
    Matrix dot(Matrix a);
    Matrix add(Matrix a);
    Matrix add(double scalar);
    Matrix subtract(Matrix a);
    Matrix multiply(Matrix a);
    Matrix multiply(double scalar);

    Matrix divide(Matrix a);
    Matrix divide(double scalar);

    Matrix rsub(double scalar);
    Matrix rsub(Matrix a);

    // Transformaciones
    Matrix transpose();
    Matrix applyFunction(Function<Double, Double> func);
    Matrix softmax();
    Matrix copy();
    Matrix reshape(int rows, int cols);

    // Funciones de activación y sus derivadas
    Matrix sigmoid();
    Matrix sigmoidDerivative();
    Matrix relu();
    Matrix reluDerivative();

    // Funciones matemáticas
    Matrix fill(double value);
    Matrix log();
    Matrix round();

    // Información
    double mean();
    int[] shape(); // {rows, cols}
    void printShape();

    // Manipulación
    Matrix slice(int from, int to); // slice vertical de filas [from, to)
    Matrix meanRows(); // Promedia cada fila y devuelve columna (1D matrix)

    float[] toFloatArray();
    float[][] toFloatMatrix();
    int rows();
    int cols();
    boolean hasNaN();

    double min();
    double max();
    double[][] getRawData();

    /**
     * Expands the dimensions of the current matrix to match the shape of the given matrix.
     * This is useful for element-wise operations between matrices of different but compatible shapes.
     *
     * @param shape The target shape to broadcast to.
     * @return A new matrix with the expanded dimensions.
     */
    Matrix broadcast(int[] shape);
}
