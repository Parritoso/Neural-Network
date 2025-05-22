package com.bioinformatica.function_prediction.Neural_network.networkAnalyzer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PredictionInterpreter {
    private double probabilityThreshold = 0.5;
    private Map<Integer, String> geneFunctionMap; // Mapeo de índice a nombre de función génica

    public PredictionInterpreter(String geneFunctionMapFile) {
        this.geneFunctionMap = loadGeneFunctionMap(geneFunctionMapFile);
    }

    public void setProbabilityThreshold(double threshold) {
        this.probabilityThreshold = threshold;
    }

    public Map<String, Double> interpretPrediction(float[] predictionArray) {
        Map<String, Double> interpretedResults = new HashMap<>();
        for (int i = 0; i < predictionArray.length; i++) {
            if (predictionArray[i] >= probabilityThreshold) {
                String geneFunction = geneFunctionMap.get(i);
                if (geneFunction != null && predictionArray[i] > 0) {
                    interpretedResults.put(geneFunction, (double) predictionArray[i]);
                }
            }
        }
        return interpretedResults;
    }

//    public void savePredictionReport(Map<String, Double> predictions, String baseFilePath) {
//        // Lógica para guardar en texto plano (.txt)
//        StringBuilder textOutput = new StringBuilder();
//        for (Map.Entry<String, Double> entry : predictions.entrySet()) {
//            textOutput.append(entry.getKey()).append(": ").append(String.format("%.4f", entry.getValue())).append("\n");
//        }
//        // (Aquí iría el código para escribir textOutput a un archivo)
//        System.out.println("Predicciones guardadas en texto plano en: " + baseFilePath + ".txt");
//
//        // Lógica para guardar en JSON (.json)
//        // (Aquí iría el código para convertir 'predictions' a JSON y escribirlo a un archivo)
//        System.out.println("Predicciones guardadas en JSON en: " + baseFilePath + ".json");
//
//        // Lógica para guardar en CSV (.csv)
//        StringBuilder csvOutput = new StringBuilder("Función Génica,Probabilidad\n");
//        for (Map.Entry<String, Double> entry : predictions.entrySet()) {
//            csvOutput.append(entry.getKey()).append(",").append(String.format("%.4f", entry.getValue())).append("\n");
//        }
//        // (Aquí iría el código para escribir csvOutput a un archivo)
//        System.out.println("Predicciones guardadas en CSV en: " + baseFilePath + ".csv");
//    }

    public void savePredictionReport(Map<String, Double> predictions, String baseFilePath) {
        // Lógica para guardar en texto plano (.txt)
        StringBuilder textOutput = new StringBuilder();
        for (Map.Entry<String, Double> entry : predictions.entrySet()) {
            textOutput.append(entry.getKey()).append(": ").append(String.format("%.4f", entry.getValue())).append("\n");
        }
        try (FileWriter fileWriter = new FileWriter(baseFilePath + ".txt")) {
            fileWriter.write(textOutput.toString());
            System.out.println("Predicciones guardadas en texto plano en: " + baseFilePath + ".txt");
        } catch (IOException e) {
            System.err.println("Error al guardar en texto plano: " + e.getMessage());
        }

        // Lógica para guardar en JSON (.json)
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(predictions);
        try (FileWriter fileWriter = new FileWriter(baseFilePath + ".json")) {
            fileWriter.write(jsonOutput);
            System.out.println("Predicciones guardadas en JSON en: " + baseFilePath + ".json");
        } catch (IOException e) {
            System.err.println("Error al guardar en JSON: " + e.getMessage());
        }

        // Lógica para guardar en CSV (.csv)
        StringBuilder csvOutput = new StringBuilder("Función Génica,Probabilidad\n");
        for (Map.Entry<String, Double> entry : predictions.entrySet()) {
            csvOutput.append(entry.getKey().replace(",", ";")).append(",").append(String.format("%.4f", entry.getValue())).append("\n");
        }
        try (FileWriter fileWriter = new FileWriter(baseFilePath + ".csv")) {
            fileWriter.write(csvOutput.toString());
            System.out.println("Predicciones guardadas en CSV en: " + baseFilePath + ".csv");
        } catch (IOException e) {
            System.err.println("Error al guardar en CSV: " + e.getMessage());
        }
    }

    private Map<Integer, String> loadGeneFunctionMap(String filePath) {
        // Lógica para leer el archivo de mapeo (por ejemplo, CSV con índice y nombre)
        Map<Integer, String> map = new HashMap<>();
        // (Aquí iría el código para leer el archivo y llenar el mapa)
        BufferedReader reader = null;
        String line = null;
        String csvSplitBy = ",";
        boolean isHeader = true;

        try {
            reader = new BufferedReader(new FileReader(filePath));
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Saltar la línea del encabezado
                }
                String[] data = line.split(csvSplitBy);
                if (data.length == 2) {
                    try {
                        int position = Integer.parseInt(data[0].trim());
                        String columnName = data[1].trim().replaceAll("\"", ""); // Eliminar las comillas
                        map.put(position, columnName);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al convertir la posición a entero en la línea: " + line);
                    }
                } else {
                    System.err.println("Formato incorrecto en la línea: " + line);
                }
            }
            System.out.println("Mapa de funciones génicas cargado desde: " + filePath + " con " + map.size() + " entradas.");
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + filePath);
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el lector del archivo.");
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
}
