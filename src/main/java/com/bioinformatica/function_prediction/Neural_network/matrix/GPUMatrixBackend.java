package com.bioinformatica.function_prediction.Neural_network.matrix;

import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.Arrays;
import java.util.function.Function;

public class GPUMatrixBackend implements Matrix {
    private static final long serialVersionUID = 1L;
    private INDArray data;

    public GPUMatrixBackend(int rows, int cols) {
        this.data = Nd4j.create(DataType.DOUBLE, rows, cols);
    }

    public GPUMatrixBackend(INDArray data) {
        this.data = data;
    }

    public GPUMatrixBackend(float[][] rawData) {
        this.data = Nd4j.createFromArray(rawData);
    }
    public GPUMatrixBackend(double[][] rawData) {
        this.data = Nd4j.createFromArray(rawData);
    }

    @Override
    public Matrix dot(Matrix a) {
        if (!(a instanceof GPUMatrixBackend))
            throw new IllegalArgumentException("Incompatible backend");

        INDArray result = data.mmul(((GPUMatrixBackend) a).data);
        return new GPUMatrixBackend(result);
    }

    @Override
    public Matrix add(Matrix a) {
        this.data.addi(((GPUMatrixBackend) a).data);
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix add(double scalar) {
        this.data.addi((float) scalar);
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix subtract(Matrix a) {
        this.data.subi(((GPUMatrixBackend) a).data);
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix multiply(double scalar) {
        this.data.muli((float) scalar);
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix multiply(Matrix a) {
        this.data.muli(((GPUMatrixBackend) a).data);
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix divide(Matrix a) {
        if (!(a instanceof GPUMatrixBackend)) {
            throw new IllegalArgumentException("Incompatible backend");
        }
        this.data.divi(((GPUMatrixBackend) a).data);
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix divide(double scalar) {
        this.data.divi((float) scalar);
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix rsub(double scalar) {
        return new GPUMatrixBackend(Nd4j.scalar(DataType.FLOAT, (float) scalar).sub(this.data));
    }

    @Override
    public Matrix rsub(Matrix a) {
        if (!(a instanceof GPUMatrixBackend)) {
            throw new IllegalArgumentException("Incompatible backend");
        }
        INDArray otherData = ((GPUMatrixBackend) a).data;
        return new GPUMatrixBackend(otherData.sub(this.data));
    }

    @Override
    public Matrix softmax() {
        this.data = Nd4j.nn.softmax(this.data, 1);
        return new GPUMatrixBackend(data);
    }

    @Override
    public Matrix copy() {
        return new GPUMatrixBackend(data.dup());
    }

    @Override
    public Matrix sigmoid() {
        return new GPUMatrixBackend(Transforms.sigmoid(data.dup()));
    }

    @Override
    public Matrix sigmoidDerivative() {
        INDArray sig = Transforms.sigmoid(data.dup());
        INDArray oneMinusSig = sig.rsub(1.0f); // 1 - sig
        return new GPUMatrixBackend(sig.mul(oneMinusSig));
    }

    @Override
    public Matrix relu() {
        return new GPUMatrixBackend(Transforms.relu(data.dup()));
    }

    @Override
    public Matrix reluDerivative() {
        INDArray relu = Transforms.relu(data.dup());
        INDArray derivative = relu.gt(0).castTo(DataType.FLOAT);
        return new GPUMatrixBackend(derivative);
    }

    @Override
    public Matrix fill(double value) {
        this.data.assign((float) value);
        return this;
    }

    @Override
    public Matrix log() {
        return new GPUMatrixBackend(Transforms.log(data.dup()));
    }

    @Override
    public Matrix round() {
        return new GPUMatrixBackend(Transforms.round(data.dup()));
    }

    @Override
    public double mean() {
        return data.meanNumber().floatValue();
    }

    @Override
    public int[] shape() {
        return new int[]{data.rows(), data.columns()};
    }

    @Override
    public void printShape() {
        System.out.println("Shape: " + data.shapeInfoToString());
    }

    @Override
    public Matrix slice(int from, int to) {
        return new GPUMatrixBackend(data.get(NDArrayIndex.interval(from, to), NDArrayIndex.all()));
    }

    @Override
    public Matrix meanRows() {
        return new GPUMatrixBackend(data.mean(1)); // Row-wise mean
    }

    @Override
    public Matrix applyFunction(Function<Double, Double> func) {
        INDArray result = data.dup();
        for (int i = 0; i < result.rows(); i++) {
            for (int j = 0; j < result.columns(); j++) {
                float val = result.getFloat(i, j);
                result.putScalar(i, j, func.apply((double) val).floatValue());
            }
        }
        this.data = result;
        return this; // Devolver la matriz actual
    }

    @Override
    public Matrix transpose() {
        return new GPUMatrixBackend(data.transpose());
    }

    public double[][] getRawData() {
        return data.toDoubleMatrix();
    }

    public static Matrix randn(int rows, int cols) {
        return new GPUMatrixBackend(Nd4j.randn(DataType.FLOAT, rows, cols));
    }

    public static Matrix zeros(int rows, int cols) {
        return new GPUMatrixBackend(Nd4j.zeros(DataType.FLOAT, rows, cols));
    }

    public static Matrix ones(int rows, int cols) {
        return new GPUMatrixBackend(Nd4j.ones(DataType.FLOAT, rows, cols));
    }

    @Override
    public Matrix reshape(int rows, int cols) {
        return new GPUMatrixBackend(data.reshape(rows, cols));
    }

    @Override
    public float[][] toFloatMatrix() {
        return data.toFloatMatrix();
    }

    @Override
    public int rows() {
        return data.rows();
    }

    @Override
    public int cols() {
        return data.columns();
    }

    @Override
    public boolean hasNaN() {
        return data.isNaN().any();
    }

    @Override
    public float[] toFloatArray() {
        return data.ravel().toFloatVector();
    }

    public double min() {
        return Nd4j.min(data).getDouble(0);
    }

    public double max() {
        return Nd4j.max(data).getDouble(0);
    }

    public Matrix broadcast(int[] shape) {
        INDArray array = data; // Suponiendo que tienes un método para obtener el INDArray subyacente
        long[] longShape = Arrays.stream(shape).mapToLong(i -> (long) i).toArray();
        INDArray broadcastedArray = array.broadcast(longShape);
        return new GPUMatrixBackend(broadcastedArray); // Suponiendo que tu constructor puede aceptar un INDArray
    }
}
