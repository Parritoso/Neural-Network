package com.bioinformatica.preprocessing.standarization;

import com.bioinformatica.preprocessing.standardization.ZScoreNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZScoreNormalizerTest {
    @Test
    void testNormalizar_casoSencillo() {
        double[][] datos = {{1.0, 2.0}, {3.0, 4.0}};
        ZScoreNormalizer.normalizar(datos);
        assertArrayEquals(new double[]{-1.0, -1.0}, datos[0], 0.001);
        assertArrayEquals(new double[]{1.0, 1.0}, datos[1], 0.001);
    }

    @Test
    void testNormalizar_conNaN() {
        double[][] datos = {{1.0, 2.0, Double.NaN}, {3.0, Double.NaN, 5.0}};
        ZScoreNormalizer.normalizar(datos);
        assertArrayEquals(new double[]{-1.0, 2.0, Double.NaN}, datos[0], 0.001);
        assertArrayEquals(new double[]{1.0, Double.NaN, 5.0}, datos[1], 0.001);
    }

    @Test
    void testNormalizar_desviacionCero() {
        double[][] datos = {{2.0, 2.0}, {2.0, 2.0}};
        ZScoreNormalizer.normalizar(datos);
        assertArrayEquals(new double[]{2.0, 2.0}, datos[0], 0.001);
        assertArrayEquals(new double[]{2.0, 2.0}, datos[1], 0.001);
    }

    @Test
    void testNormalizar_matrizVacia() {
        double[][] datos = {};
        ZScoreNormalizer.normalizar(datos);
        assertEquals(0, datos.length);
    }
}
