package com.bioinformatica.function_prediction.Neural_network.layer;

import com.bioinformatica.function_prediction.Neural_network.matrix.GPUMatrixBackend;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;

import java.util.Arrays;

public class DenseLayer {
    private Matrix weights;
    private Matrix biases;
    private Matrix lastInput;
    private Matrix lastOutput;
    private Matrix lastZ;
    private Activation activation;
    private double dropoutRate = 0.0;
    private Matrix dropoutMask;
    private boolean training = true;
    private double gradientClipThreshold = 1.0; // Nuevo parámetro
    private double maxNorm = 1.0; // Nuevo parámetro

    public DenseLayer(int inputSize, int outputSize, Activation activation) {
        this.weights = MatrixFactory.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize));//GPUMatrixBackend.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize)); // He inicializado con He
        this.biases = MatrixFactory.zeros(outputSize,1);//GPUMatrixBackend.zeros(outputSize, 1);
        this.activation = activation;
    }

    public DenseLayer(int inputSize, int outputSize, Activation activation, double dropoutRate) {
        this.weights = MatrixFactory.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize));//GPUMatrixBackend.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize)); // He inicializado con He
        this.biases = MatrixFactory.zeros(outputSize,1);//GPUMatrixBackend.zeros(outputSize, 1);
        this.activation = activation;
        this.dropoutRate = dropoutRate;
    }

    public DenseLayer(int inputSize, int outputSize, Activation activation, double dropoutRate, double gradientClipThreshold) {
        this.weights = MatrixFactory.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize));//GPUMatrixBackend.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize)); // He inicializado con He
        this.biases = MatrixFactory.zeros(outputSize,1);//GPUMatrixBackend.zeros(outputSize, 1);
        this.activation = activation;
        this.dropoutRate = dropoutRate;
        this.gradientClipThreshold = gradientClipThreshold;
    }

    public DenseLayer(int inputSize, int outputSize, Activation activation, double dropoutRate, double gradientClipThreshold, double maxNorm) {
        this.weights = MatrixFactory.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize));//GPUMatrixBackend.randn(outputSize, inputSize).multiply(Math.sqrt(2.0 / inputSize)); // He inicializado con He
        this.biases = MatrixFactory.zeros(outputSize,1);//GPUMatrixBackend.zeros(outputSize, 1);
        this.activation = activation;
        this.dropoutRate = dropoutRate;
        this.gradientClipThreshold = gradientClipThreshold;
        this.maxNorm = maxNorm;
    }

    public Matrix forward(Matrix input) {
//        System.out.println("input shape: "+ Arrays.toString(input.shape())+" ");
//        input.printShape();
        this.lastInput = input.copy();
//        System.out.println("Input has NaN: " + input.hasNaN());
//        System.out.println("Weights has NaN: " + weights.hasNaN());
//        System.out.println("Biases has NaN: " + biases.hasNaN());
        Matrix z = weights.dot(input).add(biases);
        this.lastZ = z.copy();
//        System.out.println("z after dot has NaN: " + z.hasNaN());
        this.lastOutput = activation.apply(z);
//        System.out.println("Activated output has NaN: " + lastOutput.hasNaN());

        if (dropoutRate > 0 && training) {
            dropoutMask = MatrixFactory.rand(lastOutput.rows(), lastOutput.cols())
                    .applyFunction(x -> (double) (x < dropoutRate ? 0.0F : 1.0F)); // Binaria

            if (dropoutMask.hasNaN()) {
//                System.out.println("Dropout mask contiene NaNs");
            }

            lastOutput = lastOutput.multiply(dropoutMask); // Aplica máscara

//            System.out.println("Después de aplicar dropoutMask, NaN: " + lastOutput.hasNaN());

            lastOutput = lastOutput.multiply(1.0 / (1.0 - dropoutRate)); // Escala inversa
        } else {
            dropoutMask = null; // explícitamente para no usarla en backward
        }

//        System.out.println("lastOutput: "+ Arrays.toString(lastOutput.shape())+" ");
//        lastOutput.printShape();
        return this.lastOutput;
    }

