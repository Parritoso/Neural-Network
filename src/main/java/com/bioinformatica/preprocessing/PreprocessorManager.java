package com.bioinformatica.preprocessing;

import com.bioinformatica.preprocessing.cleaning.MissingDataHandler;
import com.bioinformatica.preprocessing.cleaning.OutlierRemover;
import com.bioinformatica.preprocessing.integration.RPreprocessor;
import com.bioinformatica.preprocessing.standardization.DimensionalityReducer;
import com.bioinformatica.preprocessing.standardization.LogTransform;
import com.bioinformatica.preprocessing.standardization.ZScoreNormalizer;
import com.bioinformatica.preprocessing.structures.FeatureSelector;

/**
 * Clase para gestionar y coordinar el preprocesamiento de datos dentro del módulo.
 * Aplica limpieza, normalización, reducción de dimensionalidad y selección de características.
 */
public class PreprocessorManager {
    private final RPreprocessor rPreprocessor;

    public PreprocessorManager() {
        this.rPreprocessor = new RPreprocessor();
    }


    /**
     * Ejecuta el flujo completo de preprocesamiento sobre los datos de expresión génica.
     * @param datos Matriz de datos de expresión génica.
     * @param etiquetas Etiquetas de las muestras para la selección de características.
     * @param umbralP Valor umbral para ANOVA.
     * @param numComponentes Número de componentes para PCA.
     */
    public void ejecutarPreprocesamiento(double[][] datos, int[] etiquetas, double umbralP, int numComponentes) {
        System.out.println("Iniciando preprocesamiento de datos...");

        // Eliminación de valores atípicos
        double[][] datosLimpios = OutlierRemover.eliminarAtipicos(datos);
        System.out.println("Valores atípicos eliminados.");

        // Manejo de datos faltantes
        MissingDataHandler.imputarDatosFaltantes(datosLimpios);
        System.out.println("Datos faltantes imputados.");

        // Normalización Z-score
        ZScoreNormalizer.normalizar(datosLimpios);
        System.out.println("Datos normalizados con Z-score.");

        // Transformación logarítmica
        LogTransform.transformar(datosLimpios);
        System.out.println("Transformación logarítmica aplicada.");

        // Reducción de dimensionalidad con PCA
        double[][] pcaDatos = DimensionalityReducer.aplicarPCA(datosLimpios, numComponentes);
        System.out.println("Reducción de dimensionalidad con PCA aplicada.");

        // Selección de características con ANOVA
        double[][] datosFiltrados = FeatureSelector.seleccionarConANOVA(pcaDatos, etiquetas, umbralP);
        System.out.println("Selección de características con ANOVA aplicada.");

        // Selección de características con Chi-cuadrado en R
        FeatureSelector.seleccionarConChiCuadrado("resources/scripts_r/chi_square_script.R", rPreprocessor);
        System.out.println("Selección de características con Chi-cuadrado aplicada en R.");

        System.out.println("Preprocesamiento finalizado.");
    }
}
