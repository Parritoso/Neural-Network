package com.bioinformatica.function_prediction.Neural_network.matrix;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class CPUMatrixBackendTest {
    private static final double DELTA = 1e-6; // Margen de error para comparaciones de punto flotante

    @Test
    void constructor_withRowsAndCols() {
        CPUMatrixBackend matrix = new CPUMatrixBackend(2, 3);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        assertEquals(0.0, matrix.mean(), DELTA); // Inicializado con ceros
    }

    @Test
    void constructor_withDoubleArray() {
        double[][] rawData = {{1.1, 2.2, 3.3}, {4.4, 5.5, 6.6}};
        CPUMatrixBackend matrix = new CPUMatrixBackend(rawData);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        assertArrayEquals(new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f}, matrix.toFloatArray(), (float) DELTA);
    }

    @Test
    void dot_validMatrices() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend b = new CPUMatrixBackend(new double[][]{{5, 6}, {7, 8}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.dot(b);
        assertArrayEquals(new float[]{19, 22, 43, 50}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void dot_incompatibleMatrices() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 3);
        CPUMatrixBackend b = new CPUMatrixBackend(2, 3);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.dot(b));
        assertEquals("El número de columnas de la primera matriz debe ser igual al número de filas de la segunda matriz.", exception.getMessage());
    }

    @Test
    void add_withMatrix_sameDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend b = new CPUMatrixBackend(new double[][]{{5, 6}, {7, 8}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.add(b);
        assertArrayEquals(new float[]{6, 8, 10, 12}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void add_withMatrix_differentDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 3);
        CPUMatrixBackend b = new CPUMatrixBackend(3, 2);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.add(b));
        assertEquals("Las dimensiones de las matrices deben ser iguales para la suma.", exception.getMessage());
    }

    @Test
    void add_withScalar() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.add(2.0);
        assertArrayEquals(new float[]{3, 4, 5, 6}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void subtract_withMatrix_sameDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{5, 6}, {7, 8}});
        CPUMatrixBackend b = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.subtract(b);
        assertArrayEquals(new float[]{4, 4, 4, 4}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void subtract_withMatrix_differentDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 3);
        CPUMatrixBackend b = new CPUMatrixBackend(3, 2);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
        assertEquals("Las dimensiones de las matrices deben ser iguales para la resta.", exception.getMessage());
    }

    @Test
    void multiply_withMatrix_sameDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend b = new CPUMatrixBackend(new double[][]{{2, 2}, {2, 2}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.multiply(b);
        assertArrayEquals(new float[]{2, 4, 6, 8}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void multiply_withMatrix_differentDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 3);
        CPUMatrixBackend b = new CPUMatrixBackend(3, 2);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.multiply(b));
        assertEquals("Las dimensiones de las matrices deben ser iguales para la multiplicación elemento a elemento.", exception.getMessage());
    }

    @Test
    void multiply_withScalar() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.multiply(2.0);
        assertArrayEquals(new float[]{2, 4, 6, 8}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void divide_withMatrix_sameDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{6, 8}, {10, 12}});
        CPUMatrixBackend b = new CPUMatrixBackend(new double[][]{{2, 2}, {2, 2}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.divide(b);
        assertArrayEquals(new float[]{3, 4, 5, 6}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void divide_withMatrix_differentDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 3);
        CPUMatrixBackend b = new CPUMatrixBackend(3, 2);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.divide(b));
        assertEquals("Las dimensiones de las matrices deben ser iguales para la división elemento a elemento.", exception.getMessage());
    }

    @Test
    void divide_withScalar() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{2, 4}, {6, 8}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.divide(2.0);
        assertArrayEquals(new float[]{1, 2, 3, 4}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void rsub_withScalar() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.rsub(5.0);
        assertArrayEquals(new float[]{4, 3, 2, 1}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void rsub_withMatrix_sameDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend b = new CPUMatrixBackend(new double[][]{{5, 6}, {7, 8}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.rsub(b);
        assertArrayEquals(new float[]{4, 4, 4, 4}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void rsub_withMatrix_differentDimensions() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 3);
        CPUMatrixBackend b = new CPUMatrixBackend(3, 2);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.rsub(b));
        assertEquals("Las dimensiones de las matrices deben ser iguales para la resta elemento a elemento.", exception.getMessage());
    }

    @Test
    void transpose() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2, 3}, {4, 5, 6}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.transpose();
        assertArrayEquals(new float[]{1, 4, 2, 5, 3, 6}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{3, 2}, result.shape());
    }

    @Test
    void applyFunction() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        Function<Double, Double> square = x -> x * x;
        CPUMatrixBackend result = (CPUMatrixBackend) a.applyFunction(square);
        assertArrayEquals(new float[]{1, 4, 9, 16}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void softmax() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.softmax();
        float[] expected = {0.2689414213699951f, 0.7310585786300049f, 0.2689414213699951f, 0.7310585786300049f};
        assertArrayEquals(expected, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void copy() {
        //TODO Comprobar todas las instancias de copy en el codigo
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        CPUMatrixBackend copy = (CPUMatrixBackend) a.copy();
        assertArrayEquals(a.toFloatArray(), copy.toFloatArray(), (float) DELTA);
        assertArrayEquals(a.shape(), copy.shape());
        // Asegurarse de que la copia es independiente
        copy = (CPUMatrixBackend) copy.add(1.0);
        assertNotEquals(a.mean(), copy.mean(), DELTA);
    }

    @Test
    void reshape_validShape() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2, 3, 4}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.reshape(2, 2);
        assertArrayEquals(new float[]{1, 2, 3, 4}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void reshape_invalidShape() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2, 3, 4}});
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.reshape(3, 2));
        assertEquals("El número total de elementos debe ser el mismo para el reshape.", exception.getMessage());
    }

    @Test
    void sigmoid() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{-1, 0, 1}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.sigmoid();
        float[] expected = {0.2689414213699951f, 0.5f, 0.7310585786300049f};
        assertArrayEquals(expected, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{1, 3}, result.shape());
    }

    @Test
    void sigmoidDerivative() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{0.1, 0.5, 0.9}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.sigmoidDerivative();
        float[] expected = {0.09f, 0.25f, 0.09f};
        assertArrayEquals(expected, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{1, 3}, result.shape());
    }

    @Test
    void relu() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{-1, 0, 1, -2, 2}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.relu();
        assertArrayEquals(new float[]{0, 0, 1, 0, 2}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{1, 5}, result.shape());
    }

    @Test
    void reluDerivative() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{-1, 0, 1, -2, 2}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.reluDerivative();
        assertArrayEquals(new float[]{0, 0, 1, 0, 1}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{1, 5}, result.shape());
    }

    @Test
    void fill() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 2);
        CPUMatrixBackend result = (CPUMatrixBackend) a.fill(3.14);
        assertArrayEquals(new float[]{3.14f, 3.14f, 3.14f, 3.14f}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 2}, result.shape());
    }

    @Test
    void log() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2, 3}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.log();
        assertArrayEquals(new float[]{0.0f, 0.6931471805599453f, 1.0986122886681098f}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{1, 3}, result.shape());
    }

    @Test
    void round() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1.1, 1.9, -1.1, -1.9}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.round();
        assertArrayEquals(new float[]{1.0f, 2.0f, -1.0f, -2.0f}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{1, 4}, result.shape());
    }

    @Test
    void mean() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2}, {3, 4}});
        assertEquals(2.5, a.mean(), DELTA);
    }

    @Test
    void shape() {
        CPUMatrixBackend a = new CPUMatrixBackend(3, 4);
        assertArrayEquals(new int[]{3, 4}, a.shape());
    }

    @Test
    void printShape() {
        CPUMatrixBackend a = new CPUMatrixBackend(2, 2);
        // No se puede verificar directamente la salida de System.out.println,
        // pero la prueba se ejecutará sin errores si el método funciona.
        a.printShape();
    }

    @Test
    void slice_validIndices() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.slice(1, 3);
        assertArrayEquals(new float[]{4, 5, 6, 7, 8, 9}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 3}, result.shape());
    }

    @Test
    void slice_invalidIndices_fromNegative() {
        CPUMatrixBackend a = new CPUMatrixBackend(3, 3);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.slice(-1, 2));
        assertEquals("Indices de slice inválidos: from=-1, to=2, rows=3", exception.getMessage());
    }

    @Test
    void slice_invalidIndices_toGreaterThanRows() {
        CPUMatrixBackend a = new CPUMatrixBackend(3, 3);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.slice(0, 4));
        assertEquals("Indices de slice inválidos: from=0, to=4, rows=3", exception.getMessage());
    }

    @Test
    void slice_invalidIndices_fromGreaterThanOrEqualToTo() {
        CPUMatrixBackend a = new CPUMatrixBackend(3, 3);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> a.slice(2, 2));
        assertEquals("Indices de slice inválidos: from=2, to=2, rows=3", exception.getMessage());
    }

    @Test
    void meanRows() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1, 2, 3}, {4, 5, 6}});
        CPUMatrixBackend result = (CPUMatrixBackend) a.meanRows();
        assertArrayEquals(new float[]{2.0f, 5.0f}, result.toFloatArray(), (float) DELTA);
        assertArrayEquals(new int[]{2, 1}, result.shape());
    }

    @Test
    void getData() {
        double[][] rawData = {{1, 2}, {3, 4}};
        CPUMatrixBackend a = new CPUMatrixBackend(rawData);
        assertArrayEquals(rawData, a.getData());
    }

    @Test
    void randn_validShape() {
        CPUMatrixBackend matrix = CPUMatrixBackend.randn(2, 3);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        // No se puede verificar los valores exactos, solo que se generaron números aleatorios
        assertTrue(matrix.mean() > -1 && matrix.mean() < 1);
    }

    @Test
    void zeros_validShape() {
        CPUMatrixBackend matrix = CPUMatrixBackend.zeros(2, 3);
        assertArrayEquals(new int[]{2, 3}, matrix.shape());
        assertEquals(0.0, matrix.mean(), DELTA);
    }

    @Test
    void toFloatMatrix() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1.1, 2.2}, {3.3, 4.4}});
        float[][] floatMatrix = a.toFloatMatrix();
        assertArrayEquals(new float[]{1.1f, 2.2f}, floatMatrix[0], (float) DELTA);
        assertArrayEquals(new float[]{3.3f, 4.4f}, floatMatrix[1], (float) DELTA);
    }

    @Test
    void getRawData() {
        double[][] rawData = {{5, 6}, {7, 8}};
        CPUMatrixBackend a = new CPUMatrixBackend(rawData);
        assertArrayEquals(rawData, a.getRawData());
    }

    @Test
    void rows() {
        CPUMatrixBackend a = new CPUMatrixBackend(5, 2);
        assertEquals(5, a.rows());
    }

    @Test
    void cols() {
        CPUMatrixBackend a = new CPUMatrixBackend(5, 2);
        assertEquals(2, a.cols());
    }

    @Test
    void toFloatArray() {
        CPUMatrixBackend a = new CPUMatrixBackend(new double[][]{{1.5, 2.5}, {3.5, 4.5}});
        float[] floatArray = a.toFloatArray();
        assertArrayEquals(new float[]{1.5f, 2.5f, 3.5f, 4.5f}, floatArray, (float) DELTA);
    }
}
