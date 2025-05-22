package com.bioinformatica.load.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVLoader {
    public List<Map<String, String>> cargarDatos(String rutaArchivo) {
        List<Map<String, String>> datos = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                Map<String, String> fila = new HashMap<>();
                record.toMap().forEach(fila::put);
                datos.add(fila);
            }
        } catch (IOException e) {
//            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        }
        return datos;
    }

    public float[][] obtenerTrainingData(String rutaArchivo) throws IOException {
        List<float[]> trainingDataList = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<String> headers = csvParser.getHeaderNames();
            if (headers.isEmpty()) {
                System.err.println("El archivo CSV no tiene cabecera o está vacío.");
                return new float[0][0]; // Devolver una matriz vacía
            }

            for (CSVRecord record : csvParser) {
                if (record.size() < 10) {
                    System.err.println("Advertencia: La fila tiene menos de 10 columnas para training data.");
                    continue; // Saltar esta fila
                }
                float[] filaTrainingData = new float[10];
                for (int i = 0; i < 10; i++) {
                    String valor = record.get(i).trim();
                    try {
                        filaTrainingData[i] = Float.parseFloat(valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al convertir el valor '" + valor + "' a float en la columna '" + headers.get(i) + "' (training data). Se usará 0.0 en su lugar.");
                        filaTrainingData[i] = 0.0f;
                    }
                }
                trainingDataList.add(filaTrainingData);
            }

//            System.out.println("trainingDataList.size(): "+trainingDataList.size());
//            System.out.println("trainingLabelsDataList.get(0).length: "+((float[]) trainingDataList.get(0)).length);
            if (!trainingDataList.isEmpty()) {
                float[][] aux = trainingDataList.toArray(new float[trainingDataList.size()][]);
//                System.out.println("aux.length: "+aux.length);
//                System.out.println("aux[0].length: "+aux[0].length);
                return trainingDataList.toArray(new float[trainingDataList.size()][]);
            } else {
                return new float[0][0]; // Devolver una matriz vacía si no hay datos válidos
            }

        } catch (IOException e) {
            System.err.println("Error al leer o procesar el archivo CSV para training data: " + e.getMessage());
            return new float[0][0]; // Devolver una matriz vacía en caso de error
        }
    }

    public float[][] obtenerTrainingLabels(String rutaArchivo) {
        List<float[]> trainingLabelsList = new ArrayList<>();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<String> headers = csvParser.getHeaderNames();
            if (headers.isEmpty()) {
                System.err.println("El archivo CSV no tiene cabecera o está vacío.");
                return new float[0][0]; // Devolver una matriz vacía
            }

            for (CSVRecord record : csvParser) {
//                System.out.println("record.size(): "+record.size());
                if (record.size() <= 10) {
                    System.err.println("Advertencia: La fila no tiene suficientes columnas para training labels.");
                    continue; // Saltar esta fila
                }
                int numLabels = record.size() - 10;
                float[] filaTrainingLabels = new float[numLabels];
//                System.out.println("numLabels: "+numLabels);
                for (int i = 10; i < record.size(); i++) {
                    String valor = record.get(i).trim();
//                    System.out.println(record.get(i).trim());
//                    System.out.println("i: "+i);
                    try {
                        filaTrainingLabels[i - 10] = Float.parseFloat(valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al convertir el valor '" + valor + "' a float en la columna '" + headers.get(i) + "' (training labels). Se usará 0.0 en su lugar.");
                        filaTrainingLabels[i - 10] = 0.0f;
                    }
//                    System.out.println("########################");
                }
                trainingLabelsList.add(filaTrainingLabels);
//                System.out.println("=================================================");
            }

//            System.out.println("trainingLabels.size(): " + trainingLabelsList.size());
//            System.out.println("trainingLabelsList.get(0).length: " + ((float[]) trainingLabelsList.get(0)).length);
            if (!trainingLabelsList.isEmpty()) {
                float[][] aux = trainingLabelsList.toArray(new float[trainingLabelsList.size()][]);
//                System.out.println("aux.length: "+aux.length);
//                System.out.println("aux[0].length: "+aux[0].length);
                return trainingLabelsList.toArray(new float[trainingLabelsList.size()][]);
            } else {
                return new float[0][0]; // Devolver una matriz vacía si no hay datos válidos
            }

        } catch (IOException e) {
            System.err.println("Error al leer o procesar el archivo CSV para training labels: " + e.getMessage());
            return new float[0][0]; // Devolver una matriz vacía en caso de error
        }
    }
}