//    public Matrix backward(Matrix outputGradient, double learningRate) {
////        Matrix activationDerivative = activation.derivative(lastOutput);
////        if (activationDerivative.hasNaN()) System.out.println("Derivada de activación tiene NaNs");
////        Matrix dz = outputGradient.multiply(activationDerivative);
////        if (dz.hasNaN()) System.out.println("dz tiene NaNs antes de actualizar pesos");
////
////        if (dropoutRate > 0 && training && dropoutMask != null) {
////            dz = dz.multiply(dropoutMask); // Aplica misma máscara al gradiente
////        }
////
////        Matrix weightGradient = dz.dot(lastInput.transpose());
////        if (weightGradient.hasNaN()) System.out.println("weightGradient tiene NaNs");
////
////        // REGULARIZACIÓN L2
////        double lambda = 0.001; // Puedes hacerlo configurable si quieres
////        Matrix l2Term = weights.copy().multiply(lambda);
////        if (l2Term.hasNaN()) System.out.println("L2 tiene NaNs");
////        weightGradient = weightGradient.add(l2Term);
////
////        weights.subtract(weightGradient.multiply(learningRate));
////        Matrix meanDz = dz.multiply(learningRate).meanRows();
////        biases.subtract(meanDz.reshape(1, (int) meanDz.shape()[1]).transpose().multiply(learningRate));
////
////        return weights.transpose().dot(dz); // Gradiente para la capa anterior
//
//        // Usa lastZ en lugar de lastOutput para la derivada de activación
////        if (lastZ.hasNaN()) {
////            System.out.println("lastZ tiene NaNs antes de la derivada de la activación");
////        }
////        Matrix activationDerivative = activation.derivative(lastZ);
////        if (activationDerivative.hasNaN()) System.out.println("activationDerivative tiene NaNs");
////
////        if(outputGradient.hasNaN()){
////            System.out.println("outputGradient tiene NaNs");
////        }
////
////        System.out.print("outputGradient shape: ");
////        outputGradient.printShape();
////        System.out.print("activationDerivative shape: ");
////        activationDerivative.printShape();
////        System.out.println("Min outputGradient: " + outputGradient.min());
////        System.out.println("Max outputGradient: " + outputGradient.max());
////        System.out.println("Min activationDerivative: " + activationDerivative.min());
////        System.out.println("Max activationDerivative: " + activationDerivative.max());
////
////
////        Matrix dz = outputGradient.multiply(activationDerivative);
////        if (dz.hasNaN()) System.out.println("dz tiene NaNs antes de actualizar pesos");
////
////        if (dropoutMask != null && dropoutMask.hasNaN()) {
////            System.out.println("dropoutMask tiene NaNs");
////        }
////        // Aplica la misma máscara de dropout si está activado
////        if (dropoutRate > 0 && training && dropoutMask != null) {
////            dz = dz.multiply(dropoutMask);
////        }
////
////        // Gradiente de los pesos
////        Matrix weightGradient = dz.dot(lastInput.transpose());
////        if (weightGradient.hasNaN()) System.out.println("weightGradient tiene NaNs");
////
////        // Regularización L2
////        double lambda = 0.001;
////        Matrix l2Term = weights.copy().multiply(lambda);
////        if (l2Term.hasNaN()) System.out.println("L2 tiene NaNs");
////
////        weightGradient = weightGradient.add(l2Term);
////
////        // Actualización de pesos y biases
////        weights.subtract(weightGradient.multiply(learningRate));
////
////        Matrix meanDz = dz.multiply(learningRate).meanRows();
////        biases.subtract(meanDz.reshape(1, (int) meanDz.shape()[1]).transpose().multiply(learningRate));
////
////        if (dz.hasNaN()) {
////            System.out.println("dz tiene NaNs después de todas las operaciones");
////        }
////
////        // Devuelve el gradiente para la capa anterior
////        return weights.transpose().dot(dz);
//
//        if (lastZ.hasNaN()) {
//            System.out.println("lastZ tiene NaNs antes de la derivada de la activación");
//        }
//        Matrix activationDerivative = activation.derivative(lastZ);
//        if (activationDerivative.hasNaN()) System.out.println("activationDerivative tiene NaNs");
//
//        if(outputGradient.hasNaN()){
//            System.out.println("outputGradient tiene NaNs");
//        }
//
//        System.out.print("outputGradient shape: ");
//        outputGradient.printShape();
//        System.out.print("activationDerivative shape: ");
//        activationDerivative.printShape();
//        System.out.println("Min outputGradient: " + outputGradient.min());
//        System.out.println("Max outputGradient: " + outputGradient.max());
//        System.out.println("Min activationDerivative: " + activationDerivative.min());
//        System.out.println("Max activationDerivative: " + activationDerivative.max());
//
//        Matrix dz = outputGradient.multiply(activationDerivative);
//        if (dz.hasNaN()) System.out.println("dz tiene NaNs antes de actualizar pesos");
//
//        if (dropoutMask != null && dropoutMask.hasNaN()) {
//            System.out.println("dropoutMask tiene NaNs");
//        }
//
//        if (dropoutRate > 0 && training && dropoutMask != null) {
//            dz = dz.multiply(dropoutMask);
//        }
//
//        // Gradientes
//        Matrix weightGradient = dz.copy().dot(lastInput.transpose());
//        if (weightGradient.hasNaN()) System.out.println("weightGradient tiene NaNs");
//
//        // Regularización L2
//        double lambda = 0.001;
//        Matrix l2Term = weights.copy().multiply(lambda);
//        if (l2Term.hasNaN()) System.out.println("L2 tiene NaNs");
//        weightGradient = weightGradient.add(l2Term);
//
//        // Actualización de pesos y biases
//        weights.subtract(weightGradient.multiply(learningRate));
//
//        Matrix meanDz = dz.multiply(learningRate).meanRows();
//        biases.subtract(meanDz.reshape(1, (int) meanDz.shape()[1]).transpose().multiply(learningRate));
//
//        // Gradiente para la capa anterior
//        return weights.transpose().dot(dz);
//    }

    public Matrix backward(Matrix outputGradient, double learningRate){
        if (lastZ.hasNaN()) {
//            System.out.println("lastZ tiene NaNs antes de la derivada de la activación");
        }
        Matrix activationDerivative = activation.derivative(lastZ);
//        if (activationDerivative.hasNaN()) System.out.println("activationDerivative tiene NaNs");

        if(outputGradient.hasNaN()){
//            System.out.println("outputGradient tiene NaNs");
        }

//        System.out.print("outputGradient shape: ");
//        outputGradient.printShape();
//        System.out.print("activationDerivative shape: ");
//        activationDerivative.printShape();
//        System.out.println("Min outputGradient: " + outputGradient.min());
//        System.out.println("Max outputGradient: " + outputGradient.max());
//        System.out.println("Min activationDerivative: " + activationDerivative.min());
//        System.out.println("Max activationDerivative: " + activationDerivative.max());

        Matrix dz = outputGradient.multiply(activationDerivative);
//        if (dz.hasNaN()) System.out.println("dz tiene NaNs antes de actualizar pesos");

        if (dropoutRate > 0 && training && dropoutMask != null) {
            dz = dz.multiply(dropoutMask);
        }

        // Gradientes
        Matrix weightGradient = dz.copy().dot(lastInput.transpose());
//        if (weightGradient.hasNaN()) System.out.println("weightGradient tiene NaNs antes del clipping");

        // --- Gradient Clipping Global ---
        double globalNorm = 0;
        double[][] rawWeightGradient = weightGradient.getRawData();
        for (double[] row : rawWeightGradient) {
            for (double value : row) {
                globalNorm += value * value;
            }
        }
        globalNorm = Math.sqrt(globalNorm);

        if (globalNorm > gradientClipThreshold) {
            double clipFactor = gradientClipThreshold / globalNorm;
            weightGradient = weightGradient.multiply(clipFactor);
//            System.out.println("Gradientes recortados por un factor de: " + clipFactor);
        }
//        if (weightGradient.hasNaN()) System.out.println("weightGradient tiene NaNs después del clipping");
        // --- Fin Gradient Clipping Global ---

        // Regularización L2
        double lambda = 0.001;
        Matrix l2Term = weights.copy().multiply(lambda);
//        if (l2Term.hasNaN()) System.out.println("L2 tiene NaNs");
        weightGradient = weightGradient.add(l2Term);

        // Actualización de pesos y biases
        weights.subtract(weightGradient.multiply(learningRate));

        // --- Normalización de Pesos (Max-Norm) ---
        double weightsNorm = 0;
        double[][] rawWeights = weights.getRawData();
        for (double[] row : rawWeights) {
            for (double value : row) {
                weightsNorm += value * value;
            }
        }
        weightsNorm = Math.sqrt(weightsNorm);

        if (weightsNorm > maxNorm) {
            double scaleFactor = maxNorm / weightsNorm;
            weights = weights.multiply(scaleFactor);
//            System.out.println("Pesos normalizados por un factor de: " + scaleFactor);
        }
//        if (weights.hasNaN()) System.out.println("weights tiene NaNs después de la normalización");
        // --- Fin Normalización de Pesos ---

        Matrix meanDz = dz.multiply(learningRate).meanRows();
        biases.subtract(meanDz.reshape(1, (int) meanDz.shape()[1]).transpose().multiply(learningRate));

        // Gradiente para la capa anterior
        if(weights.hasNaN()) System.out.println("weights tiene NaNs al final");
        if(biases.hasNaN()) System.out.println("biases tiene NaNs al final");
        if(dz.hasNaN()) System.out.println("dz tiene NaNs al final");
        if(meanDz.hasNaN()) System.out.println("dz tiene NaNs al final");
        return weights.transpose().dot(dz);
    }
    public Matrix getWeights() {
        return weights;
    }

    public Matrix getBiases() {
        return biases;
    }

    public void setWeights(Matrix weights){
        this.weights = weights;
    }

    public void setBiases(Matrix biases){
        this.biases = biases;
    }

    public int getOutputSize() {
        return weights.shape()[0];
    }

    public void setDropoutRate(double rate) {
        if (rate < 0.0 || rate >= 1.0) {
            throw new IllegalArgumentException("Dropout rate debe estar entre 0.0 (inclusive) y 1.0 (exclusivo)");
        }
        this.dropoutRate = rate;
    }

    public void setTraining(boolean training) {
        this.training = training;
    }
}
