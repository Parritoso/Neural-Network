package com.bioinformatica.function_prediction.Neural_network.DataInterpreter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataInterpreterTest {
    private final DataInterpreter interpreter = new DataInterpreter();
    private static final float DELTA = 1e-6f;

    @Test
    void testInterpretFloatArray_validInput() {
        float[] data = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f};
        float[] result = interpreter.interpretFloatArray(data);
        assertArrayEquals(data, result, DELTA);
    }

    @Test
    void testInterpretFloatArray_invalidInputSize() {
        float[] data = {1.0f, 2.0f, 3.0f};
        float[] result = interpreter.interpretFloatArray(data);
        assertNull(result);
    }

    @Test
    void testInterpretCSVLine_validInputWithLabel() throws IOException {
        String line = "1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,0.5";
        CSVFormat format = CSVFormat.DEFAULT;
        try (StringReader reader = new StringReader(line);
             CSVParser parser = new CSVParser(reader, format)) {
            CSVRecord record = parser.getRecords().get(0); // Obtiene el primer (y único) registro
            float[] result = interpreter.interpretCSVLine(record, true);
            assertNotNull(result);
            assertEquals(1.0f, result[0], DELTA);
            assertEquals(10.0f, result[9], DELTA);
        }
    }

    @Test
    void testInterpretCSVLine_validInputWithoutLabel() throws IOException {
        String line = "1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0";
        CSVFormat format = CSVFormat.DEFAULT;
        try (StringReader reader = new StringReader(line);
             CSVParser parser = new CSVParser(reader, format)) {
            CSVRecord record = parser.getRecords().get(0); // Obtiene el primer (y único) registro
            float[] result = interpreter.interpretCSVLine(record, false);
            assertNotNull(result);
            assertEquals(1.0f, result[0], DELTA);
            assertEquals(10.0f, result[9], DELTA);
        }
    }

    @Test
    void testLoadTrainingDataFromCSV_validFile() {
        List<float[]> trainingData = interpreter.loadTrainingDataFromCSV("src/test/resources/test_data.csv");
        assertNotNull(trainingData);
        assertEquals(2, trainingData.size());
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f}, interpreter.getInputData(trainingData.get(0)), DELTA);
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 0.5f}, trainingData.get(0), DELTA);
        assertArrayEquals(new float[]{11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f, 17.0f, 18.0f, 19.0f, 20.0f, 1.0f}, trainingData.get(1), DELTA);
    }

    @Test
    void testInterpretJsonFile_validFile() {
        float[] result = interpreter.interpretJsonFile("src/test/resources/test_data.json");
        assertNull(result, "Este test espera un archivo JSON con solo un array de 10 números.");
        // Si quieres probar el método interpretJsonFile para un array simple, crea otro archivo JSON de ejemplo.
    }

    @Test
    void testLoadTrainingDataFromJson_validFile() {
        List<float[]> trainingData = interpreter.loadTrainingDataFromJson("src/test/resources/test_data.json");
        assertNotNull(trainingData);
        assertEquals(2, trainingData.size());
        assertArrayEquals(new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 10.1f}, interpreter.getInputData(trainingData.get(0)), DELTA);
        assertEquals(0.1f, interpreter.extractLabel(trainingData.get(0)), DELTA);
        assertArrayEquals(new float[]{11.1f, 12.2f, 13.3f, 14.4f, 15.5f, 16.6f, 17.7f, 18.8f, 19.9f, 20.1f}, interpreter.getInputData(trainingData.get(1)), DELTA);
        assertEquals(0.9f, interpreter.extractLabel(trainingData.get(1)), DELTA);
    }

    @Test
    void testInterpretXmlFile_validFile() {
        float[] result = interpreter.interpretXmlFile("src/test/resources/test_data.xml");
        assertNull(result, "Este test espera un archivo XML con solo una etiqueta <input> con 10 <value>s.");
        // Si quieres probar el método interpretXmlFile para un array simple, crea otro archivo XML de ejemplo.
    }

    @Test
    void testLoadTrainingDataFromXml_validFile() {
        List<float[]> trainingData = interpreter.loadTrainingDataFromXml("src/test/resources/test_data.xml");
        assertNotNull(trainingData);
        assertEquals(2, trainingData.size());
        assertArrayEquals(new float[]{1.2f, 2.3f, 3.4f, 4.5f, 5.6f, 6.7f, 7.8f, 8.9f, 9.0f, 10.2f}, interpreter.getInputData(trainingData.get(0)), DELTA);
        assertEquals(0.2f, interpreter.extractLabel(trainingData.get(0)), DELTA);
        assertArrayEquals(new float[]{11.2f, 12.3f, 13.4f, 14.5f, 15.6f, 16.7f, 17.8f, 18.9f, 19.0f, 20.2f}, interpreter.getInputData(trainingData.get(1)), DELTA);
        assertEquals(0.8f, interpreter.extractLabel(trainingData.get(1)), DELTA);
    }
}
