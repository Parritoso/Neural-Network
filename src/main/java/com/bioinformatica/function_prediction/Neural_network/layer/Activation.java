package com.bioinformatica.function_prediction.Neural_network.layer;

import com.bioinformatica.function_prediction.Neural_network.matrix.Matrix;

public enum Activation {
    SIGMOID {
        @Override
        public Matrix apply(Matrix z) {
            return z.sigmoid();
        }

        @Override
        public Matrix derivative(Matrix a) {
            return a.sigmoidDerivative();
        }
    },
    RELU {
        @Override
        public Matrix apply(Matrix z) {
            return z.relu();
        }

        @Override
        public Matrix derivative(Matrix a) {
            return a.reluDerivative();
        }
    },
    SWISH {
        @Override
        public Matrix apply(Matrix z) {
            return z.copy().multiply(z.copy().sigmoid()); // z * sigmoid(z)
        }

        @Override
        public Matrix derivative(Matrix a) {
            // Aproximación: swish' = sigmoid(z) + swish(z) * (1 - sigmoid(z))
            Matrix sig = a.sigmoid();
            return sig.add(a.copy().multiply(sig.rsub(1.0)));
        }
    },
    LEAKY_RELU {
        @Override
        public Matrix apply(Matrix z) {
            return z.applyFunction(x -> x > 0 ? x : 0.01 * x);
        }

        @Override
        public Matrix derivative(Matrix a) {
            return a.applyFunction(x -> x > 0 ? 1.0 : 0.01);
        }
    };

    public abstract Matrix apply(Matrix z);
    public abstract Matrix derivative(Matrix a);
}
