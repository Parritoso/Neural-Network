package com.bioinformatica.preprocessing.cleaning;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneFilterTest {
    @Test
    void testFiltrarGenes_casoSencillo() {
        double[][] datos = {{1.0, 2.0, 3.0}, {0.1, 0.2, 0.3}, {5.0, 6.0, 7.0}};
        double umbralExpresion = 0.5;
        double umbralVarianza = 0.1;
        double[][] resultado = GeneFilter.filtrarGenes(datos, umbralExpresion, umbralVarianza);
        assertEquals(2, resultado.length);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, resultado[0]);
        assertArrayEquals(new double[]{5.0, 6.0, 7.0}, resultado[1]);
    }

    @Test
    void testFiltrarGenes_sinGenesQueCumplanUmbral() {
        double[][] datos = {{0.1, 0.2, 0.3}, {0.05, 0.06, 0.07}};
        double umbralExpresion = 1.0;
        double umbralVarianza = 0.5;
        double[][] resultado = GeneFilter.filtrarGenes(datos, umbralExpresion, umbralVarianza);
        assertEquals(0, resultado.length);
    }

    @Test
    void testFiltrarGenes_genesConMediaJustoEnUmbral() {
        double[][] datos = {{1.0, 1.0, 1.0}};
        double umbralExpresion = 1.0;
        double umbralVarianza = 0.0;
        double[][] resultado = GeneFilter.filtrarGenes(datos, umbralExpresion, umbralVarianza);
        assertEquals(0, resultado.length);
    }

    @Test
    void testFiltrarGenes_genesConVarianzaJustoEnUmbral() {
        double[][] datos = {{1.0, 2.0}, {2.0, 1.0}};
        double umbralExpresion = 1.0;
        double umbralVarianza = 0.5;
        double[][] resultado = GeneFilter.filtrarGenes(datos, umbralExpresion, umbralVarianza);
        assertEquals(0, resultado.length);
    }
}
