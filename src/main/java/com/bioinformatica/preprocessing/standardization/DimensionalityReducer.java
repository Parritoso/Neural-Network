package com.bioinformatica.preprocessing.standardization;

import com.bioinformatica.preprocessing.integration.RPreprocessor;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 * Clase para la reducción de dimensionalidad con PCA y t-SNE.
 */
public class DimensionalityReducer {
    public static double[][] aplicarPCA(double[][] datos, int numComponentes) {
        RealMatrix matrix = new Array2DRowRealMatrix(datos);
        Covariance covariance = new Covariance(matrix);
        RealMatrix covMatrix = covariance.getCovarianceMatrix();
        EigenDecomposition eigen = new EigenDecomposition(covMatrix);

        RealMatrix eigenVectors = eigen.getV();
        RealMatrix pcaResult = matrix.multiply(eigenVectors.getSubMatrix(0, matrix.getColumnDimension() - 1, 0, numComponentes - 1));

        return pcaResult.getData();
    }

    /**
     * Ejecuta t-SNE en R usando un script externo a través de RPreprocessor.
     * @param rutaScript Ruta del script R.
     * @param rPreprocessor Instancia de RPreprocessor para ejecutar el script.
     */
    public static void aplicarTSNEConR(String rutaScript, RPreprocessor rPreprocessor) {
        rPreprocessor.ejecutarScript(rutaScript);
    }
}
