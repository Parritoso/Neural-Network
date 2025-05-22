package com.bioinformatica.load.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TrainingDataAndLabels {
    private float[][] trainingData;
    private float[][] trainingLabels;

    public TrainingDataAndLabels(float[][] trainingData, float[][] trainingLabels) {
        this.trainingData = trainingData;
        this.trainingLabels = trainingLabels;
    }

    public float[][] getTrainingData() {
        return trainingData;
    }

    public float[][] getTrainingLabels() {
        return trainingLabels;
    }

    public static TrainingDataAndLabels cargarDatosYEtiquetas(String rutaArchivo) throws IOException {
        List<float[]> dataList = new ArrayList<>();
        List<float[]> labelsList = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<String> headers = csvParser.getHeaderNames();
            if (headers.isEmpty()) {
                System.err.println("El archivo CSV no tiene cabecera o está vacío.");
                return new TrainingDataAndLabels(new float[0][0], new float[0][0]);
            }

            for (CSVRecord record : csvParser) {
                if (record.size() > 10) {
                    float[] filaTrainingData = new float[10];
                    for (int i = 0; i < 10; i++) {
                        String valor = record.get(i).trim();
                        try {
                            filaTrainingData[i] = Float.parseFloat(valor);
                        } catch (NumberFormatException e) {
                            System.err.println("Error al convertir valor a float (training data). Se usará 0.0.");
                            filaTrainingData[i] = 0.0f;
                        }
                    }
                    dataList.add(filaTrainingData);

                    int numLabels = record.size() - 10;
                    float[] filaTrainingLabels = new float[numLabels];
                    for (int i = 10; i < record.size(); i++) {
                        String valor = record.get(i).trim();
                        try {
                            filaTrainingLabels[i - 10] = Float.parseFloat(valor);
                        } catch (NumberFormatException e) {
                            System.err.println("Error al convertir valor a float (training labels). Se usará 0.0.");
                            filaTrainingLabels[i - 10] = 0.0f;
                        }
                    }
                    labelsList.add(filaTrainingLabels);
                } else if (record.size() == 10) {
                    float[] filaTrainingData = new float[10];
                    for (int i = 0; i < 10; i++) {
                        String valor = record.get(i).trim();
                        try {
                            filaTrainingData[i] = Float.parseFloat(valor);
                        } catch (NumberFormatException e) {
                            System.err.println("Error al convertir valor a float (training data). Se usará 0.0.");
                            filaTrainingData[i] = 0.0f;
                        }
                    }
                    dataList.add(filaTrainingData);
                    // No hay etiquetas para esta fila
                    labelsList.add(new float[0]); // O maneja esto de la forma que sea apropiada para tu caso
                    System.err.println("Advertencia: Fila con solo 10 columnas, no se encontraron etiquetas.");
                } else {
                    System.err.println("Advertencia: Fila con menos de 10 columnas, se omitirá.");
                }
            }

            System.out.println("Número de filas de datos cargados: " + dataList.size());
            System.out.println("Número de filas de etiquetas cargadas: " + labelsList.size());

            return new TrainingDataAndLabels(dataList.toArray(new float[dataList.size()][]), labelsList.toArray(new float[labelsList.size()][]));

        } catch (IOException e) {
            System.err.println("Error al leer o procesar el archivo CSV: " + e.getMessage());
            return new TrainingDataAndLabels(new float[0][0], new float[0][0]);
        }
    }
}