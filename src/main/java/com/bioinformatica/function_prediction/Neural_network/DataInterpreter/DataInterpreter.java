package com.bioinformatica.function_prediction.Neural_network.DataInterpreter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataInterpreter {
    // Método para interpretar un array de floats directamente
    public float[] interpretFloatArray(float[] data) {
        if (data.length == 10) {
            return data;
        } else {
            System.err.println("Error: Se esperaban 10 valores de entrada, pero se recibieron " + data.length + ".");
            return null; // O podrías lanzar una excepción
        }
    }
    // Método para interpretar una línea de un archivo CSV usando Apache Commons CSV
    public float[] interpretCSVLine(CSVRecord record, boolean hasLabel) {
        int expectedLength = hasLabel ? 11 : 10;
        if (record.size() == expectedLength) {
            float[] result = new float[10];
            for (int i = 0; i < 10; i++) {
                try {
                    result[i] = Float.parseFloat(record.get(i).trim());
                } catch (NumberFormatException e) {
                    System.err.println("Error: No se pudo convertir el valor '" + record.get(i) + "' a un número.");
                    return null;
                }
            }
            return result;
        } else {
            System.err.println("Error: Se esperaban " + expectedLength + " valores en la línea CSV, pero se encontraron " + record.size() + ".");
            return null;
        }
    }
    // Método para leer datos de un archivo CSV y devolverlos como una lista de entradas (y etiquetas si las hay)
    public List<float[]> loadTrainingDataFromCSV(String filePath) {
        List<float[]> trainingData = new ArrayList<>();
        CSVFormat format = CSVFormat.DEFAULT;
        try (Reader reader = new FileReader(filePath);
             CSVParser parser = new CSVParser(reader, format)) {
            for (CSVRecord record : parser) {
                float[] interpretedData = interpretCSVLine(record, true); // Asumimos que la última columna es la etiqueta
                if (interpretedData != null) {
                    // Ahora necesitamos extraer la etiqueta del record y añadirla al array
                    if (record.size() == 11) {
                        float label;
                        try {
                            // La etiqueta está en la última columna (índice 10)
                            label = Float.parseFloat(record.get(10).trim());
                            float[] dataWithLabel = new float[11];
                            System.arraycopy(interpretedData, 0, dataWithLabel, 0, 10);
                            dataWithLabel[10] = label;
                            trainingData.add(dataWithLabel);
                        } catch (NumberFormatException e) {
                            System.err.println("Error al leer la etiqueta del CSV: " + e.getMessage());
                        }
                    } else if (record.size() == 10) {
                        trainingData.add(interpretedData); // Para datos sin etiqueta
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        }
        return trainingData;
    }
    // Método para extraer la etiqueta (si está presente) de los datos interpretados
    public float extractLabel(float[] interpretedData) {
        if (interpretedData != null && interpretedData.length == 11) {
            return interpretedData[10]; // Asumimos que la etiqueta es el último valor
        } else {
            System.err.println("Error: No se puede extraer la etiqueta. Se esperaban 11 valores.");
            return -1; // O algún valor que indique ausencia de etiqueta
        }
    }

    // Método para obtener solo los valores de entrada para la predicción
    public float[] getInputData(float[] interpretedData) {
        if (interpretedData != null && (interpretedData.length == 10 || interpretedData.length == 11)) {
            return Arrays.copyOfRange(interpretedData, 0, 10);
        } else {
            System.err.println("Error: Formato de datos incorrecto para obtener la entrada.");
            return null;
        }
    }

    // Método para interpretar un array de 10 floats desde un archivo JSON
    public float[] interpretJsonFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(new File(filePath));
            if (rootNode.isArray() && rootNode.size() == 10) {
                float[] data = new float[10];
                for (int i = 0; i < 10; i++) {
                    JsonNode numberNode = rootNode.get(i);
                    if (numberNode.isNumber()) {
                        data[i] = numberNode.floatValue();
                    } else {
                        System.err.println("Error: El elemento en el índice " + i + " no es un número en el archivo JSON.");
                        return null;
                    }
                }
                return data;
            } else {
                System.err.println("Error: El archivo JSON debe contener un array de 10 números.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            return null;
        }
    }

    // Método para leer datos de entrenamiento (con etiquetas) desde un archivo JSON
    // Asumimos que cada objeto en el array JSON tiene un campo "input" (array de 10 floats)
    // y un campo "label" (un float).
    public List<float[]> loadTrainingDataFromJson(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        List<float[]> trainingData = new ArrayList<>();
        try {
            JsonNode rootNode = mapper.readTree(new File(filePath));
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    JsonNode inputNode = node.get("input");
                    JsonNode labelNode = node.get("label");
                    if (inputNode != null && inputNode.isArray() && inputNode.size() == 10 && labelNode != null && labelNode.isNumber()) {
                        float[] inputData = new float[11]; // 10 inputs + 1 label
                        for (int i = 0; i < 10; i++) {
                            inputData[i] = inputNode.get(i).floatValue();
                        }
                        inputData[10] = labelNode.floatValue();
                        trainingData.add(inputData);
                    } else {
                        System.err.println("Advertencia: Objeto JSON con formato incorrecto, omitiendo.");
                    }
                }
            } else {
                System.err.println("Error: El archivo JSON debe contener un array de objetos.");
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
        }
        return trainingData;
    }

    // Método para interpretar un array de 10 floats desde un archivo XML
    public float[] interpretXmlFile(String filePath) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList inputList = doc.getElementsByTagName("input"); // Busca la etiqueta 'input'
            if (inputList.getLength() == 1) {
                Element inputElement = (Element) inputList.item(0);
                NodeList valueNodes = inputElement.getElementsByTagName("value"); // Dentro de 'input', busca etiquetas 'value'
                if (valueNodes.getLength() == 10) {
                    float[] data = new float[10];
                    for (int i = 0; i < 10; i++) {
                        Element valueElement = (Element) valueNodes.item(i);
                        try {
                            data[i] = Float.parseFloat(valueElement.getTextContent());
                        } catch (NumberFormatException e) {
                            System.err.println("Error: No se pudo convertir el valor XML '" + valueElement.getTextContent() + "' a un número.");
                            return null;
                        }
                    }
                    return data;
                } else {
                    System.err.println("Error: La etiqueta 'input' debe contener 10 etiquetas 'value'.");
                    return null;
                }
            } else {
                System.err.println("Error: El archivo XML debe contener una única etiqueta 'input'.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error al leer el archivo XML: " + e.getMessage());
            return null;
        }
    }

    // Método para leer datos de entrenamiento (con etiquetas) desde un archivo XML
    // Asumimos un formato donde cada 'record' tiene un 'input' (10 'value's) y una 'label'.
    public List<float[]> loadTrainingDataFromXml(String filePath) {
        List<float[]> trainingData = new ArrayList<>();
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList recordList = doc.getElementsByTagName("record"); // Busca etiquetas 'record'
            for (int i = 0; i < recordList.getLength(); i++) {
                Element recordElement = (Element) recordList.item(i);
                NodeList inputList = recordElement.getElementsByTagName("input");
                NodeList labelList = recordElement.getElementsByTagName("label");

                if (inputList.getLength() == 1 && labelList.getLength() == 1) {
                    Element inputElement = (Element) inputList.item(0);
                    NodeList valueNodes = inputElement.getElementsByTagName("value");
                    Element labelElement = (Element) labelList.item(0);

                    if (valueNodes.getLength() == 10) {
                        float[] inputData = new float[11]; // 10 inputs + 1 label
                        for (int j = 0; j < 10; j++) {
                            Element valueElement = (Element) valueNodes.item(j);
                            inputData[j] = Float.parseFloat(valueElement.getTextContent());
                        }
                        inputData[10] = Float.parseFloat(labelElement.getTextContent());
                        trainingData.add(inputData);
                    } else {
                        System.err.println("Advertencia: Registro XML con 10 valores de entrada incorrectos, omitiendo.");
                    }
                } else {
                    System.err.println("Advertencia: Registro XML sin etiqueta 'input' o 'label', omitiendo.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer el archivo XML: " + e.getMessage());
        }
        return trainingData;
    }
}
