package com.bioinformatica;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class TestCUDA {

    @Test
    public void testBackendName() {
        String backendName = Nd4j.getExecutioner().getClass().getSimpleName();
        System.out.println("ND4J está utilizando el backend: " + backendName);
        assertNotNull(backendName);
    }

    @Test
    public void testArrayCreation() {
        INDArray array = Nd4j.linspace(1, 10, 10);
        System.out.println("Array original:\n" + array);
        assertEquals(10, array.length());
        assertEquals(1.0, array.getDouble(0), 1e-6);
        assertEquals(10.0, array.getDouble(9), 1e-6);
    }

    @Test
    public void testMultiplication() {
        INDArray array = Nd4j.linspace(1, 10, 10);
        INDArray array2 = Nd4j.ones(10).mul(2);
        INDArray product = array.mul(array2);
        System.out.println("Producto:\n" + product);
        assertEquals(array.getDouble(0) * 2, product.getDouble(0), 1e-6);
        assertEquals(array.getDouble(9) * 2, product.getDouble(9), 1e-6);
    }

    @Test
    public void testCUDAExecutionerDetection() {
        String execName = Nd4j.getExecutioner().getClass().getSimpleName().toLowerCase();
        if (execName.contains("cuda")) {
            System.out.println("ND4J está usando GPU con CUDA.");
        } else {
            System.out.println("ND4J está usando CPU (no CUDA). Verifica la instalación de CUDA y dependencias.");
        }
        assertNotNull(execName);
    }
}