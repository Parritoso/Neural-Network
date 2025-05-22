package com.bioinformatica.load.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
public class JSONLoader {
    public List<Map<String, Object>> cargarDatos(String rutaArchivo) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(rutaArchivo), List.class);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public float[][] obtenerTrainingData(String rutaArchivo) {
        List<float[]> trainingDataList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Map<String, Object>> datos = mapper.readValue(new File(rutaArchivo), new TypeReference<List<Map<String, Object>>>() {});

            if (datos.isEmpty()) {
                return new float[0][0];
            }

            for (Map<String, Object> objeto : datos) {
                float[] filaTrainingData = new float[10];
                boolean hasAllData = true;
                for (int i = 0; i < 10; i++) {
                    String key = String.valueOf(i); // Asumimos claves "0", "1", ..., "9"
                    Object value = objeto.get(key);
                    if (value != null) {
                        try {
                            filaTrainingData[i] = ((Number) value).floatValue();
                        } catch (ClassCastException e) {
//                            System.err.println("Error al convertir el valor '" + value + "' (clave: '" + key + "') a float en training data. Se usará 0.0 en su lugar.");
                            filaTrainingData[i] = 0.0f;
                        } catch (NullPointerException e) {
//                            System.err.println("Valor nulo encontrado para la clave '" + key + "' en training data. Se usará 0.0 en su lugar.");
                            filaTrainingData[i] = 0.0f;
                            hasAllData = false;
                        }
                    } else {
//                        System.err.println("Clave '" + key + "' no encontrada en training data. Se usará 0.0 en su lugar.");
                        filaTrainingData[i] = 0.0f;
                        hasAllData = false;
                    }
                }
                if (hasAllData) {
                    trainingDataList.add(filaTrainingData);
                } else {
                    System.err.println("Fila incompleta para training data, omitiendo.");
                }
            }

            return trainingDataList.toArray(new float[trainingDataList.size()][]);

        } catch (IOException e) {
//            System.err.println("Error al leer o procesar el archivo JSON para training data: " + e.getMessage());
            return new float[0][0];
        }
    }

    public float[][] obtenerTrainingLabels(String rutaArchivo) {
        List<float[]> trainingLabelsList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Map<String, Object>> datos = mapper.readValue(new File(rutaArchivo), new TypeReference<List<Map<String, Object>>>() {});

            if (datos.isEmpty()) {
                return new float[0][0];
            }

            for (Map<String, Object> objeto : datos) {
                List<Float> filaTrainingLabels = new ArrayList<>();
                for (Map.Entry<String, Object> entry : objeto.entrySet()) {
                    try {
                        int key = Integer.parseInt(entry.getKey());
                        if (key >= 10) {
                            Object value = entry.getValue();
                            if (value != null) {
                                filaTrainingLabels.add(((Number) value).floatValue());
                            } else {
//                                System.err.println("Valor nulo encontrado para la clave '" + entry.getKey() + "' en training labels. Se usará 0.0 en su lugar.");
                                filaTrainingLabels.add(0.0f);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar claves no numéricas o menores que 10
                    } catch (ClassCastException e) {
//                        System.err.println("Error al convertir el valor '" + entry.getValue() + "' (clave: '" + entry.getKey() + "') a float en training labels. Se usará 0.0 en su lugar.");
                        filaTrainingLabels.add(0.0f);
                    }
                }
                if (!filaTrainingLabels.isEmpty()) {
                    float[] floatArray = new float[filaTrainingLabels.size()];
                    for (int i = 0; i < filaTrainingLabels.size(); i++) {
                        floatArray[i] = filaTrainingLabels.get(i);
                    }
                    trainingLabelsList.add(floatArray);
                }
            }

            return trainingLabelsList.toArray(new float[trainingLabelsList.size()][]);

        } catch (IOException e) {
//            System.err.println("Error al leer o procesar el archivo JSON para training labels: " + e.getMessage());
            return new float[0][0];
        }
    }
}
