package com.bioinformatica.preprocessing.structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneExpressionMatrixTest {
    @Test
    void testGetValor_casoSencillo() {
        double[][] matriz = {{1.0, 2.0}, {3.0, 4.0}};
        String[] genes = {"gene1", "gene2"};
        String[] condiciones = {"cond1", "cond2"};
        GeneExpressionMatrix gem = new GeneExpressionMatrix(matriz, genes, condiciones);
        assertEquals(3.0, gem.getValor(1, 0), 0.001);
    }

    @Test
    void testSetValor_casoSencillo() {
        double[][] matriz = {{1.0, 2.0}, {3.0, 4.0}};
        String[] genes = {"gene1", "gene2"};
        String[] condiciones = {"cond1", "cond2"};
        GeneExpressionMatrix gem = new GeneExpressionMatrix(matriz, genes, condiciones);
        gem.setValor(0, 1, 5.0);
        assertEquals(5.0, gem.getValor(0, 1), 0.001);
    }

    @Test
    void testGetNumeroGenes() {
        double[][] matriz = {{1.0}, {2.0}, {3.0}};
        String[] genes = {"gene1", "gene2", "gene3"};
        String[] condiciones = {"cond1"};
        GeneExpressionMatrix gem = new GeneExpressionMatrix(matriz, genes, condiciones);
        assertEquals(3, gem.getNumeroGenes());
    }

    @Test
    void testGetNumeroCondiciones() {
        double[][] matriz = {{1.0, 2.0}};
        String[] genes = {"gene1"};
        String[] condiciones = {"cond1", "cond2"};
        GeneExpressionMatrix gem = new GeneExpressionMatrix(matriz, genes, condiciones);
        assertEquals(2, gem.getNumeroCondiciones());
    }

    @Test
    void testImprimirMatriz_noLanzaExcepcion() {
        double[][] matriz = {{1.0, 2.0}, {3.0, 4.0}};
        String[] genes = {"gene1", "gene2"};
        String[] condiciones = {"cond1", "cond2"};
        GeneExpressionMatrix gem = new GeneExpressionMatrix(matriz, genes, condiciones);
        assertDoesNotThrow(gem::imprimirMatriz);
    }
}
