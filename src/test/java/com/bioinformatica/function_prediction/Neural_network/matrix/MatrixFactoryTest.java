package com.bioinformatica.function_prediction.Neural_network.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatrixFactoryTest {
    @Test
    void testCreateMatrix_GPUSuccess() {
        MatrixFactory.setUseGPU(true);
        Matrix matrix = MatrixFactory.createMatrix(new float[][]{{1, 2}, {3, 4}});
        assertTrue(matrix instanceof GPUMatrixBackend, "Debería ser GPUMatrixBackend cuando useGPU es true y no hay error simulado.");
        MatrixFactory.setSimulateGPUMemoryError(false); // Desactivamos la simulación para otros tests
    }

    @Test
    void testCreateMatrix_GPUFailsSwitchesToCPU() {
        MatrixFactory.setUseGPU(true);
        MatrixFactory.setSimulateGPUMemoryError(true); // Activamos la simulación de error

        Matrix matrix = MatrixFactory.createMatrix(new float[][]{{1, 2}, {3, 4}});

        assertTrue(matrix instanceof CPUMatrixBackend, "Debería ser CPUMatrixBackend cuando la GPU simula un error.");
        assertFalse(MatrixFactory.isUsingGPU(), "El flag useGPU debería ser false después de simular un error.");

        MatrixFactory.setSimulateGPUMemoryError(false); // Desactivamos la simulación para otros tests
        MatrixFactory.setUseGPU(true); // Volvemos a activar el uso de la GPU para otros tests
    }

    @Test
    void testCreateMatrix_CPUShouldBeUsedWhenUseGPUSetToFalse() {
        MatrixFactory.setUseGPU(false);
        Matrix matrix = MatrixFactory.createMatrix(new float[][]{{1, 2}, {3, 4}});
        assertTrue(matrix instanceof CPUMatrixBackend, "Debería ser CPUMatrixBackend cuando useGPU es false.");
        MatrixFactory.setSimulateGPUMemoryError(false); // Desactivamos la simulación para otros tests
        MatrixFactory.setUseGPU(true); // Volvemos a activar el uso de la GPU para otros tests
    }
}
