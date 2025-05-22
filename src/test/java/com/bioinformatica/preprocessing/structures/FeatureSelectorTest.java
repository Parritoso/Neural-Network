package com.bioinformatica.preprocessing.structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureSelectorTest {
    @Test
    void testSeleccionarConANOVA_casoSencillo() {
        double[][] datos = {{1.0, 2.0, 3.0, 4.0}, {5.0, 6.0, 7.0, 8.0}, {2.0, 3.0, 4.0, 5.0}};
        int[] etiquetas = {0, 1, 0, 1};
        double umbralP = 0.1;
        double[][] resultado = FeatureSelector.seleccionarConANOVA(datos, etiquetas, umbralP);
        // En este caso, es difícil predecir el resultado exacto sin calcular el p-valor.
        // La prueba verifica que no haya errores y que el resultado sea una matriz.
        assertNotNull(resultado);
    }

    @Test
    void testSeleccionarConANOVA_sinCaracteristicasSignificativas() {
        double[][] datos = {{1.0, 1.1, 1.2, 1.3}, {2.0, 2.1, 2.2, 2.3}, {1.5, 1.6, 1.7, 1.8}};
        int[] etiquetas = {0, 0, 1, 1}; // Ahora ambos grupos tienen 2 valores
        double umbralP = 0.01;
        double[][] resultado = FeatureSelector.seleccionarConANOVA(datos, etiquetas, umbralP);
        assertEquals(0, resultado.length);
    }

    @Test
    void testSeleccionarConANOVA_datosVacios() {
        double[][] datos = {};
        int[] etiquetas = {};
        double umbralP = 0.05;
        double[][] resultado = FeatureSelector.seleccionarConANOVA(datos, etiquetas, umbralP);
        assertEquals(0, resultado.length);
    }

    @Test
    void testSeleccionarConANOVA_etiquetasInconsistentes() {
        double[][] datos = {{1.0}, {2.0}};
        int[] etiquetas = {0, 1, 0}; // Más etiquetas que datos
        double umbralP = 0.05;
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> FeatureSelector.seleccionarConANOVA(datos, etiquetas, umbralP));
    }
}
