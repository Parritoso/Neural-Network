package com.bioinformatica.function_prediction.Neural_network.model;

import com.bioinformatica.function_prediction.Neural_network.layer.Activation;
import com.bioinformatica.function_prediction.Neural_network.layer.DenseLayer;
import com.bioinformatica.function_prediction.Neural_network.loss.LossFunction;
import com.bioinformatica.function_prediction.Neural_network.optimizer.Optimizer;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.NaN;

public class NetworkBuilder {
    private int inputSize;
    private int outputSize;
    private List<Integer> hiddenLayerSizes = new ArrayList<>();
    private List<Activation> hiddenActivations = new ArrayList<>();
    private List<Double> hiddenLayerDropOut = new ArrayList<>();
    private Activation outputActivation;
    private LossFunction lossFunction;
    private Optimizer optimizer;
    private double learningRate = 0.01;
    private double gradientClipThreshold = NaN;
    private double maxNorm = NaN;

    public NetworkBuilder setInputSize(int inputSize) {
        this.inputSize = inputSize;
        return this;
    }

    public NetworkBuilder setOutputSize(int outputSize) {
        this.outputSize = outputSize;
        return this;
    }

    public NetworkBuilder addHiddenLayer(int size, Activation activation, double dropout) {
        this.hiddenLayerSizes.add(size);
        this.hiddenActivations.add(activation);
        this.hiddenLayerDropOut.add(dropout);
        return this;
    }

    public NetworkBuilder addHiddenLayer(int size, Activation activation) {
        this.hiddenLayerSizes.add(size);
        this.hiddenActivations.add(activation);
        return this;
    }

    public NetworkBuilder setOutputActivation(Activation outputActivation) {
        this.outputActivation = outputActivation;
        return this;
    }

    public NetworkBuilder setLossFunction(LossFunction lossFunction) {
        this.lossFunction = lossFunction;
        return this;
    }

    public NetworkBuilder setOptimizer(Optimizer optimizer) {
        this.optimizer = optimizer;
        return this;
    }

    public NetworkBuilder setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public NetworkBuilder setGradientClipping(double gradientClipThreshold) {
        this.gradientClipThreshold = gradientClipThreshold;
        return this;
    }

    public NetworkBuilder setMaxNorm(double maxNorm) {
        this.maxNorm = maxNorm;
        return this;
    }

    public NeuralNetwork build() {
        if (inputSize <= 0 || outputSize <= 0 || hiddenLayerSizes.size() != hiddenActivations.size() || outputActivation == null || lossFunction == null || optimizer == null) {
            throw new IllegalStateException("Network configuration is incomplete.");
        }

        List<DenseLayer> layers = new ArrayList<>();
        int currentSize = inputSize;

        for (int i = 0; i < hiddenLayerSizes.size(); i++) {
            if(!Double.isNaN(gradientClipThreshold) && !Double.isNaN(maxNorm) && !hiddenLayerDropOut.isEmpty()){
                layers.add(new DenseLayer(currentSize, hiddenLayerSizes.get(i), hiddenActivations.get(i), hiddenLayerDropOut.get(i),gradientClipThreshold,maxNorm));
            } else {
                layers.add(new DenseLayer(currentSize, hiddenLayerSizes.get(i), hiddenActivations.get(i)));
            }
            currentSize = hiddenLayerSizes.get(i);
        }

        layers.add(new DenseLayer(currentSize, outputSize, outputActivation));

        return new NeuralNetwork(layers, lossFunction, optimizer, learningRate);
    }
}
