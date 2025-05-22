package com.bioinformatica.preprocessing.standarization;

import com.bioinformatica.preprocessing.standardization.LogTransform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogTransformTest {
    @Test
    void testTransformar_casoSencillo() {
        double[][] datos = {{1.0, 2.0}, {0.5, 3.0}};
        LogTransform.transformar(datos);
        assertArrayEquals(new double[]{Math.log1p(1.0), Math.log1p(2.0)}, datos[0], 0.001);
        assertArrayEquals(new double[]{Math.log1p(0.5), Math.log1p(3.0)}, datos[1], 0.001);
    }

    @Test
    void testTransformar_conCerosYNaN() {
        double[][] datos = {{0.0, 2.0}, {Double.NaN, 3.0}};
        LogTransform.transformar(datos);
        assertEquals(Double.NEGATIVE_INFINITY, datos[0][0], 0.001);
        assertEquals(Math.log1p(2.0), datos[0][1], 0.001);
        assertTrue(Double.isNaN(datos[1][0]));
        assertEquals(Math.log1p(3.0), datos[1][1], 0.001);
    }

    @Test
    void testTransformar_valoresNegativosIgnorados() {
        double[][] datos = {{-1.0, 2.0}};
        LogTransform.transformar(datos);
        assertEquals(-1.0, datos[0][0], 0.001);
        assertEquals(Math.log1p(2.0), datos[0][1], 0.001);
    }

    @Test
    void testTransformar_matrizVacia() {
        double[][] datos = {};
        LogTransform.transformar(datos);
        assertEquals(0, datos.length);
    }
}
