package com.bioinformatica.preprocessing.structures;

import java.util.Arrays;

public class GeneExpressionMatrix {
    private final double[][] matriz;
    private final String[] genes;
    private final String[] condiciones;

    public GeneExpressionMatrix(double[][] matriz, String[] genes, String[] condiciones) {
        this.matriz = matriz;
        this.genes = genes;
        this.condiciones = condiciones;
    }

    public double getValor(int fila, int columna) {
        return matriz[fila][columna];
    }

    public void setValor(int fila, int columna, double valor) {
        matriz[fila][columna] = valor;
    }

    public int getNumeroGenes() {
        return genes.length;
    }

    public int getNumeroCondiciones() {
        return condiciones.length;
    }

    public void imprimirMatriz() {
        for (double[] fila : matriz) {
            System.out.println(Arrays.toString(fila));
        }
    }
}
