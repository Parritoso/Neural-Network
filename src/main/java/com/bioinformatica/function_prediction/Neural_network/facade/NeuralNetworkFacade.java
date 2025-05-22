package com.bioinformatica.function_prediction.Neural_network.facade;

import com.bioinformatica.function_prediction.Neural_network.loss.FocalLoss;
import com.bioinformatica.function_prediction.Neural_network.matrix.GPUMatrixBackend;
import com.bioinformatica.function_prediction.Neural_network.layer.Activation;
import com.bioinformatica.function_prediction.Neural_network.loss.BinaryCrossEntropy;
import com.bioinformatica.function_prediction.Neural_network.loss.LossFunction;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;
import com.bioinformatica.function_prediction.Neural_network.model.NetworkBuilder;
import com.bioinformatica.function_prediction.Neural_network.model.NeuralNetwork;
import com.bioinformatica.function_prediction.Neural_network.optimizer.Adam;
import com.bioinformatica.function_prediction.Neural_network.optimizer.Optimizer;
import com.bioinformatica.function_prediction.Neural_network.optimizer.ThresholdAdjuster;
import com.bioinformatica.function_prediction.Neural_network.trainer.NetworkTrainer;
import com.bioinformatica.function_prediction.Neural_network.model.NetworkSaverLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeuralNetworkFacade {
    private NeuralNetwork network;
    private NetworkTrainer trainer;
    private Map<String, Object> hyperparameters = new HashMap<>();
    private long trainingTime;

    public void buildNetwork(int inputSize, int outputSize, double bestLR) {
        if (!loadNetworkFacade(inputSize, outputSize)) { // Intenta cargar la red y si falla...
            // ...crea una nueva red
            Optimizer optimizer = new Adam(bestLR);
            LossFunction lossFunction = new BinaryCrossEntropy();

            NetworkBuilder builder = new NetworkBuilder()
                    .setInputSize(inputSize)
                    .setOutputSize(outputSize)
                    .addHiddenLayer(512, Activation.RELU)
                    .addHiddenLayer(256, Activation.RELU)
                    .addHiddenLayer(128, Activation.RELU)
//                    .addHiddenLayer(512, Activation.SWISH)
//                    .addHiddenLayer(256, Activation.SWISH)
//                    .addHiddenLayer(128, Activation.SWISH)
                    .setOutputActivation(Activation.SIGMOID)
                    .setLossFunction(lossFunction)
//                    .setLossFunction(new FocalLoss(2.0, 0.25)) // gamma = 2, alpha = 0.25
                    .setOptimizer(optimizer)
                    .setLearningRate(bestLR);

            this.network = builder.build();
            this.trainer = new NetworkTrainer(this.network); // Asegúrate de que el trainer se actualice si se reconstruye la red
            System.out.println("Se creó una nueva red neuronal.");
        } else {
            NetworkSaverLoader.LoadResult loadResult = this.loadLoadResult();
            this.trainer = new NetworkTrainer(this.network);
            if(loadResult!=null){
                this.trainingTime = loadResult.getTrainingTime();
                this.trainer.setLossHistory(loadResult.getLossHistory());
                this.trainer.setEpochs(loadResult.getEpochs());
                this.trainer.setBatchSize(loadResult.getBatchSize());
            }
            System.out.println("Se cargó una red neuronal pre-entrenada.");
        }
    }
    public void buildNetwork(int inputSize, int outputSize, double bestLR, double bestdropout, double bestGradientClipping, double bestMaxNorm) {
        if (!loadNetworkFacade(inputSize, outputSize)) { // Intenta cargar la red y si falla...
            // ...crea una nueva red
            Optimizer optimizer = new Adam(bestLR);
            LossFunction lossFunction = new BinaryCrossEntropy();

            NetworkBuilder builder = new NetworkBuilder()
                    .setInputSize(inputSize)
                    .setOutputSize(outputSize)
                    .addHiddenLayer(512, Activation.RELU,bestdropout)
                    .addHiddenLayer(256, Activation.RELU,bestdropout)
                    .addHiddenLayer(128, Activation.RELU,bestdropout)
//                    .addHiddenLayer(512, Activation.SWISH)
//                    .addHiddenLayer(256, Activation.SWISH)
//                    .addHiddenLayer(128, Activation.SWISH)
                    .setOutputActivation(Activation.SIGMOID)
                    .setLossFunction(lossFunction)
//                    .setLossFunction(new FocalLoss(2.0, 0.25)) // gamma = 2, alpha = 0.25
                    .setOptimizer(optimizer)
                    .setGradientClipping(bestGradientClipping)
                    .setMaxNorm(bestMaxNorm)
                    .setLearningRate(bestLR);

            this.network = builder.build();
            this.trainer = new NetworkTrainer(this.network); // Asegúrate de que el trainer se actualice si se reconstruye la red
            System.out.println("Se creó una nueva red neuronal.");
        } else {
            NetworkSaverLoader.LoadResult loadResult = this.loadLoadResult();
            this.trainer = new NetworkTrainer(this.network);
            if(loadResult!=null){
                this.trainingTime = loadResult.getTrainingTime();
                this.trainer.setLossHistory(loadResult.getLossHistory());
                this.trainer.setEpochs(loadResult.getEpochs());
                this.trainer.setBatchSize(loadResult.getBatchSize());
            }
            System.out.println("Se cargó una red neuronal pre-entrenada.");
        }
    }

    private boolean loadNetworkFacade(int inputSize, int outputSize) {
        this.network = NetworkSaverLoader.loadNetwork(inputSize, outputSize);
        if (this.network == null) {
            return false; // La carga falló
        } else {
            return true;  // La carga fue exitosa
        }
    }

    private NetworkSaverLoader.LoadResult loadLoadResult(){
        return NetworkSaverLoader.loadMetadata();
    }

    public void saveNetwork(boolean SaveMetadata) {
        if(SaveMetadata){
            NetworkSaverLoader.saveNetwork(network,trainingTime,this.getLossHistory(),this.getEpochs(),this.getBatchSize());
        } else {
            NetworkSaverLoader.saveNetwork(network);
        }
    }

    public void trainNetwork(float[][] trainingData, float[][] trainingLabels, int epochs, int batchSize) {
        long startTime = System.currentTimeMillis();
        Matrix trainDataMatrix = MatrixFactory.createMatrix(trainingData);// GPUMatrixBackend(trainingData);//.transpose();
        Matrix trainLabelsMatrix = MatrixFactory.createMatrix(trainingLabels);//new GPUMatrixBackend(trainingLabels);//.transpose();
//        if(trainDataMatrix.hasNaN()) System.out.println("Hay NaNs en trainDataMatrix");
//        if(trainLabelsMatrix.hasNaN()) System.out.println("Hay NaNs en trainLabelsMatrix");
//        trainDataMatrix.printShape();
//        trainLabelsMatrix.printShape();
        trainer.train(trainDataMatrix, trainLabelsMatrix, epochs, batchSize);
        long endTime = System.currentTimeMillis();
        trainingTime = endTime - startTime;
    }

    public float[][] predict(float[][] inputData) {
        Matrix inputMatrix = MatrixFactory.createMatrix(inputData);//new GPUMatrixBackend(inputData).transpose();
        Matrix outputMatrix = trainer.predict(inputMatrix);
        //System.out.println("outputMatrix: ");
        //outputMatrix.printShape();
        return (outputMatrix.transpose()).toFloatMatrix();
    }
    public float[][] predictone(float[][] inputData) {
        Matrix inputMatrix = MatrixFactory.createMatrix(inputData);//new GPUMatrixBackend(inputData).transpose();
        Matrix outputMatrix = trainer.predictone(inputMatrix);
        //System.out.println("outputMatrix: ");
        //outputMatrix.printShape();
        return (outputMatrix.transpose()).toFloatMatrix();
    }

    public double evaluateNetwork(float[][] testData, float[][] testLabels) {
        Matrix testDataMatrix = MatrixFactory.createMatrix(testData).transpose();//new GPUMatrixBackend(testData).transpose();
        Matrix testLabelsMatrix = MatrixFactory.createMatrix(testLabels);//new GPUMatrixBackend(testLabels);//.transpose();
        //System.out.println("teatLabelsMatrix: ");
        //testLabelsMatrix.printShape();
        return trainer.evaluate(testDataMatrix, testLabelsMatrix);
    }

    public Map<String, Object> getHyperparameters(){
        hyperparameters.put("learningRate",this.getLearningRate());
        hyperparameters.put("epochs", this.getEpochs());
        hyperparameters.put("batchSize", this.getBatchSize());
        return hyperparameters;
    }

    public ThresholdAdjuster fitThresholds(float[][] testData, float[][] testLabels){
        return trainer.fitThresholds(MatrixFactory.createMatrix(testData),MatrixFactory.createMatrix(testLabels));
    }
    private double getLearningRate(){
        return this.network.getLearningRate();
    }

    public List<Double> getLossHistory(){
        return this.trainer.getLossHistory();
    }

    private int getEpochs(){
        return this.trainer.getEpochs();
    }
    private int getBatchSize() {
        return this.trainer.getBatchSize();
    }

    public long getTrainingTime() {
        return trainingTime;
    }
}
