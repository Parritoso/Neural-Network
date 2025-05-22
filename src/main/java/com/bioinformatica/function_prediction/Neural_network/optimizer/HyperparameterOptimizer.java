package com.bioinformatica.function_prediction.Neural_network.optimizer;

import com.bioinformatica.function_prediction.Neural_network.layer.Activation;
import com.bioinformatica.function_prediction.Neural_network.loss.BinaryCrossEntropy;
import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;
import com.bioinformatica.function_prediction.Neural_network.matrix.MatrixFactory;
import com.bioinformatica.function_prediction.Neural_network.model.NetworkBuilder;
import com.bioinformatica.function_prediction.Neural_network.model.NeuralNetwork;
import com.bioinformatica.function_prediction.Neural_network.trainer.NetworkTrainer;
import com.bioinformatica.load.LoaderManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class HyperparameterOptimizer {
    private final Matrix trainData;
    private final Matrix trainLabels;
    private final Matrix valData;
    private final Matrix valLabels;
    private final int numTrials;
    private final int numThreads;
    private final Random random = new Random();
    private final Object lock = new Object();

    // Mejores hiperparámetros encontrados
    private double bestLearningRate;
    private int bestBatchSize;
    private int bestEpochs;
    private double bestDropoutRate;
    private double bestGradientClipThreshold;
    private double bestMaxNorm;
    private double bestLoss = Double.MAX_VALUE;
    AtomicReference<Double> bestLossRef = new AtomicReference<>(Double.MAX_VALUE);

    // Límites de los hiperparámetros (ajustables)
    private static final double LEARNING_RATE_MIN = 1e-4;
    private static final double LEARNING_RATE_MAX = 1e-2;
    private static final int BATCH_SIZE_MIN = 16;
    private static final int BATCH_SIZE_MAX = 256;
    private static final int EPOCHS_MIN = 5;
    private static final int EPOCHS_MAX = 15;
    private static final double DROPOUT_RATE_MIN = 0.0;
    private static final double DROPOUT_RATE_MAX = 0.5;
    private static final double GRADIENT_CLIP_THRESHOLD_MIN = 0.5;
    private static final double GRADIENT_CLIP_THRESHOLD_MAX = 1.0;
    private static final double MAX_NORM_MIN = 1.0;
    private static final double MAX_NORM_MAX = 5.0;

    // Historial de evaluaciones para TPE
    private final List<Trial> history = new ArrayList<>();
    private static final int N_STARTUP_TRIALS = 10; // Mínimo de trials antes de usar TPE
    private static final double GAMMA = 0.25; // Factor para determinar buenos vs. malos trials en TPE
    private double subsetRatio = 0.1;

//    public HyperparameterOptimizer(Matrix trainData, Matrix trainLabels, Matrix valData, Matrix valLabels, int numTrials) {
//        this.trainData = trainData;
//        this.trainLabels = trainLabels;
//        this.valData = valData;
//        this.valLabels = valLabels;
//        this.numTrials = numTrials;
//    }
//
//    public void search() {
//
//        // Verificar si trainData tiene NaN
//        if (containsNaN(trainData)) {
//            System.out.println("trainData contiene NaN.");
//        } else {
//            System.out.println("trainData está limpio.");
//        }
//
//        // Verificar si trainLabels tiene NaN
//        if (containsNaN(trainLabels)) {
//            System.out.println("trainLabels contiene NaN.");
//        } else {
//            System.out.println("trainLabels está limpio.");
//        }
//
//        int availableCores = Runtime.getRuntime().availableProcessors();
//        int numThreads = Math.max(1, availableCores / 2);
//
//        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
//        List<Future<?>> futures = new ArrayList<>();
//
//        Set<String> triedCombinations = Collections.newSetFromMap(new ConcurrentHashMap<>());
//
//        System.out.println(" Iniciando búsqueda con " + numThreads + " hilos paralelos...");
//
////        // Subset Sampling: 20% del dataset
////        int subsetSize = (int) (trainData.rows() * 0.2);
////        Matrix smallTrainData = trainData.slice(0, subsetSize);
////        Matrix  smallTrainLabels = trainLabels.slice(0, subsetSize);
//
////        // Red base reutilizable (sin optimizador aún)
////        NeuralNetwork baseNetwork = new NetworkBuilder()
////                .setInputSize(trainData.cols())
////                .setOutputSize(trainLabels.cols())
////                .addHiddenLayer(128, Activation.RELU)
////                .addHiddenLayer(64, Activation.RELU)
////                .setOutputActivation(Activation.SIGMOID)
////                .setLossFunction(new BinaryCrossEntropy())
////                .setOptimizer(new Adam(0.001)) // <-- placeholder
////                .setLearningRate(0.001)
////                .build();
//
//        for (int i = 0; i < numTrials; i++) {
//            futures.add(executor.submit(() -> {
//                LoaderManager loaderManager = new LoaderManager();
//                Matrix trainData = this.trainData;
//                Matrix trainLabels = this.trainLabels;
//                Matrix valData = this.valData;
//                Matrix valLabels = this.valLabels;
////                try {
////                    loaderManager.obtenerDatos("train_dataset.csv");
////                } catch (IOException e) {
////                    throw new RuntimeException(e);
////                }
////                if(containsNaN(trainData)){
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"trainData contiene NaN.");
////                    float[][] trainingData = loaderManager.getTrainingData();
////                    trainData = MatrixFactory.createMatrix(trainingData);
////                } else {
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"trainData está limpio.");
////                }
////                if(containsNaN(trainLabels)){
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"trainLabels contiene NaN.");
////                    float[][] trainingLabels = loaderManager.getTrainingLabels();
////                    trainLabels = MatrixFactory.createMatrix(trainingLabels);
////                } else {
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"trainLabels está limpio.");
////                }
//
////                try {
////                    loaderManager.obtenerDatos("test_dataset.csv");
////                } catch (IOException e) {
////                    throw new RuntimeException(e);
////                }
////                if(containsNaN(valData)){
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"valData contiene NaN.");
////                    float[][] trainingData = loaderManager.getTrainingData();
////                    valData = MatrixFactory.createMatrix(trainingData);
////                } else {
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"valData está limpio.");
////                }
////                if(containsNaN(valLabels)){
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"valLabels contiene NaN.");
////                    float[][] trainingLabels = loaderManager.getTrainingLabels();
////                   valLabels = MatrixFactory.createMatrix(trainingLabels);
////                } else {
////                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "]"+"valLabels está limpio.");
////                }
//
//
//                Random random = new Random();
//                double learningRate; // 1e-2 a 1e-4
//                int batchSize; // 16, 32, ..., 256
//                int epochs; // 5 a 15
//                String key;
//
//                // Evitar duplicados
//                do {
//                    learningRate = Math.pow(10, -random.nextInt(3) - 2); // 1e-2 a 1e-4
//                    batchSize = (int) Math.pow(2, random.nextInt(5) + 4); // 16, 32, ..., 256
//                    epochs = random.nextInt(11) + 5; // 5 a 15
//                    key = learningRate + "-" + batchSize + "-" + epochs;
//                } while (!triedCombinations.add(key)); // solo sigue si es nuevo
//
//                System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "] Probando -> LR: " + learningRate + ", BS: " + batchSize + ", Epochs: " + epochs);
//
//                NeuralNetwork network = new NetworkBuilder()
//                        .setInputSize(trainData.cols())
//                        .setOutputSize(trainLabels.cols())
//                        .addHiddenLayer(128, Activation.RELU)
//                        .addHiddenLayer(64, Activation.RELU)
//                        .setOutputActivation(Activation.SIGMOID)
//                        .setLossFunction(new BinaryCrossEntropy())
//                        .setOptimizer(new Adam(learningRate))
//                        .setLearningRate(learningRate)
//                        .build();
//
////                // Clonar red base
////                NeuralNetwork network = baseNetwork.clone();
////                network.setOptimizer(new Adam(learningRate));
////                network.setLearningRate(learningRate);
//
//                NetworkTrainer trainer = new NetworkTrainer(network);
//                System.out.println("[Thread ID: " + Thread.currentThread().getId() + "]" + "[" + Thread.currentThread().getName() + "] epochs: "+epochs+", BS: "+batchSize);
//                trainer.train(trainData.copy(), trainLabels.copy(), epochs, batchSize);
//                double loss = trainer.evaluate(valData.copy(), valLabels.copy());
//
//                if (Double.isNaN(loss)) {
//                    System.out.println("[Thread ID: " + Thread.currentThread().getId() + "] Evaluación produjo NaN -> LR: " + learningRate + ", BS: " + batchSize + ", Epochs: " + epochs);
//                }
//
//                System.out.println("[Thread ID: " + Thread.currentThread().getId() + "] Evaluando -> LR: " + learningRate +
//                        ", BS: " + batchSize + ", Epochs: " + epochs + ", Loss: " + loss);
//
//                synchronized (lock) {
//                    double currentBestLoss = bestLossRef.get();
//                    System.out.println("Comprobando condición -> loss: " + loss + ", bestLoss: " + currentBestLoss);
//                    if (!Double.isNaN(loss) && loss < currentBestLoss) {
//                        bestLossRef.set(loss); // Actualiza correctamente la referencia de bestLoss
//                        bestLoss = loss;
//                        bestLearningRate = learningRate;
//                        bestBatchSize = batchSize;
//                        bestEpochs = epochs;
//                        System.out.println(" Mejor hasta ahora -> Loss: " + loss + " | LR: " + learningRate + ", BS: " + batchSize + ", Epochs: " + epochs);
//                    }
//                }
//            }));
//        }
//
//        // Esperar a que terminen todos los hilos
//        for (Future<?> f : futures) {
//            try {
//                f.get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//
//        executor.shutdown();
//
//        saveBestHyperparameters(Paths.get("resources","best_hyperparameters.txt").toString());
//
//        System.out.println("\n Mejor configuración encontrada:");
//        System.out.println("   - Learning Rate: " + bestLearningRate);
//        System.out.println("   - Batch Size: " + bestBatchSize);
//        System.out.println("   - Epochs: " + bestEpochs);
//        System.out.println("   - Validation Loss: " + bestLoss);
//    }
//
//    public double getBestLearningRate() {
//        return bestLearningRate;
//    }
//
//    public int getBestBatchSize() {
//        return bestBatchSize;
//    }
//
//    public int getBestEpochs() {
//        return bestEpochs;
//    }
//
//    public double getBestLoss() {
//        return bestLoss;
//    }
//
//    private void saveBestHyperparameters(String filename) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
//            writer.write("learning_rate=" + bestLearningRate);
//            writer.newLine();
//            writer.write("batch_size=" + bestBatchSize);
//            writer.newLine();
//            writer.write("epochs=" + bestEpochs);
//            writer.newLine();
//            writer.write("validation_loss=" + bestLoss);
//            writer.newLine();
//            System.out.println(" Configuración óptima guardada en: " + filename);
//        } catch (IOException e) {
//            System.err.println(" Error al guardar los hiperparámetros: " + e.getMessage());
//        }
//    }

    public HyperparameterOptimizer(Matrix trainData, Matrix trainLabels, Matrix valData, Matrix valLabels, int numTrials) {
        this.trainData = trainData;
        this.trainLabels = trainLabels;
        this.valData = valData;
        this.valLabels = valLabels;
        this.numTrials = numTrials;
        this.numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2); // Usa la mitad de los cores disponibles
    }
    public void setSubsetRatio(double subsetRatio) {
        this.subsetRatio = subsetRatio;
    }

    /**
     * Realiza la búsqueda de hiperparámetros utilizando el algoritmo TPE.
     */
    public void search() {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        System.out.println("Iniciando búsqueda de hiperparámetros con TPE y " + numThreads + " hilos.");

        for (int i = 0; i < numTrials; i++) {
            final int trialId = i;
            futures.add(executor.submit(() -> {
                try {
                    Trial trial = optimizeTrial(trialId);
                    // Crear subconjuntos de datos para este hilo
                    Matrix threadTrainData;
                    Matrix threadTrainLabels;

                    // Calcular el tamaño del subconjunto para este hilo
                    int subsetSize = (int) (trainData.rows() * subsetRatio);
                    if (subsetSize > 0) {
                        // Muestrear un subconjunto aleatorio de los datos de entrenamiento
                        threadTrainData = trainData.slice(0, subsetSize);
                        threadTrainLabels = trainLabels.slice(0, subsetSize);
                    } else {
                        threadTrainData = trainData;
                        threadTrainLabels = trainLabels;
                    }
                    double loss = runTrial(threadTrainData, threadTrainLabels, trial.learningRate, trial.batchSize, trial.epochs, trial.dropoutRate, trial.gradientClipThreshold, trial.maxNorm);
                    trial.loss = loss;
                    synchronized (lock) {
                        history.add(trial);
                        updateBestHyperparameters(trial.learningRate, trial.batchSize, trial.epochs, trial.dropoutRate, trial.gradientClipThreshold, trial.maxNorm, loss);
                    }
                    System.out.println("[Thread " + Thread.currentThread().getId() + "] Trial " + trialId + " - Loss: " + loss +
                            " (LR: " + trial.learningRate + ", BS: " + trial.batchSize + ", Epochs: " + trial.epochs +
                            ", DR: " + trial.dropoutRate + ", Clip: " + trial.gradientClipThreshold + ", Norm=" + trial.maxNorm + ")");
                } catch (Exception e) {
                    System.err.println("[Thread " + Thread.currentThread().getId() + "] Error en el trial " + trialId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }));
        }

        // Esperar a que terminen todos los hilos
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        saveBestHyperparameters(Paths.get("resources","best_hyperparameters.txt").toString()); //Guarda los hiperparametros

        System.out.println("\nMejor configuración encontrada:");
        System.out.println("  Learning Rate: " + bestLearningRate);
        System.out.println("  Batch Size: " + bestBatchSize);
        System.out.println("  Epochs: " + bestEpochs);
        System.out.println("  Dropout Rate: " + bestDropoutRate);
        System.out.println("  Gradient Clip Threshold: " + bestGradientClipThreshold);
        System.out.println("  Max Norm: " + bestMaxNorm);
        System.out.println("  Validation Loss: " + bestLoss);
    }

    /**
     * Determina los hiperparámetros para un trial usando TPE.
     */
    private Trial optimizeTrial(int trialId) {
        if (trialId < N_STARTUP_TRIALS) {
            // Búsqueda aleatoria para los primeros trials
            return new Trial(
                    random.nextDouble() * (LEARNING_RATE_MAX - LEARNING_RATE_MIN) + LEARNING_RATE_MIN,
                    (int) Math.pow(2, random.nextInt((int) (Math.log(BATCH_SIZE_MAX) / Math.log(2)) - (int) (Math.log(BATCH_SIZE_MIN) / Math.log(2)) + 1) + (int) (Math.log(BATCH_SIZE_MIN) / Math.log(2))),
                    random.nextInt(EPOCHS_MAX - EPOCHS_MIN + 1) + EPOCHS_MIN,
                    random.nextDouble() * (DROPOUT_RATE_MAX - DROPOUT_RATE_MIN) + DROPOUT_RATE_MIN,
                    random.nextDouble() * (GRADIENT_CLIP_THRESHOLD_MAX - GRADIENT_CLIP_THRESHOLD_MIN) + GRADIENT_CLIP_THRESHOLD_MIN,
                    random.nextDouble() * (MAX_NORM_MAX - MAX_NORM_MIN) + MAX_NORM_MIN,
                    0
            );
        } else {
            // Aplicar TPE
            int nGood = (int) (history.size() * GAMMA);
            history.sort(Comparator.comparingDouble(t -> t.loss)); // Ordena por pérdida (de menor a mayor)

            List<Trial> goodTrials = history.subList(0, Math.min(nGood, history.size()));
            List<Trial> badTrials = history.subList(Math.min(nGood, history.size()), history.size());

            // Funciones lambda para muestrear de buenos y malos
            Function<List<Trial>, Double> sampleLearningRate = trials -> {
                if (trials.isEmpty())
                    return random.nextDouble() * (LEARNING_RATE_MAX - LEARNING_RATE_MIN) + LEARNING_RATE_MIN;
                double[] values = trials.stream().mapToDouble(t -> t.learningRate).toArray();
                return sampleKernelDensityEstimation(values, LEARNING_RATE_MIN, LEARNING_RATE_MAX);
            };

            Function<List<Trial>, Integer> sampleBatchSize = trials -> {
                if (trials.isEmpty())
                    return (int) Math.pow(2, random.nextInt((int) (Math.log(BATCH_SIZE_MAX) / Math.log(2)) - (int) (Math.log(BATCH_SIZE_MIN) / Math.log(2)) + 1) + (int) (Math.log(BATCH_SIZE_MIN) / Math.log(2)));
                double[] values = trials.stream().mapToDouble(t -> Math.log(t.batchSize) / Math.log(2)).toArray();
                double sampledValue = sampleKernelDensityEstimation(values, Math.log(BATCH_SIZE_MIN) / Math.log(2), Math.log(BATCH_SIZE_MAX) / Math.log(2));
                return (int) Math.pow(2, (int) Math.round(sampledValue));
            };

            Function<List<Trial>, Integer> sampleEpochs = trials -> {
                if (trials.isEmpty())
                    return random.nextInt(EPOCHS_MAX - EPOCHS_MIN + 1) + EPOCHS_MIN;
                double[] values = trials.stream().mapToDouble(t -> t.epochs).toArray();
                return (int) Math.round(sampleKernelDensityEstimation(values, EPOCHS_MIN, EPOCHS_MAX));
            };

            Function<List<Trial>, Double> sampleDropoutRate = trials -> {
                if (trials.isEmpty())
                    return random.nextDouble() * (DROPOUT_RATE_MAX - DROPOUT_RATE_MIN) + DROPOUT_RATE_MIN;
                double[] values = trials.stream().mapToDouble(t -> t.dropoutRate).toArray();
                return sampleKernelDensityEstimation(values, DROPOUT_RATE_MIN, DROPOUT_RATE_MAX);
            };

            Function<List<Trial>, Double> sampleGradientClipThreshold = trials -> {
                if (trials.isEmpty())
                    return random.nextDouble() * (GRADIENT_CLIP_THRESHOLD_MAX - GRADIENT_CLIP_THRESHOLD_MIN) + GRADIENT_CLIP_THRESHOLD_MIN;
                double[] values = trials.stream().mapToDouble(t -> t.gradientClipThreshold).toArray();
                return sampleKernelDensityEstimation(values, GRADIENT_CLIP_THRESHOLD_MIN, GRADIENT_CLIP_THRESHOLD_MAX);
            };
            Function<List<Trial>, Double> sampleMaxNorm = trials -> {
                if (trials.isEmpty())
                    return random.nextDouble() * (MAX_NORM_MAX - MAX_NORM_MIN) + MAX_NORM_MIN;
                double[] values = trials.stream().mapToDouble(t -> t.maxNorm).toArray();
                return sampleKernelDensityEstimation(values, MAX_NORM_MIN, MAX_NORM_MAX);
            };

            double l_good = sampleLearningRate.apply(goodTrials);
            int bs_good = sampleBatchSize.apply(goodTrials);
            int e_good = sampleEpochs.apply(goodTrials);
            double dr_good = sampleDropoutRate.apply(goodTrials);
            double gct_good = sampleGradientClipThreshold.apply(goodTrials);
            double mn_good = sampleMaxNorm.apply(goodTrials);

            double l_bad = sampleLearningRate.apply(badTrials);
            int bs_bad = sampleBatchSize.apply(badTrials);
            int e_bad = sampleEpochs.apply(badTrials);
            double dr_bad = sampleDropoutRate.apply(badTrials);
            double gct_bad = sampleGradientClipThreshold.apply(badTrials);
            double mn_bad = sampleMaxNorm.apply(badTrials);

            // Seleccionar el mejor valor entre good y bad para cada hiperparámetro
            double learningRate = (getProbability(l_good, goodTrials, LEARNING_RATE_MIN, LEARNING_RATE_MAX) / getProbability(l_bad, badTrials, LEARNING_RATE_MIN, LEARNING_RATE_MAX) > 1.0) ? l_good : l_bad;
            int batchSize = (getProbability(bs_good, goodTrials, Math.log(BATCH_SIZE_MIN) / Math.log(2), Math.log(BATCH_SIZE_MAX) / Math.log(2)) / getProbability(bs_bad, badTrials, Math.log(BATCH_SIZE_MIN) / Math.log(2), Math.log(BATCH_SIZE_MAX) / Math.log(2)) > 1.0) ? bs_good : bs_bad;
            int epochs = (getProbability(e_good, goodTrials, EPOCHS_MIN, EPOCHS_MAX) / getProbability(e_bad, badTrials, EPOCHS_MIN, EPOCHS_MAX) > 1.0) ? e_good : e_bad;
            double dropoutRate = (getProbability(dr_good, goodTrials, DROPOUT_RATE_MIN, DROPOUT_RATE_MAX) / getProbability(dr_bad, badTrials, DROPOUT_RATE_MIN, DROPOUT_RATE_MAX) > 1.0) ? dr_good : dr_bad;
            double gradientClipThreshold = (getProbability(gct_good, goodTrials, GRADIENT_CLIP_THRESHOLD_MIN, GRADIENT_CLIP_THRESHOLD_MAX) / getProbability(gct_bad, badTrials, GRADIENT_CLIP_THRESHOLD_MIN, GRADIENT_CLIP_THRESHOLD_MAX) > 1.0) ? gct_good : gct_bad;
            double maxNorm = (getProbability(mn_good, goodTrials, MAX_NORM_MIN, MAX_NORM_MAX) / getProbability(mn_bad, badTrials, MAX_NORM_MIN, MAX_NORM_MAX) > 1.0) ? mn_good : mn_bad;
            return new Trial(learningRate, (int) Math.pow(2, (int) Math.round(batchSize)), epochs, dropoutRate, gradientClipThreshold, maxNorm, 0);
        }
    }

    /**
     * Calcula la probabilidad de un valor dado, usando KDE.
     */
    private double getProbability(double value, List<Trial> trials, double minVal, double maxVal) {
        if (trials.isEmpty()) return 1.0 / (maxVal - minVal); // Uniforme si no hay trials

        double bandwidth = calculateBandwidth(trials.size());
        double probability = 0;
        for (Trial trial : trials) {
            double diff = value - (trial.learningRate);
            if (value == trial.batchSize)
                diff = value - trial.batchSize;
            if (value == trial.epochs)
                diff = value - trial.epochs;
            if (value == trial.dropoutRate)
                diff = value - trial.dropoutRate;
            if (value == trial.gradientClipThreshold)
                diff = value - trial.gradientClipThreshold;
            if (value == trial.maxNorm)
                diff = value - trial.maxNorm;

            probability += gaussianKernel(diff / bandwidth) / bandwidth;
        }
        return probability / trials.size();
    }

    /**
     * Calcula el ancho de banda para KDE usando la regla de Silverman.
     */
    private double calculateBandwidth(int n) {
        return 1.06 * Math.pow(n, -0.2);
    }

    /**
     * Función kernel Gaussiana.
     */
    private double gaussianKernel(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI);
    }

    /**
     * Muestrea un valor usando KDE.
     */
    private double sampleKernelDensityEstimation(double[] values, double minVal, double maxVal) {
        if (values.length == 0) return random.nextDouble() * (maxVal - minVal) + minVal;

        double bandwidth = calculateBandwidth(values.length);
        double sample = 0;
        double weightSum = 0;

        for (int i = 0; i < 100; i++) { // Muestrea 100 veces y promedia
            int index = random.nextInt(values.length);
            double noise = random.nextGaussian() * bandwidth;
            double value = values[index] + noise;
            if (value >= minVal && value <= maxVal) {
                sample += value;
                weightSum++;
            }
        }
        return weightSum > 0 ? sample / weightSum : random.nextDouble() * (maxVal - minVal) + minVal;
    }

    /**
     * Ejecuta un trial con los hiperparámetros dados.
     * @return La pérdida de validación.
     */
    private double runTrial(Matrix trainData, Matrix trainLabels,double learningRate, int batchSize, int epochs, double dropoutRate, double gradientClipThreshold, double maxNorm) {
        NeuralNetwork network = new NetworkBuilder()
                .setInputSize(trainData.cols())
                .setOutputSize(trainLabels.cols())
                .addHiddenLayer(128, Activation.RELU, dropoutRate) // Usar dropoutRate
                .addHiddenLayer(64, Activation.RELU, dropoutRate) // Usar dropoutRate
                .setOutputActivation(Activation.SIGMOID)
                .setLossFunction(new BinaryCrossEntropy())
                .setOptimizer(new Adam(learningRate))
                .setLearningRate(learningRate)
                .setGradientClipping(gradientClipThreshold) // Usar gradientClipThreshold
                .setMaxNorm(maxNorm) // Usar maxNorm
                .build();

        NetworkTrainer trainer = new NetworkTrainer(network);
        trainer.train(trainData.copy(), trainLabels.copy(), epochs, batchSize);
        return trainer.evaluate(valData.copy(), valLabels.copy());
    }

    /**
     * Actualiza los mejores hiperparámetros si la pérdida actual es menor que la mejor pérdida encontrada hasta ahora.
     */
    private void updateBestHyperparameters(double learningRate, int batchSize, int epochs, double dropoutRate, double gradientClipThreshold, double maxNorm, double loss) {
        synchronized (lock) {
            double currentBestLoss = bestLossRef.get();
            if (!Double.isNaN(loss) && loss < currentBestLoss) {
                bestLossRef.set(loss);
                bestLoss = loss;
                bestLearningRate = learningRate;
                bestBatchSize = batchSize;
                bestEpochs = epochs;
                bestDropoutRate = dropoutRate;
                bestGradientClipThreshold = gradientClipThreshold;
                bestMaxNorm = maxNorm;
                System.out.println("Nuevo mejor loss: " + loss + " en LR=" + learningRate + ", BS=" + batchSize + ", Epochs=" + epochs + ", DR=" + dropoutRate + ", Clip=" + gradientClipThreshold + ", Norm=" + maxNorm);
            }
        }
    }

    // Getters para los mejores hiperparámetros
    public double getBestLearningRate() { return bestLearningRate; }
    public int getBestBatchSize() { return bestBatchSize; }
    public int getBestEpochs() { return bestEpochs; }
    public double getBestDropoutRate() { return bestDropoutRate; }
    public double getBestGradientClipThreshold() { return bestGradientClipThreshold; }
    public double getBestMaxNorm() { return bestMaxNorm; }
    public double getBestLoss() { return bestLoss; }

    /**
     * Guarda los mejores hiperparámetros en un archivo.
     */
    private void saveBestHyperparameters(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("learning_rate=" + bestLearningRate);
            writer.newLine();
            writer.write("batch_size=" + bestBatchSize);
            writer.newLine();
            writer.write("epochs=" + bestEpochs);
            writer.newLine();
            writer.write("dropout_rate=" + bestDropoutRate);
            writer.newLine();
            writer.write("gradient_clip_threshold=" + bestGradientClipThreshold);
            writer.newLine();
            writer.write("max_norm=" + bestMaxNorm);
            writer.newLine();
            writer.write("validation_loss=" + bestLoss);
            writer.newLine();
            System.out.println("Configuración óptima guardada en: " + filename);
        } catch (IOException e) {
            System.err.println("Error al guardar los hiperparámetros: " + e.getMessage());
        }
    }

    // Función que verifica si hay valores NaN en una matriz
    public boolean containsNaN(Matrix matrix) {
        float[][] aux = matrix.toFloatMatrix();
        for (int i = 0; i < matrix.rows(); i++) {
            for (int j = 0; j < matrix.cols(); j++) {
                if (Double.isNaN(aux[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clase interna para representar un trial.
     */
    private static class Trial {
        double learningRate;
        int batchSize;
        int epochs;
        double dropoutRate;
        double gradientClipThreshold;
        double maxNorm;
        double loss; // Pérdida asociada al trial

        public Trial(double learningRate, int batchSize, int epochs, double dropoutRate, double gradientClipThreshold, double maxNorm, double loss) {
            this.learningRate = learningRate;
            this.batchSize = batchSize;
            this.epochs = epochs;
            this.dropoutRate = dropoutRate;
            this.gradientClipThreshold = gradientClipThreshold;
            this.maxNorm = maxNorm;
            this.loss = loss;
        }
    }
}
