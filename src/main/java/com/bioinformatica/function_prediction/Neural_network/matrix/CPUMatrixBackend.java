package com.bioinformatica.function_prediction.Neural_network.matrix;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;

public class CPUMatrixBackend implements Matrix {
    private double[][] data;
    private int rows;
    private int cols;
    private static double cpuUsagePercentage = 0.5; // Porcentaje de núcleos a usar (configurable)
    private static final int MIN_TASK_SIZE = 1000; // Tamaño mínimo para paralelizar tareas

    public CPUMatrixBackend(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public CPUMatrixBackend(double[][] data) {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, cols);
        }
    }

    public static void setCpuUsagePercentage(double percentage) {
        if (percentage > 0 && percentage <= 1.0) {
            cpuUsagePercentage = percentage;
        } else {
            System.err.println("Porcentaje de uso de CPU inválido. Debe estar entre 0 y 1. Usando el valor por defecto (0.5).");
        }
    }

    private ForkJoinPool getForkJoinPool() {
        int numThreads = (int) (Runtime.getRuntime().availableProcessors() * cpuUsagePercentage);
        return new ForkJoinPool(Math.max(1, numThreads)); // Aseguramos al menos 1 hilo
    }

    @Override
    public Matrix dot(Matrix a) {
        if (cols != a.shape()[0]) {
            throw new IllegalArgumentException("El número de columnas de la primera matriz debe ser igual al número de filas de la segunda matriz.");
        }
        int m = rows;
        int n = cols;
        int p = a.shape()[1];
        CPUMatrixBackend result = new CPUMatrixBackend(m, p);
        double[][] aData = this.data;
        double[][] bData = ((CPUMatrixBackend) a).data;
        double[][] cData = result.data;

        ForkJoinPool pool = getForkJoinPool();
        RecursiveAction task = new RecursiveAction() {
            @Override
            protected void compute() {
                if (m * p < MIN_TASK_SIZE) {
                    // Cálculo secuencial para tareas pequeñas
                    for (int i = 0; i < m; i++) {
                        for (int j = 0; j < p; j++) {
                            double sum = 0;
                            for (int k = 0; k < n; k++) {
                                sum += aData[i][k] * bData[k][j];
                            }
                            cData[i][j] = sum;
                        }
                    }
                } else {
                    // Dividir la tarea en subtareas
                    int mid = m / 2;
                    invokeAll(new RecursiveAction() {
                        @Override
                        protected void compute() {
                            CPUMatrixBackend.multiplyBlock(aData, bData, cData, 0, 0, 0, 0, n, mid, p);
                        }
                    }, new RecursiveAction() {
                        @Override
                        protected void compute() {
                            CPUMatrixBackend.multiplyBlock(aData, bData, cData, mid, 0, 0, 0, n, m - mid, p);
                        }
                    });
                }
            }
        };
        pool.invoke(task);
        return result;
    }

    private static void multiplyBlock(double[][] a, double[][] b, double[][] c,
                                      int rowA, int colA, int rowB, int colB, int common,
                                      int rowsResult, int colsResult) {
        for (int i = 0; i < rowsResult; i++) {
            for (int j = 0; j < colsResult; j++) {
                double sum = 0;
                for (int k = 0; k < common; k++) {
                    sum += a[rowA + i][colA + k] * b[rowB + k][colB + j];
                }
                c[i][j] += sum; // Importante: += para acumular resultados de bloques
            }
        }
    }

    @Override
    public Matrix add(Matrix a) {
        if (!Arrays.equals(shape(), a.shape())) {
            throw new IllegalArgumentException("Las dimensiones de las matrices deben ser iguales para la suma.");
        }
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        double[][] aData = this.data;
        double[][] bData = ((CPUMatrixBackend) a).data;
        double[][] cData = result.data;

        ForkJoinPool pool = getForkJoinPool();
        RecursiveAction task = new RecursiveAction() {
            @Override
            protected void compute() {
                int length = rows * cols;
                if (length < MIN_TASK_SIZE) {
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            cData[i][j] = aData[i][j] + bData[i][j];
                        }
                    }
                } else {
                    int mid = length / 2;
                    invokeAll(new RecursiveAction() {
                        @Override
                        protected void compute() {
                            for (int i = 0; i < mid / cols; i++) {
                                for (int j = 0; j < cols; j++) {
                                    cData[i][j] = aData[i][j] + bData[i][j];
                                }
                            }
                            for (int j = 0; j < mid % cols; j++) {
                                cData[mid / cols][j] = aData[mid / cols][j] + bData[mid / cols][j];
                            }
                        }
                    }, new RecursiveAction() {
                        @Override
                        protected void compute() {
                            for (int i = mid / cols; i < rows; i++) {
                                for (int j = 0; j < cols; j++) {
                                    int row = i;
                                    int col = j;
                                    if (i == mid / cols) col = mid % cols;
                                    cData[row][col] = aData[row][col] + bData[row][col];
                                }
                            }
                        }
                    });
                }
            }
        };
        pool.invoke(task);
        return result;
    }

    @Override
    public Matrix add(double scalar) {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        double[][] aData = this.data;
        double[][] cData = result.data;

        ForkJoinPool pool = getForkJoinPool();
        RecursiveAction task = new RecursiveAction() {
            @Override
            protected void compute() {
                int length = rows * cols;
                if (length < MIN_TASK_SIZE) {
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            cData[i][j] = aData[i][j] + scalar;
                        }
                    }
                } else {
                    // Similar parallelization as in add(Matrix a)
                    int mid = length / 2;
                    invokeAll(new RecursiveAction() {
                        @Override
                        protected void compute() {
                            for (int i = 0; i < mid / cols; i++) {
                                for (int j = 0; j < cols; j++) {
                                    cData[i][j] = aData[i][j] + scalar;
                                }
                            }
                            for (int j = 0; j < mid % cols; j++) {
                                cData[mid / cols][j] = aData[mid / cols][j] + scalar;
                            }
                        }
                    }, new RecursiveAction() {
                        @Override
                        protected void compute() {
                            for (int i = mid / cols; i < rows; i++) {
                                for (int j = 0; j < cols; j++) {
                                    int row = i;
                                    int col = j;
                                    if (i == mid / cols) col = mid % cols;
                                    cData[row][col] = aData[row][col] + scalar;
                                }
                            }
                        }
                    });
                }
            }
        };
        pool.invoke(task);
        return result;
    }

    // Implementaciones secuenciales para las demás operaciones por simplicidad en este ejemplo
    @Override
    public Matrix subtract(Matrix a) {
        if (!Arrays.equals(shape(), a.shape())) {
            throw new IllegalArgumentException("Las dimensiones de las matrices deben ser iguales para la resta.");
        }
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j] - ((CPUMatrixBackend) a).data[i][j];
            }
        }
        return result;
    }

    @Override
    public Matrix multiply(Matrix a) {
        if (!Arrays.equals(shape(), a.shape())) {
            throw new IllegalArgumentException("Las dimensiones de las matrices deben ser iguales para la multiplicación elemento a elemento.");
        }
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j] * ((CPUMatrixBackend) a).data[i][j];
            }
        }
        return result;
    }

    @Override
    public Matrix multiply(double scalar) {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j] * scalar;
            }
        }
        return result;
    }

    @Override
    public Matrix divide(Matrix a) {
        if (!Arrays.equals(shape(), a.shape())) {
            throw new IllegalArgumentException("Las dimensiones de las matrices deben ser iguales para la división elemento a elemento.");
        }
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j] / ((CPUMatrixBackend) a).data[i][j];
            }
        }
        return result;
    }

    @Override
    public Matrix divide(double scalar) {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j] / scalar;
            }
        }
        return result;
    }

    @Override
    public Matrix rsub(double scalar) {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = scalar - data[i][j];
            }
        }
        return result;
    }

    @Override
    public Matrix rsub(Matrix a) {
        if (!Arrays.equals(shape(), a.shape())) {
            throw new IllegalArgumentException("Las dimensiones de las matrices deben ser iguales para la resta elemento a elemento.");
        }
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = ((CPUMatrixBackend) a).data[i][j] - data[i][j];
            }
        }
        return result;
    }

    @Override
    public Matrix transpose() {
        CPUMatrixBackend result = new CPUMatrixBackend(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[j][i] = data[i][j];
            }
        }
        return result;
    }

    @Override
    public Matrix applyFunction(Function<Double, Double> func) {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = func.apply(data[i][j]);
            }
        }
        return result;
    }

    @Override
    public Matrix softmax() {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        for (int i = 0; i < rows; i++) {
            double sumExp = 0;
            double[] row = data[i];
            double[] resultRow = result.data[i];
            for (double val : row) {
                sumExp += Math.exp(val);
            }
            for (int j = 0; j < cols; j++) {
                resultRow[j] = Math.exp(row[j]) / sumExp;
            }
        }
        return result;
    }

    @Override
    public Matrix copy() {
        //return new CPUMatrixBackend(this.data);
        double[][] newData = new double[this.rows][this.cols];
        CPUMatrixBackend copy = new CPUMatrixBackend(this.rows, this.cols);
        for (int i = 0; i < this.rows; i++) {
            System.arraycopy(this.data[i], 0, copy.data[i], 0, this.cols);
        }
        return copy;
    }

    @Override
    public Matrix reshape(int newRows, int newCols) {
        if (rows * cols != newRows * newCols) {
            throw new IllegalArgumentException("El número total de elementos debe ser el mismo para el reshape.");
        }
        CPUMatrixBackend result = new CPUMatrixBackend(newRows, newCols);
        double[] flattened = new double[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, flattened, index, cols);
            index += cols;
        }
        index = 0;
        for (int i = 0; i < newRows; i++) {
            for (int j = 0; j < newCols; j++) {
                result.data[i][j] = flattened[index++];
            }
        }
        return result;
    }

    @Override
    public Matrix sigmoid() {
        return applyFunction(x -> 1 / (1 + Math.exp(-x)));
    }

    @Override
    public Matrix sigmoidDerivative() {
        return applyFunction(x -> x * (1 - x)); // Asume que la entrada ya es el resultado de sigmoid()
    }

    @Override
    public Matrix relu() {
        return applyFunction(x -> Math.max(0, x));
    }

    @Override
    public Matrix reluDerivative() {
        return applyFunction(x -> (x > 0) ? 1.0 : 0.0);
    }

    @Override
    public Matrix fill(double value) {
        for (int i = 0; i < rows; i++) {
            Arrays.fill(data[i], value);
        }
        return this;
    }

    @Override
    public Matrix log() {
        return applyFunction(Math::log);
    }

    @Override
    public Matrix round() {
        return applyFunction(x -> (double) Math.round(x));
    }

    @Override
    public double mean() {
        double sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sum += data[i][j];
            }
        }
        return sum / (rows * cols);
    }

    @Override
    public int[] shape() {
        return new int[]{rows, cols};
    }

    @Override
    public void printShape() {
        System.out.println("Shape: {" + rows + ", " + cols + "}");
    }

    @Override
    public Matrix slice(int from, int to) {
        if (from < 0 || to > rows || from >= to) {
            throw new IllegalArgumentException("Indices de slice inválidos: from=" + from + ", to=" + to + ", rows=" + rows);
        }
        int newRows = to - from;
        CPUMatrixBackend result = new CPUMatrixBackend(newRows, cols);
        for (int i = 0; i < newRows; i++) {
            System.arraycopy(data[from + i], 0, result.data[i], 0, cols);
        }
        return result;
    }

    @Override
    public Matrix meanRows() {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, 1);
        for (int i = 0; i < rows; i++) {
            double sum = 0;
            for (int j = 0; j < cols; j++) {
                sum += data[i][j];
            }
            result.data[i][0] = sum / cols;
        }
        return result;
    }

    public double[][] getData() {
        return data;
    }

    public static CPUMatrixBackend randn(int rows, int cols) {
        CPUMatrixBackend result = new CPUMatrixBackend(rows, cols);
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = random.nextGaussian(); // Genera números con distribución normal (media 0, desviación estándar 1)
            }
        }
        return result;
    }

    public static CPUMatrixBackend zeros(int rows, int cols) {
        return new CPUMatrixBackend(rows, cols); // La inicialización por defecto de double[][] es a 0.0
    }
    public static CPUMatrixBackend ones(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                matrix[i][j] = 1;
            }
        }
        return new CPUMatrixBackend(matrix);
    }

    public float[][] toFloatMatrix() {
        float[][] floatMatrix = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                floatMatrix[i][j] = (float) data[i][j];
            }
        }
        return floatMatrix;
    }

    public double[][] getRawData() {
        return data;
    }

    @Override
    public int rows() {
        return this.rows;
    }

    @Override
    public int cols() {
        return this.cols;
    }

    @Override
    public boolean hasNaN() {
        for (int i = 0; i < this.rows(); i++) {
            for (int j = 0; j < this.cols(); j++) {
                if (Double.isNaN(data[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float[] toFloatArray() {
        float[] floatArray = new float[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                floatArray[index++] = (float) data[i][j];
            }
        }
        return floatArray;
    }

    public double min() {
        // Recorre todos los valores y retorna el mínimo.
        double min = Double.MAX_VALUE;
        for (int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++){
                if (data[i][j] < min) {
                    min = data[i][j];
                }
            }
        }
        return min;
    }

    public double max() {
        // Recorre todos los valores y retorna el máximo.
        double max = Double.MIN_VALUE;
        for (int i = 0; i < data.length; i++) {
            for(int j = 0; j < cols; j++){
                if (data[i][j] > max) {
                    max = data[i][j];
                }
            }
        }
        return max;
    }

    public Matrix broadcast(int[] shape) {
        if (shape.length != 2) {
            throw new IllegalArgumentException("La forma de broadcast debe tener 2 dimensiones (filas, columnas)");
        }
        int targetRows = shape[0];
        int targetCols = shape[1];

        if (this.rows == targetRows && this.cols == targetCols) {
            return this.copy(); // No es necesario broadcast
        }

        double[][] newData = new double[targetRows][targetCols];

        if (this.rows == 1 && this.cols == targetCols) {
            // Broadcast de una fila a múltiples filas
            for (int i = 0; i < targetRows; i++) {
                for (int j = 0; j < targetCols; j++) {
                    newData[i][j] = this.data[0][j];
                }
            }
        } else if (this.rows == targetRows && this.cols == 1) {
            // Broadcast de una columna a múltiples columnas
            for (int i = 0; i < targetRows; i++) {
                for (int j = 0; j < targetCols; j++) {
                    newData[i][j] = this.data[i][0];
                }
            }
        } else if (this.rows == 1 && this.cols == 1) {
            // Broadcast de un escalar a una matriz
            for (int i = 0; i < targetRows; i++) {
                for (int j = 0; j < targetCols; j++) {
                    newData[i][j] = this.data[0][0];
                }
            }
        } else if (this.rows == targetRows && this.cols == targetCols) {
            // No es necesario broadcast, ya se manejó al principio
        } else {
            throw new IllegalArgumentException("La forma actual [" + this.rows + ", " + this.cols + "] no es broadcastable a [" + targetRows + ", " + targetCols + "]");
        }

        return new CPUMatrixBackend(newData);
    }
}
