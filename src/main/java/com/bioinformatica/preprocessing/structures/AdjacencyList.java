package com.bioinformatica.preprocessing.structures;

import java.util.HashMap;
import java.util.Map;

public class AdjacencyList {
    private final Map<String, Map<String, Double>> grafo;

    public AdjacencyList() {
        this.grafo = new HashMap<>();
    }

    public void agregarRelacion(String nodo1, String nodo2, double peso) {
        grafo.computeIfAbsent(nodo1, k -> new HashMap<>()).put(nodo2, peso);
        grafo.computeIfAbsent(nodo2, k -> new HashMap<>()).put(nodo1, peso);
    }

    public Map<String, Double> obtenerVecinos(String nodo) {
        return grafo.getOrDefault(nodo, new HashMap<>());
    }

    public void imprimirRed() {
        for (Map.Entry<String, Map<String, Double>> entrada : grafo.entrySet()) {
            System.out.println(entrada.getKey() + " -> " + entrada.getValue());
        }
    }
}
