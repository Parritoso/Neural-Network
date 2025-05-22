package com.bioinformatica.function_prediction.Neural_network.matrix;

import org.nd4j.linalg.factory.Nd4j;

import java.util.Random;

public class MatrixFactory {
    private static boolean useGPU = true; // Configurable
    private static boolean simulateGPUMemoryError = false; // Nueva variable para simular error
    private static Random cpuRandom = new Random(42);

    public static void setUseGPU(boolean useGPU) {
        MatrixFactory.useGPU = useGPU;
    }
    // Nuevo método para activar/desactivar la simulación de error
    public static void setSimulateGPUMemoryError(boolean simulateError) {
        MatrixFactory.simulateGPUMemoryError = simulateError;
    }

    public static Matrix createMatrix(float[][] data) {
        if (useGPU) {
            try {
                if (simulateGPUMemoryError) {
                    throw new RuntimeException("Simulated GPU memory error"); // Simula un error
                }
                return new GPUMatrixBackend(data);
            } catch (Exception e) {
                System.err.println("Error al crear matriz en GPU: " + e.getMessage());
                System.out.println("Cambiando a CPU para esta operación.");
                useGPU = false; // Desactivamos el uso de la GPU para futuras operaciones
                double[][] doubleData = new double[data.length][data[0].length];
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data[0].length; j++) {
                        doubleData[i][j] = data[i][j];
                    }
                }
                return new CPUMatrixBackend(doubleData);
            }
        } else {
            double[][] doubleData = new double[data.length][data[0].length];
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    doubleData[i][j] = data[i][j];
                }
            }
            return new CPUMatrixBackend(doubleData);
        }
    }

    public static Matrix createMatrix(int rows, int cols) {
        if (useGPU) {
            try {
                if (simulateGPUMemoryError) {
                    throw new RuntimeException("Simulated GPU memory error"); // Simula un error
                }
                return new GPUMatrixBackend(rows, cols);
            } catch (Exception e) {
                System.err.println("Error al crear matriz en GPU: " + e.getMessage());
                System.out.println("Cambiando a CPU para esta operación.");
                useGPU = false; // Desactivamos el uso de la GPU para futuras operaciones
                return new CPUMatrixBackend(rows, cols);
            }
        } else {
            return new CPUMatrixBackend(rows, cols);
        }
    }

    public static boolean isUsingGPU() {
        return useGPU;
    }

    public static Matrix randn(int rows, int cols){
        if(isUsingGPU()){
            try {
                if (simulateGPUMemoryError) {
                    throw new RuntimeException("Simulated GPU memory error"); // Simula un error
                }
                return GPUMatrixBackend.randn(rows, cols);
            } catch (Exception e) {
                System.err.println("Error al crear matriz aleatoria en GPU: " + e.getMessage());
                System.out.println("Cambiando a CPU para esta operación.");
                useGPU = false; // Desactivamos el uso de la GPU para futuras operaciones
                return CPUMatrixBackend.randn(rows, cols);
            }
        }
        return CPUMatrixBackend.randn(rows, cols);
    }

    public static Matrix zeros(int rows, int cols) {
        if (isUsingGPU()) {
            try {
                if (simulateGPUMemoryError) {
                    throw new RuntimeException("Simulated GPU memory error"); // Simula un error
                }
                return GPUMatrixBackend.zeros(rows, cols);
            } catch (Exception e) {
                System.err.println("Error al crear matriz de ceros en GPU: " + e.getMessage());
                System.out.println("Cambiando a CPU para esta operación.");
                useGPU = false; // Desactivamos el uso de la GPU para futuras operaciones
                return CPUMatrixBackend.zeros(rows, cols);
            }
        }
        return CPUMatrixBackend.zeros(rows, cols);
    }

    public static Matrix ones(int rows, int cols) {
        if (isUsingGPU()) {
            try {
                if (simulateGPUMemoryError) {
                    throw new RuntimeException("Simulated GPU memory error"); // Simula un error
                }
                return GPUMatrixBackend.ones(rows, cols);
            } catch (Exception e) {
                System.err.println("Error al crear matriz de ceros en GPU: " + e.getMessage());
                System.out.println("Cambiando a CPU para esta operación.");
                useGPU = false; // Desactivamos el uso de la GPU para futuras operaciones
                return CPUMatrixBackend.zeros(rows, cols);
            }
        }
        return CPUMatrixBackend.ones(rows, cols);
    }

    public static Matrix rand(int rows, int cols){
        if(isUsingGPU()){
            try {
                return new GPUMatrixBackend(Nd4j.rand(rows, cols));
            } catch (Exception e){
                System.err.println("Error al crear matriz de ceros en GPU: " + e.getMessage());
                System.out.println("Cambiando a CPU para esta operación.");
                useGPU = false; // Desactivamos el uso de la GPU para futuras operaciones
            }
        }
        double[][] data = new double[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = cpuRandom.nextDouble(); // uniforme [0,1)
            }
        }
        return new CPUMatrixBackend(data);
    }

    public static void setSeed(long seed) {
        cpuRandom = new Random(seed);
        if (useGPU) Nd4j.getRandom().setSeed(seed);
    }
}
