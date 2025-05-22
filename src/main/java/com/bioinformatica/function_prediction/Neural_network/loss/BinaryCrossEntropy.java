package com.bioinformatica.function_prediction.Neural_network.loss;

import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;

import java.util.Arrays;

public class BinaryCrossEntropy implements LossFunction {
    private final double epsilon = 1e-15; // Para evitar divisiones por cero

    @Override
    public double calculate(Matrix predicted, Matrix target) {
        if (predicted.hasNaN()) {
            System.out.println("Predicted tiene NaNs antes de calculate");
        }
        if (target.hasNaN()) {
            System.out.println("Predicted tiene NaNs antes de calculate");
        }
        int n = target.shape()[0];
        Matrix clippedPredicted = predicted.copy().applyFunction(p -> Math.max(epsilon, Math.min(1 - epsilon, p)));
//        System.out.println("Shape of target before first multiply: " + Arrays.toString(target.shape()));
//        target.printShape();
//        System.out.println("Shape of clippedPredicted.log() before first multiply: " + Arrays.toString(clippedPredicted.log().shape()));
        Matrix term1 = target.copy().multiply(-1.0).multiply(clippedPredicted.log());
//        System.out.println("Shape of target before second multiply: " + Arrays.toString(target.copy().rsub(1.0).shape()));
//        System.out.println("Shape of clippedPredicted.copy().rsub(1.0).log() before second multiply: " + Arrays.toString(clippedPredicted.copy().rsub(1.0).log().shape()));
        Matrix term2 = target.copy().rsub(1.0).multiply(clippedPredicted.copy().rsub(1.0).add(epsilon).log());
//        System.out.println("clippedPredicted.copy().rsub(1.0) min: "+clippedPredicted.copy().rsub(1.0).min());
//        System.out.println("clippedPredicted.copy().rsub(1.0) max: "+clippedPredicted.copy().rsub(1.0).max());
//        System.out.println("clippedPredicted.copy().rsub(1.0).add(epsilon) max: "+clippedPredicted.copy().rsub(1.0).add(epsilon).min());
//        System.out.println("clippedPredicted.copy().rsub(1.0).add(epsilon) max: "+clippedPredicted.copy().rsub(1.0).add(epsilon).max());
//        System.out.println("clippedPredicted.copy().rsub(1.0).add(epsilon).log() min: "+clippedPredicted.copy().rsub(1.0).add(epsilon).log().min());
//        System.out.println("clippedPredicted.copy().rsub(1.0).add(epsilon).log() max: "+clippedPredicted.copy().rsub(1.0).add(epsilon).log().max());
//        System.out.println("target.copy().rsub(1.0) max: "+target.copy().rsub(1.0).min());
//        System.out.println("target.copy().rsub(1.0) max: "+target.copy().rsub(1.0).max());
//        System.out.println("term2 max: "+term2.min());
//        System.out.println("term2 max: "+term2.max());
//        if(clippedPredicted.copy().rsub(1.0).hasNaN()) System.out.println("clippedPredicted.copy().rsub(1.0) devuelve NaN");
//        if(clippedPredicted.copy().rsub(1.0).log().hasNaN()) System.out.println("clippedPredicted.copy().rsub(1.0).log() devuelve NaN");
//        if(target.copy().rsub(1.0).hasNaN()) System.out.println("target.copy().rsub(1.0) devuelve NaN");
//        if(term2.hasNaN()) System.out.println("terms2 tiene NaNs");
//        if(term1.hasNaN()) System.out.println("terms1 tiene NaNs");
//        if(Double.isNaN((term1.add(term2).mean()) / n)) System.out.println("(term1.add(term2).mean()) / n devuelve NaN");
        return (term1.add(term2).mean()) / n;
    }

    @Override
    public Matrix derivative(Matrix predicted, Matrix target) {
        if (predicted.hasNaN()) {
            System.out.println("Predicted tiene NaNs antes de derivative");
        }
//        Matrix clippedPredicted = predicted.copy().applyFunction(p -> Math.max(epsilon, Math.min(1 - epsilon, p)));
//        Matrix numerator = clippedPredicted.copy().rsub(1.0).multiply(-1.0).divide(clippedPredicted).subtract(target.copy().rsub(1.0).multiply(-1.0).divide(clippedPredicted.copy().rsub(1.0)));
//        return numerator.divide(target.shape()[0]);
//        Matrix clipped = predicted.copy().applyFunction(p -> Math.max(epsilon, Math.min(1 - epsilon, p)));
//
//        if (clipped.hasNaN()) System.out.println("clipped tiene NaNs");
//
//        Matrix grad = clipped.copy().subtract(target)
//                .divide(clipped.copy().multiply(clipped.copy().rsub(1.0))); // (p - y) / (p * (1 - p))
//
//        if (grad.hasNaN()){
//            System.out.println("grad tiene NaNs");
//            grad = grad.applyFunction(g -> Double.isFinite(g) ? g : 0.0);
//        }
//
//        return grad.divide(target.shape()[0]);

        // Clip predicted para evitar 0 y 1 extremos
        Matrix clipped = predicted.copy().applyFunction(p -> Math.max(epsilon, Math.min(1 - epsilon, p)));

        // (pred - target) / (pred * (1 - pred))
        Matrix numerator = clipped.copy().subtract(target);

        Matrix denominator = clipped.copy().multiply(clipped.copy().rsub(1.0));

        // Prevenir divisiones por 0 o valores extremos
        denominator = denominator.applyFunction(d -> Math.abs(d) < epsilon ? epsilon : d);

        Matrix grad = numerator.divide(denominator);

        if (grad.hasNaN()) {
            System.out.println("grad tiene NaNs ANTES de return");
        }

        return grad.divide(target.shape()[0]);
    }
}
