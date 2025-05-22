package com.bioinformatica.function_prediction.Neural_network.matrix;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.factory.Nd4j;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class GPUMatrixBackendTest {
    private static final float DELTA = 1e-6f; // Margen de error para comparaciones de punto flotante

    @Test
    void constructor_withRowsAndCols() {
        GPUMatrixBackend matrix = new GPUMatrixBackend(2, 3);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        assertEquals(0.0f, matrix.mean(), DELTA); // Inicializado con ceros
    }

    @Test
    void constructor_withINDArray() {
        INDArray ndArray = Nd4j.create(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend matrix = new GPUMatrixBackend(ndArray);
        assertArrayEquals(new int[]{2, 2}, matrix.shape());
        assertArrayEquals(new float[]{1, 2, 3, 4}, matrix.toFloatArray(), DELTA);
    }

    @Test
    void constructor_withFloatArray() {
        float[][] rawData = {{1.1f, 2.2f, 3.3f}, {4.4f, 5.5f, 6.6f}};
        GPUMatrixBackend matrix = new GPUMatrixBackend(rawData);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        assertArrayEquals(new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f}, matrix.toFloatArray(), DELTA);
    }

    @Test
    void dot_validMatrices() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend b = new GPUMatrixBackend(new float[][]{{5, 6}, {7, 8}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.dot(b);
        assertArrayEquals(new float[]{19, 22, 43, 50}, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void dot_incompatibleMatrices() {
        GPUMatrixBackend a = new GPUMatrixBackend(2, 3);
        GPUMatrixBackend b = new GPUMatrixBackend(2, 3);
        ND4JIllegalStateException exception = assertThrows(ND4JIllegalStateException.class, () -> a.dot(b));
        assertEquals("Cannot execute matrix multiplication: [2, 3]x[2, 3]: Column of left array 3 != rows of right 2", exception.getMessage());
    }

    @Test
    void add_withMatrix() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend b = new GPUMatrixBackend(new float[][]{{5, 6}, {7, 8}});
        a.add(b);
        assertArrayEquals(new float[]{6, 8, 10, 12}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void add_withScalar() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        a.add(2.0);
        assertArrayEquals(new float[]{3, 4, 5, 6}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void subtract_withMatrix() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{5, 6}, {7, 8}});
        GPUMatrixBackend b = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        a.subtract(b);
        assertArrayEquals(new float[]{4, 4, 4, 4}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void multiply_withScalar() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        a.multiply(2.0);
        assertArrayEquals(new float[]{2, 4, 6, 8}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void multiply_withMatrix() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend b = new GPUMatrixBackend(new float[][]{{2, 2}, {2, 2}});
        a.multiply(b);
        assertArrayEquals(new float[]{2, 4, 6, 8}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void divide_withMatrix() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{6, 8}, {10, 12}});
        GPUMatrixBackend b = new GPUMatrixBackend(new float[][]{{2, 2}, {2, 2}});
        a.divide(b);
        assertArrayEquals(new float[]{3, 4, 5, 6}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void divide_withScalar() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{2, 4}, {6, 8}});
        a.divide(2.0);
        assertArrayEquals(new float[]{1, 2, 3, 4}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void rsub_withScalar() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.rsub(5.0);
        assertArrayEquals(new float[]{4, 3, 2, 1}, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void rsub_withMatrix() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend b = new GPUMatrixBackend(new float[][]{{5, 6}, {7, 8}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.rsub(b);
        assertArrayEquals(new float[]{4, 4, 4, 4}, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void softmax() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.softmax();
        float[] expected = {0.26894143f, 0.7310586f, 0.26894143f, 0.7310586f};
        assertArrayEquals(expected, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void copy() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend copy = (GPUMatrixBackend) a.copy();
        assertArrayEquals(a.toFloatArray(), copy.toFloatArray(), DELTA);
        assertArrayEquals(a.shape(), copy.shape());
        // Asegurarse de que la copia es independiente
        copy = (GPUMatrixBackend) copy.add(1.0);
        assertNotEquals(a.mean(), copy.mean(), DELTA);
    }

    @Test
    void sigmoid() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{-1, 0, 1}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.sigmoid();
        float[] expected = {0.26894143f, 0.5f, 0.7310586f};
        assertArrayEquals(expected, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{1, 3}, result.shape());
    }

    @Test
    void sigmoidDerivative() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{0.1f, 0.5f, 0.9f}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.sigmoidDerivative();
        float[] expected = {0.249376f, 0.235004f, 0.2055005194f};
        assertArrayEquals(expected, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{1, 3}, result.shape());
    }

    @Test
    void relu() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{-1, 0, 1, -2, 2}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.relu();
        float[] expected = {0, 0, 1, 0, 2};
        assertArrayEquals(expected, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{1, 5}, result.shape());
    }

    @Test
    void reluDerivative() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{-1, 0, 1, -2, 2}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.reluDerivative();
        float[] expected = {0, 0, 1, 0, 1};
        assertArrayEquals(expected, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{1, 5}, result.shape());
    }

    @Test
    void fill() {
        GPUMatrixBackend a = new GPUMatrixBackend(2, 2);
        a.fill(3.14);
        assertArrayEquals(new float[]{3.14f, 3.14f, 3.14f, 3.14f}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void log() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2, 3}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.log();
        float[] expected = {0.0f, 0.69314718f, 1.09861229f};
        assertArrayEquals(expected, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{1, 3}, result.shape());
    }

    @Test
    void round() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1.1f, 1.9f, -1.1f, -1.9f}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.round();
        float[] expected = {1.0f, 2.0f, -1.0f, -2.0f};
        assertArrayEquals(expected, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{1, 4}, result.shape());
    }

    @Test
    void mean() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        assertEquals(2.5f, a.mean(), DELTA);
    }

    @Test
    void shape() {
        GPUMatrixBackend a = new GPUMatrixBackend(3, 4);
        assertArrayEquals(new int[]{3, 4}, a.shape());
    }

    @Test
    void printShape() {
        GPUMatrixBackend a = new GPUMatrixBackend(2, 2);
        // No se puede verificar directamente la salida de System.out.println,
        // pero la prueba se ejecutará sin errores si el método funciona.
        a.printShape();
    }

    @Test
    void slice() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.slice(1, 3);
        assertArrayEquals(new float[]{4, 5, 6, 7, 8, 9}, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 3}, result.shape());
    }

    @Test
    void meanRows() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2, 3}, {4, 5, 6}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.meanRows();
        assertArrayEquals(new float[]{2.0f, 5.0f}, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{1, 2}, result.shape());
    }

    @Test
    void applyFunction() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2}, {3, 4}});
        Function<Double, Double> square = x -> x * x;
        a.applyFunction(square);
        assertArrayEquals(new float[]{1, 4, 9, 16}, a.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, a.shape());
    }

    @Test
    void transpose() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2, 3}, {4, 5, 6}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.transpose();
        assertArrayEquals(new float[]{1, 4, 2, 5, 3, 6}, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{3, 2}, result.shape());
    }

    @Test
    void getRawData() {
        INDArray ndArray = Nd4j.create(new float[][]{{1, 2}, {3, 4}});
        GPUMatrixBackend matrix = new GPUMatrixBackend(ndArray);
        assertEquals(ndArray, matrix.getRawData());
    }

    @Test
    void randn_validShape() {
        Matrix matrix = GPUMatrixBackend.randn(2, 3);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        // No se puede verificar los valores exactos, solo que se generaron números
        assertTrue(matrix.mean() > -1 && matrix.mean() < 1);
    }

    @Test
    void zeros_validShape() {
        Matrix matrix = GPUMatrixBackend.zeros(2, 3);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        assertEquals(0.0f, matrix.mean(), DELTA);
    }

    @Test
    void reshape_validShape() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2, 3, 4}});
        GPUMatrixBackend result = (GPUMatrixBackend) a.reshape(2, 2);
        assertArrayEquals(new float[]{1, 2, 3, 4}, result.toFloatArray(), DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void reshape_invalidShape() {
        GPUMatrixBackend a = new GPUMatrixBackend(new float[][]{{1, 2, 3, 4}});
        ND4JIllegalStateException exception = assertThrows(ND4JIllegalStateException.class, () -> a.reshape(3, 2));
        assertEquals("New shape length doesn't match original length: [6] vs [4]. Original shape: [1, 4] New Shape: [3, 2]", exception.getMessage());
    }
}
