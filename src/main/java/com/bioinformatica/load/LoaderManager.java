package com.bioinformatica.load;

import com.bioinformatica.load.csv.CSVLoader;
import com.bioinformatica.load.csv.TrainingDataAndLabels;
import com.bioinformatica.load.json.JSONLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class LoaderManager {
    private final CSVLoader csvLoader;
    private final JSONLoader jsonLoader;
    TrainingDataAndLabels trainingDataAndLabels = null;

    public LoaderManager() {
        this.csvLoader = new CSVLoader();
        this.jsonLoader = new JSONLoader();
    }

    public List<Map<String, String>> cargarCSV(String rutaArchivo) {
        return csvLoader.cargarDatos(rutaArchivo);
    }

    public List<Map<String, Object>> cargarJSON(String rutaArchivo) {
        return jsonLoader.cargarDatos(rutaArchivo);
    }

    public List<?> cargarDatos(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.err.println("El archivo no existe: " + rutaArchivo);
            return Collections.emptyList();
        }

        String extension = obtenerExtension(archivo);
        switch (extension) {
            case "csv":
                return validarDatos(csvLoader.cargarDatos(rutaArchivo));
            case "json":
                return validarDatos(jsonLoader.cargarDatos(rutaArchivo));
            case "txt":
                return validarDatos(cargarTXT(rutaArchivo));
            default:
                System.err.println("Formato de archivo no soportado: " + extension);
                return Collections.emptyList();
        }
    }

    public void obtenerDatos(String nombreArchivo) throws IOException {
        Path rutaArchivo = Paths.get("resources", nombreArchivo);
        trainingDataAndLabels = TrainingDataAndLabels.cargarDatosYEtiquetas(rutaArchivo.toString());
    }

    public float[][] getTrainingData() {
        return trainingDataAndLabels.getTrainingData();
    }

    public float[][] getTrainingLabels() {
        return trainingDataAndLabels.getTrainingLabels();
    }

    public float[][] obtenerTrainingData(String nombreArchivo) throws Exception {
        Path rutaArchivo = Paths.get("resources", nombreArchivo);
        float[][] trainingDataCSV = null;
        float[][] trainingDataJSON = null;
        Exception csvException = null;
        Exception jsonException = null;

        try {
            trainingDataCSV = csvLoader.obtenerTrainingData(rutaArchivo.toString());
            if (trainingDataCSV.length > 0 && trainingDataCSV[0].length == 10) {
                return trainingDataCSV; // Carga exitosa como CSV
            } else if (trainingDataCSV.length > 0) {
                System.err.println("Advertencia: La training data cargada desde CSV no tiene 10 columnas.");
            }
        } catch (Exception e) {
            csvException = e;
            throw csvException;
            // No mostrar el error aquí, intentaremos con JSON
        }

        try {
            trainingDataJSON = jsonLoader.obtenerTrainingData(rutaArchivo.toString());
            if (trainingDataJSON.length > 0 && trainingDataJSON[0].length == 10) {
                return trainingDataJSON; // Carga exitosa como JSON
            } else if (trainingDataJSON.length > 0) {
                System.err.println("Advertencia: La training data cargada desde JSON no tiene 10 columnas.");
            }
        } catch (Exception e) {
            jsonException = e;
            // No mostrar el error aquí
        }

        // Si ambos intentos fallaron, mostrar las excepciones
        if (csvException != null && jsonException != null) {
            System.err.println("Error al intentar obtener training data desde CSV: " + csvException.getMessage());
            System.err.println("Error al intentar obtener training data desde JSON: " + jsonException.getMessage());
        } else if (csvException != null) {
            System.err.println("Error al intentar obtener training data desde CSV: " + csvException.getMessage());
        } else if (jsonException != null) {
            System.err.println("Error al intentar obtener training data desde JSON: " + jsonException.getMessage());
        }

        return new float[0][0]; // Devolver una matriz vacía si ambos fallan o no tienen el formato esperado
    }

    public float[][] obtenerTrainingLabels(String nombreArchivo) {
        Path rutaArchivo = Paths.get("resources", nombreArchivo);
        float[][] trainingLabelsCSV = null;
        float[][] trainingLabelsJSON = null;
        Exception csvException = null;
        Exception jsonException = null;

        try {
            trainingLabelsCSV = csvLoader.obtenerTrainingLabels(rutaArchivo.toString());
            if (trainingLabelsCSV.length > 0 && trainingLabelsCSV[0].length > 0) {
                return trainingLabelsCSV; // Carga exitosa como CSV
            } else if (trainingLabelsCSV.length > 0) {
                System.err.println("Advertencia: La training labels cargada desde CSV está vacía.");
            }
        } catch (Exception e) {
            csvException = e;
            System.err.println("Error en CSV");
            // No mostrar el error aquí, intentaremos con JSON
        }

        try {
            trainingLabelsJSON = jsonLoader.obtenerTrainingLabels(rutaArchivo.toString());
            if (trainingLabelsJSON.length > 0 && trainingLabelsJSON[0].length > 0) {
                return trainingLabelsJSON; // Carga exitosa como JSON
            } else if (trainingLabelsJSON.length > 0) {
                System.err.println("Advertencia: La training labels cargada desde JSON está vacía.");
            }
        } catch (Exception e) {
            jsonException = e;
            System.err.println("Error en Json");
            // No mostrar el error aquí
        }

        // Si ambos intentos fallaron, mostrar las excepciones
        if (csvException != null && jsonException != null) {
            System.err.println("Error al intentar obtener training labels desde CSV: " + csvException.getMessage());
            System.err.println("Error al intentar obtener training labels desde JSON: " + jsonException.getMessage());
        } else if (csvException != null) {
            System.err.println("Error al intentar obtener training labels desde CSV: " + csvException.getMessage());
        } else if (jsonException != null) {
            System.err.println("Error al intentar obtener training labels desde JSON: " + jsonException.getMessage());
        }

        return new float[0][0]; // Devolver una matriz vacía si ambos fallan o no tienen el formato esperado
    }


    public List<Map<String, Object>> cargarDesdeBaseDeDatos(String url, String usuario, String contrasenya, String consultaSQL) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(url, usuario, contrasenya);
             PreparedStatement stmt = conexion.prepareStatement(consultaSQL);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnas = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                for (int i = 1; i <= columnas; i++) {
                    fila.put(metaData.getColumnName(i), rs.getObject(i));
                }
                resultados.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos de la base de datos: " + e.getMessage());
        }
        return validarDatos(resultados);
    }

    private <T> List<T> validarDatos(List<T> datos) {
        if (datos.isEmpty()) {
            System.err.println("Advertencia: Los datos están vacíos o no tienen registros válidos.");
        }
        return datos;
    }

    public String extraerDatosDesdeURL(String url) {
        StringBuilder resultado = new StringBuilder();
        try {
            HttpURLConnection conexion = (HttpURLConnection) new URL(url).openConnection();
            conexion.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream(), StandardCharsets.UTF_8))) {
                resultado.append(reader.lines().collect(Collectors.joining("\n")));
            }
        } catch (IOException e) {
            System.err.println("Error al obtener datos desde la URL: " + url + " - " + e.getMessage());
            return "";
        }
        return resultado.toString();
    }

    public void descargarYConvertir(String url, String formatoDestino, String archivoSalida) {
        String datos = extraerDatosDesdeURL(url);
        if (datos.isEmpty()) {
            System.err.println("No se pudieron extraer datos desde: " + url);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida))) {
            if ("csv".equalsIgnoreCase(formatoDestino)) {
                writer.write(convertirAFormatoCSV(datos));
            } else if ("json".equalsIgnoreCase(formatoDestino)) {
                writer.write(convertirAFormatoJSON(datos));
            } else {
                System.err.println("Formato de conversión no soportado: " + formatoDestino);
            }
        } catch (IOException e) {
            System.err.println("Error al escribir archivo convertido: " + e.getMessage());
        }
    }

    private String convertirAFormatoCSV(String datos) {
        return datos.replaceAll("\t", ","); // Suponiendo datos tabulados
    }

    private String convertirAFormatoJSON(String datos) {
        return "{\"datos\": [" + datos.replaceAll("\n", ",") + "]}";
    }

    private List<String> cargarTXT(String rutaArchivo) {
        List<String> datos = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(rutaArchivo))) {
            while (scanner.hasNextLine()) {
                datos.add(scanner.nextLine());
            }
        } catch (Exception e) {
            System.err.println("Error al leer el archivo TXT: " + e.getMessage());
        }
        return datos;
    }

    private String obtenerExtension(File archivo) {
        String nombre = archivo.getName();
        int lastIndex = nombre.lastIndexOf('.');
        return (lastIndex == -1) ? "" : nombre.substring(lastIndex + 1).toLowerCase();
    }

    public void mostrarDatosCSV(String rutaArchivo) {
        List<Map<String, String>> datos = cargarCSV(rutaArchivo);
        System.out.println("\nDatos CSV cargados:");
        for (Map<String, String> fila : datos) {
            System.out.println(fila);
        }
    }

    public void mostrarDatosJSON(String rutaArchivo) {
        List<Map<String, Object>> datos = cargarJSON(rutaArchivo);
        System.out.println("\nDatos JSON cargados:");
        for (Map<String, Object> fila : datos) {
            System.out.println(fila);
        }
    }
}
