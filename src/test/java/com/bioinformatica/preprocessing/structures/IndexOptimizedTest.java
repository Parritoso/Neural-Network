package com.bioinformatica.preprocessing.structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IndexOptimizedTest {
    @Test
    void testAgregarIndice_yBuscar() {
        IndexOptimized indice = new IndexOptimized(3);
        indice.agregarIndice("clave1", 10.0);
        Double valor = indice.buscar("clave1");
        assertEquals(10.0, valor);
    }

    @Test
    void testBuscar_claveNoExistente() {
        IndexOptimized indice = new IndexOptimized(3);
        Double valor = indice.buscar("claveNoExiste");
        assertNull(valor);
    }

    @Test
    void testAgregarMultiplesIndices_yBuscar() {
        IndexOptimized indice = new IndexOptimized(4);
        indice.agregarIndice("A", 1.0);
        indice.agregarIndice("B", 2.0);
        indice.agregarIndice("C", 3.0);
        assertEquals(1.0, indice.buscar("A"));
        assertEquals(2.0, indice.buscar("B"));
        assertEquals(3.0, indice.buscar("C"));
        assertNull(indice.buscar("D"));
    }

    @Test
    void testImprimirIndices_noLanzaExcepcion() {
        IndexOptimized indice = new IndexOptimized(3);
        indice.agregarIndice("test", 5.0);
        assertDoesNotThrow(indice::imprimirIndices);
    }
}
