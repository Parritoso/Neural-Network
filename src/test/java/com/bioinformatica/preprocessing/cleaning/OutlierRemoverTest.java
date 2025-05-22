package com.bioinformatica.preprocessing.cleaning;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OutlierRemoverTest {
    @Test
    void testEliminarAtipicos_casoSencillo() {
        double[][] datos = {{1.0, 2.0, 3.0, 10.0}, {4.0, 5.0, 6.0, -2.0}, {7.0, 8.0, 9.0, 1.0}};
        double[][] resultado = OutlierRemover.eliminarAtipicos(datos);
        assertArrayEquals(new Double[]{1.0, 2.0, 3.0, 10.0}, Arrays.stream(resultado[0]).boxed().toArray(Double[]::new));
        assertArrayEquals(new Double[]{4.0, 5.0, 6.0, -2.0}, Arrays.stream(resultado[1]).boxed().toArray(Double[]::new));
        assertArrayEquals(new Double[]{7.0, 8.0, 9.0, 1.0}, Arrays.stream(resultado[2]).boxed().toArray(Double[]::new));
    }

    @Test
    void testEliminarAtipicos_sinAtipicos() {
        double[][] datos = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        double[][] resultado = OutlierRemover.eliminarAtipicos(datos);
        assertArrayEquals(datos[0], resultado[0]);
        assertArrayEquals(datos[1], resultado[1]);
        assertArrayEquals(datos[2], resultado[2]);
    }

    @Test
    void testEliminarAtipicos_columnaConMismosValores() {
        double[][] datos = {{2.0}, {2.0}, {2.0}};
        double[][] resultado = OutlierRemover.eliminarAtipicos(datos);
        assertArrayEquals(datos[0], resultado[0]);
        assertArrayEquals(datos[1], resultado[1]);
        assertArrayEquals(datos[2], resultado[2]);
    }

    @Test
    void testEliminarAtipicos_matrizVacia() {
        double[][] datos = {};
        double[][] resultado = OutlierRemover.eliminarAtipicos(datos);
        assertEquals(0, resultado.length);
    }
}
