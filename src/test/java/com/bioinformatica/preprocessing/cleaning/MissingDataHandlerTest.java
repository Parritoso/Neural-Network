package com.bioinformatica.preprocessing.cleaning;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MissingDataHandlerTest {
    @Test
    void testImputarDatosFaltantes_casoSencillo() {
        double[][] datos = {{1.0, 2.0, Double.NaN}, {4.0, Double.NaN, 6.0}, {Double.NaN, 8.0, 9.0}};
        MissingDataHandler.imputarDatosFaltantes(datos);
        assertArrayEquals(new double[]{1.0, 2.0, 7.5}, datos[0], 0.001);
        assertArrayEquals(new double[]{4.0, 5.0, 6.0}, datos[1], 0.001);
        assertArrayEquals(new double[]{2.5, 8.0, 9.0}, datos[2], 0.001);
    }

    @Test
    void testImputarDatosFaltantes_sinDatosFaltantes() {
        double[][] datos = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        double[][] datosOriginales = Arrays.stream(datos).map(double[]::clone).toArray(double[][]::new);
        MissingDataHandler.imputarDatosFaltantes(datos);
        assertArrayEquals(datosOriginales, datos);
    }

    @Test
    void testImputarDatosFaltantes_columnaConSoloNaN() {
        double[][] datos = {{1.0, Double.NaN}, {2.0, Double.NaN}, {3.0, Double.NaN}};
        MissingDataHandler.imputarDatosFaltantes(datos);
        assertArrayEquals(new double[]{1.0, 0.0}, datos[0], 0.001);
        assertArrayEquals(new double[]{2.0, 0.0}, datos[1], 0.001);
        assertArrayEquals(new double[]{3.0, 0.0}, datos[2], 0.001);
    }

    @Test
    void testImputarDatosFaltantes_matrizVacia() {
        double[][] datos = {};
        MissingDataHandler.imputarDatosFaltantes(datos);
        assertEquals(0, datos.length);
    }
}
