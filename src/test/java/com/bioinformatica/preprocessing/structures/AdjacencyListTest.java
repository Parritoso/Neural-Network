package com.bioinformatica.preprocessing.structures;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdjacencyListTest {
    @Test
    void testAgregarRelacion_casoSencillo() {
        AdjacencyList grafo = new AdjacencyList();
        grafo.agregarRelacion("A", "B", 0.5);
        Map<String, Double> vecinosA = grafo.obtenerVecinos("A");
        Map<String, Double> vecinosB = grafo.obtenerVecinos("B");
        assertEquals(1, vecinosA.size());
        assertEquals(1, vecinosB.size());
        assertEquals(0.5, vecinosA.get("B"), 0.001);
        assertEquals(0.5, vecinosB.get("A"), 0.001);
    }

    @Test
    void testAgregarRelacion_multiplesVecinos() {
        AdjacencyList grafo = new AdjacencyList();
        grafo.agregarRelacion("A", "B", 0.5);
        grafo.agregarRelacion("A", "C", 0.8);
        Map<String, Double> vecinosA = grafo.obtenerVecinos("A");
        assertEquals(2, vecinosA.size());
        assertEquals(0.5, vecinosA.get("B"), 0.001);
        assertEquals(0.8, vecinosA.get("C"), 0.001);
    }

    @Test
    void testObtenerVecinos_nodoNoExistente() {
        AdjacencyList grafo = new AdjacencyList();
        Map<String, Double> vecinos = grafo.obtenerVecinos("Z");
        assertTrue(vecinos.isEmpty());
    }

    @Test
    void testAgregarRelacion_relacionExistenteActualizada() {
        AdjacencyList grafo = new AdjacencyList();
        grafo.agregarRelacion("A", "B", 0.5);
        grafo.agregarRelacion("A", "B", 0.9);
        Map<String, Double> vecinosA = grafo.obtenerVecinos("A");
        assertEquals(1, vecinosA.size());
        assertEquals(0.9, vecinosA.get("B"), 0.001);
    }
}
