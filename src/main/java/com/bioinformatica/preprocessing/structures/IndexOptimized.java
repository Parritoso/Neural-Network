package com.bioinformatica.preprocessing.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementación de un índice optimizado con Arbol B+ y HashMap.
 * Se usa HashMap para accesos rápidos y un Arbol B+ para almacenamiento ordenado.
 */
public class IndexOptimized {
    private final Map<String, Double> hashMap;
    private final BPlusTree<String, Double> bPlusTree;

    public IndexOptimized(int orden) {
        this.hashMap = new HashMap<>();
        this.bPlusTree = new BPlusTree<>(orden);
    }

    public void agregarIndice(String clave, double valor) {
        hashMap.put(clave, valor);
        bPlusTree.insert(clave, valor);
    }

    public Double buscar(String clave) {
        return hashMap.getOrDefault(clave, bPlusTree.search(clave));
    }

    public void imprimirIndices() {
        System.out.println("HashMap: " + hashMap);
        System.out.println("B+ Tree: ");
        bPlusTree.printTree();
    }
}
/**
 * Implementación básica de un Arbol B+.
 */
class BPlusTree<K extends Comparable<K>, V> {
    private final int orden;
    private final TreeMap<K, V> tree;

    public BPlusTree(int orden) {
        this.orden = orden;
        this.tree = new TreeMap<>();
    }

    public void insert(K key, V value) {
        tree.put(key, value);
        if (tree.size() > orden) {
            tree.pollFirstEntry(); // Simulación de división de nodos
        }
    }

    public V search(K key) {
        return tree.get(key);
    }

    public void printTree() {
        System.out.println(tree);
    }
}

