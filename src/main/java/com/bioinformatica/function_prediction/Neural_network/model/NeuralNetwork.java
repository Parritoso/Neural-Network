package com.bioinformatica.function_prediction.Neural_network.model;

import com.bioinformatica.function_prediction.Neural_network.layer.DenseLayer;
import com.bioinformatica.function_prediction.Neural_network.loss.LossFunction;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.optimizer.Optimizer;

import java.util.Arrays;
import java.util.List;

public class NeuralNetwork {
    private List<DenseLayer> layers;
    private LossFunction lossFunction;
    private Optimizer optimizer;
    private double learningRate;

    public NeuralNetwork(List<DenseLayer> layers, LossFunction lossFunction, Optimizer optimizer, double learningRate) {
        this.layers = layers;
        this.lossFunction = lossFunction;
        this.optimizer = optimizer;
        this.learningRate = learningRate;
    }

    public Matrix predict(Matrix input) {
        Matrix output = input;
        for (DenseLayer layer : layers) {
            output = layer.forward(output);
        }
        return output;
    }

    public double trainStep(Matrix input, Matrix target) {
        // Forward pass
        Matrix output = input;
//        System.out.println("output shape: "+ Arrays.toString(output.shape()) +" ");
//        output.printShape();
//        System.out.println("\n--- Forward ---\n");
        int j=0;
        for (DenseLayer layer : layers) {
//            System.out.println("\n+++ Forward Layer " +(j++)+ " +++\n");
            output = layer.forward(output);
//            System.out.println("\n+++++++++++++++++++++++\n");
        }
//        System.out.println("\n---------------\n");

        // Calcular la pérdida
//        System.out.println("Shape of output: " + Arrays.toString(output.shape()));
//        System.out.println("Shape of target: " + Arrays.toString(target.shape()));
//        if (output.hasNaN()) System.out.println("Output contiene NaNs");
        double loss = lossFunction.calculate(output, target);
        if(Double.isNaN(loss)) System.out.println("loss is NaN!");

        // Backward pass
        Matrix outputGradient = lossFunction.derivative(output, target);
//        if (outputGradient.hasNaN()) System.out.println("Gradiente de pérdida tiene NaNs");
//        System.out.println("\n--- Backward ---\n");
        for (int i = layers.size() - 1; i >= 0; i--) {
//            System.out.println("\n+++ Backward Layer " +i+ " +++\n");
            DenseLayer layer = layers.get(i);
            outputGradient = layer.backward(outputGradient, learningRate);
//            System.out.println("\n+++++++++++++++++++++++\n");
        }
//        System.out.println("\n---------------\n");

        // Actualizar los pesos y biases
        for (DenseLayer layer : layers) {
            // Aquí podríamos pasar los gradientes calculados en el backward pass si los almacenáramos
            // Por ahora, el backward de cada capa actualiza sus propios pesos y biases.
            // Si el optimizador necesita los gradientes explícitamente, habría que modificarl el backward.
        }

//        System.out.println("\n======================================\n");
        return loss;
    }

    public List<DenseLayer> getLayers() {
        return layers;
    }

    public LossFunction getLossFunction(){
        return lossFunction;
    }

    public double getLearningRate(){return learningRate;}

    public NeuralNetwork clone() {
        // Debe ser un clon profundo, incluyendo pesos, capas, etc.
        return new NeuralNetwork(this.layers,this.lossFunction,this.optimizer,this.learningRate); // Asume constructor copia
    }

    public void setOptimizer(Optimizer optimizer){
        this.optimizer = optimizer;
    }

    public void setLearningRate(double learningRate){
        this.learningRate = learningRate;
    }
}
