package com.bioinformatica.function_prediction.Neural_network.networkAnalyzer;

import com.bioinformatica.Main;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
//    public void generateTrainingReport(List<Double> lossHistory, long trainingTime, Map<String, Object> hyperparameters, String filePath) {
//        String pdfFilePath = filePath + ".pdf";
//        String docxFilePath = filePath + ".docx";
//
//        generateTrainingReportPDF(lossHistory, trainingTime, hyperparameters, pdfFilePath);
//        generateTrainingReportDOCX(lossHistory, trainingTime, hyperparameters, docxFilePath);
//
//        System.out.println("Informe de entrenamiento generado en: " + pdfFilePath + " y " + docxFilePath);
//    }
    public void generateTrainingReport(List<Double> lossHistory, long trainingTime, Map<String, Object> hyperparameters, String filePath,float[][] trainingData, float[][] trainingLabels) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yOffset = page.getMediaBox().getHeight() - 50;
                float leading = 15;

                contentStream.beginText();
                // Título
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                contentStream.setFont(font, 12);
                contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, 50, yOffset)); // Nueva matriz
                //contentStream.newLineAtOffset(50, page.getMediaBox().getHeight() - 50);
                contentStream.showText("--- Reporte de Entrenamiento ---");
                contentStream.newLineAtOffset(0, -leading);
                yOffset -= leading;

                // Historial de Pérdida
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                if (lossHistory != null && !lossHistory.isEmpty()) {
                    contentStream.showText("Perdida en la primera época: " + String.format("%.9f", lossHistory.get(0)));
                    contentStream.newLineAtOffset(0, -10);
                    yOffset -= 10;
                    contentStream.showText("Perdida en la última época: " + String.format("%.9f", lossHistory.get(lossHistory.size() - 1)));
                    contentStream.newLineAtOffset(0, -leading);
                    yOffset -= leading;
                    // Aquí podrías añadir más detalles del historial de pérdida si es necesario
                } else {
                    contentStream.showText("No hay historial de perdida disponible.");
                    contentStream.newLineAtOffset(0, -leading);
                    yOffset -= leading;
                }

                // Tiempo de Entrenamiento
                contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, 50, yOffset)); // Nueva matriz
                long seconds = (trainingTime / 1000) % 60;
                long minutes = (trainingTime / (1000 * 60)) % 60;
                long hours = trainingTime / (1000 * 60 * 60);
                contentStream.showText("Tiempo total de entrenamiento: " + hours + " horas, " + minutes + " minutos, " + seconds + " segundos.");
                contentStream.newLineAtOffset(0, -leading);
                yOffset -= leading;

                // Hiperparámetros
                contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, 50, yOffset)); // Nueva matriz
                if (hyperparameters != null && !hyperparameters.isEmpty()) {
                    contentStream.showText("--- Hiperparametros de la Red ---");
                    contentStream.newLineAtOffset(0, -leading);
                    for (Map.Entry<String, Object> entry : hyperparameters.entrySet()) {
                        contentStream.showText(entry.getKey() + ": " + entry.getValue());
                        contentStream.newLineAtOffset(0, -10);
                        yOffset -= 10;
                    }
                } else {
                    contentStream.showText("No se proporcionaron hiperparametros.");
                    contentStream.newLineAtOffset(0, -10);
                    yOffset -= 10;
                }
                yOffset -= leading;
                contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, 50, yOffset)); // Nueva matriz
                if (trainingData != null) {
                    contentStream.showText("--- Datos de Entrenamiento ---");
                    contentStream.newLineAtOffset(0, -leading);
                    contentStream.showText("Numero de muestras de entrenamiento: " + trainingData.length);
                    contentStream.newLineAtOffset(0, -10);
                    yOffset -= 10;
                    if (trainingData.length > 0) {
                        contentStream.showText("Numero de caracteristicas por muestra: " + trainingData[0].length);
                        contentStream.newLineAtOffset(0, -10);
                        yOffset -= 10;
                    }
                } else {
                    contentStream.showText("No hay datos de entrenamiento disponibles.");
                    contentStream.newLineAtOffset(0, -10);
                    yOffset -= 10;
                }

                if (trainingLabels != null) {
                    contentStream.showText("Numero de etiquetas de entrenamiento: " + trainingLabels.length);
                    contentStream.newLineAtOffset(0, -10);
                    yOffset -= 10;
                } else {
                    System.out.println("No hay etiquetas de entrenamiento disponibles.");
                    contentStream.newLineAtOffset(0, -10);
                    yOffset -= 10;
                }
                contentStream.endText();
            } finally {
                contentStream.close();
            }
            document.save(new File(filePath +".pdf"));
        } catch (IOException e) {
            System.err.println("Error al generar el reporte de entrenamiento en PDF: " + e.getMessage());
        }

        try (XWPFDocument document = new XWPFDocument()) {
            FileOutputStream outputStream = new FileOutputStream(new File(filePath + ".docx"));
            org.apache.poi.xwpf.usermodel.XWPFParagraph title = document.createParagraph();
            org.apache.poi.xwpf.usermodel.XWPFRun runTitle = title.createRun();
            runTitle.setText("--- Reporte de Entrenamiento ---");
            runTitle.setBold(true);
            runTitle.setFontSize(14);

            org.apache.poi.xwpf.usermodel.XWPFParagraph lossParagraph = document.createParagraph();
            org.apache.poi.xwpf.usermodel.XWPFRun runLoss = lossParagraph.createRun();
            if (lossHistory != null && !lossHistory.isEmpty()) {
                runLoss.setText("Perdida en la primera época: " + String.format("%.9f", lossHistory.get(0)));
                runLoss.addBreak();
                runLoss.setText("Perdida en la última época: " + String.format("%.9f", lossHistory.get(lossHistory.size() - 1)));
            } else {
                runLoss.setText("No hay historial de perdida disponible.");
            }

            org.apache.poi.xwpf.usermodel.XWPFParagraph timeParagraph = document.createParagraph();
            org.apache.poi.xwpf.usermodel.XWPFRun runTime = timeParagraph.createRun();
            long seconds = (trainingTime / 1000) % 60;
            long minutes = (trainingTime / (1000 * 60)) % 60;
            long hours = trainingTime / (1000 * 60 * 60);
            runTime.setText("Tiempo total de entrenamiento: " + hours + " horas, " + minutes + " minutos, " + seconds + " segundos.");

            org.apache.poi.xwpf.usermodel.XWPFParagraph hyperTitle = document.createParagraph();
            org.apache.poi.xwpf.usermodel.XWPFRun runHyperTitle = hyperTitle.createRun();
            runHyperTitle.setText("--- Hiperparametros de la Red ---");
            runHyperTitle.setBold(true);

            if (hyperparameters != null && !hyperparameters.isEmpty()) {
                for (Map.Entry<String, Object> entry : hyperparameters.entrySet()) {
                    org.apache.poi.xwpf.usermodel.XWPFParagraph hyperParam = document.createParagraph();
                    hyperParam.createRun().setText(entry.getKey() + ": " + entry.getValue());
                }
            } else {
                org.apache.poi.xwpf.usermodel.XWPFParagraph hyperParam = document.createParagraph();
                hyperParam.createRun().setText("No se proporcionaron hiperparametros.");
            }

            if(trainingData!=null){
                org.apache.poi.xwpf.usermodel.XWPFParagraph TrainTitle = document.createParagraph();
                org.apache.poi.xwpf.usermodel.XWPFRun runTrainTitle = TrainTitle.createRun();
                runTrainTitle.setText("--- Datos de Entrenamiento ---");
                runTrainTitle.setBold(true);

                org.apache.poi.xwpf.usermodel.XWPFParagraph TrainParagraph = document.createParagraph();
                org.apache.poi.xwpf.usermodel.XWPFRun runTrain = TrainParagraph.createRun();
                runTrain.setText("Numero de muestras de entrenamiento: " + trainingData.length);
                runTrain.addBreak();
                if (trainingData.length > 0) {
                    runTrain.setText("Numero de caracteristicas por muestra: " + trainingData[0].length);
                    runTrain.addBreak();
                }

                if (trainingLabels != null) {
                    runTrain.setText("Numero de etiquetas de entrenamiento: " + trainingLabels.length);
                } else {
                    runTrain.setText("No hay etiquetas de entrenamiento disponibles.");
                }
            }

            document.write(outputStream);
        } catch (IOException e) {
            System.err.println("Error al generar el reporte de entrenamiento en DOCX: " + e.getMessage());
        }
    }

    // Método para generar un informe de las predicciones (tabla con colores)
//    public void generatePredictionTableReport(Map<String, Double> predictions, String filePath) {
//        String pdfFilePath = filePath + ".pdf";
//        String docxFilePath = filePath + ".docx";
//
//        generatePredictionTablePDF(predictions, pdfFilePath);
//        System.out.println("Número de predicciones en el mapa: " + predictions.size());
//        generatePredictionTableDOCX(predictions, docxFilePath);
//
//        System.out.println("Tabla de predicciones generada en: " + pdfFilePath + " y " + docxFilePath);
//    }
    public void generatePredictionTableReport(Map<String, Double> predictions, String filePath) {
        generatePredictionTablePDF(predictions, filePath +".pdf");
        generatePredictionTableDOCX(predictions, filePath + ".docx");
    }

    private void generatePredictionTablePDF(Map<String, Double> predictions, String filePath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float rowHeight = 20;
            float cellMargin = 5;

            float[] columnWidths = {tableWidth * 0.7f, tableWidth * 0.3f};
            String[] headers = {"Funcion Genica", "Probabilidad"};

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                // Bloque de texto para el título
                contentStream.beginText();
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                contentStream.setFont(boldFont, 12);
                String title = "Predicciones de Funciones Genicas";
                float titleWidth = boldFont.getStringWidth(title) / 1000 * 12;
                float titleX = margin + (tableWidth - titleWidth) / 2;
                contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, titleX, yPosition));
                contentStream.showText(title);
                contentStream.endText();
                // Fin del bloque de texto del título
                yPosition -= 20;

                // Dibujar encabezados
                contentStream.setLineWidth(0.5f);
                contentStream.setStrokingColor(Color.BLACK);
                float nextX = margin;
                contentStream.setFont(boldFont, 10);
                PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                for (int i = 0; i < headers.length; i++) {
                    contentStream.addRect(nextX, yPosition, columnWidths[i], rowHeight);
                    contentStream.stroke();
                    // Bloque de texto para el encabezado
                    contentStream.beginText();
                    float textWidth = boldFont.getStringWidth(headers[i]) / 1000 * 10;
                    //float textX = nextX + (columnWidths[i] - textWidth) / 2;
                    float textX = nextX + cellMargin;
                    drawText(contentStream, textX, yPosition + rowHeight/2, headers[i], contentStream);
                    contentStream.endText();
                    // Fin del bloque de texto del encabezado
                    nextX += columnWidths[i];
                }
                yPosition -= rowHeight;

                // Dibujar datos
                contentStream.setFont(normalFont, 10);
                for (Map.Entry<String, Double> entry : predictions.entrySet()) {
                    float probability = entry.getValue().floatValue();
                    // Calcular el color de fondo basado en la probabilidad (de rojo a verde)
                    Color backgroundColor = interpolateColor(Color.RED, Color.GREEN, probability);
                    contentStream.setNonStrokingColor(backgroundColor);
                    contentStream.addRect(margin, yPosition, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(Color.BLACK); // Volver al color negro para el texto y las líneas

                    nextX = margin;
                    contentStream.addRect(nextX, yPosition, columnWidths[0], rowHeight);
                    contentStream.stroke();
                    // Bloque de texto para el nombre del gen
                    contentStream.beginText();
                    float textWidth = normalFont.getStringWidth(entry.getKey()) / 1000 * 10;
                    float textX = nextX + (columnWidths[0] - textWidth) / 2;
                    drawText(contentStream, textX, yPosition+ rowHeight/2, entry.getKey(), contentStream);
                    contentStream.endText();
                    // Fin del bloque de texto del nombre del gen
                    nextX += columnWidths[0];

                    contentStream.addRect(nextX, yPosition, columnWidths[1], rowHeight);
                    contentStream.stroke();
                    // Bloque de texto para la probabilidad
                    contentStream.beginText();
                    String probabilityText = String.format("%.4f", entry.getValue());
                    textWidth = normalFont.getStringWidth(probabilityText) / 1000 * 10;
                    textX = nextX + (columnWidths[1] - textWidth) / 2;
                    drawText(contentStream, textX, yPosition+ rowHeight/2, probabilityText, contentStream);
                    contentStream.endText();
                    // Fin del bloque de texto de la probabilidad

                    yPosition -= rowHeight;
                    if (yPosition <= margin) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(normalFont, 10);
                        yPosition = page.getMediaBox().getHeight() - margin - rowHeight;
                    }
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            document.save(new File(filePath));
        } catch (IOException e) {
            System.err.println("Error al generar la tabla de predicciones en PDF: " + e.getMessage());
        }
    }

    private void generatePredictionTableDOCX(Map<String, Double> predictions, String filePath) {
        try (XWPFDocument document = new XWPFDocument()) {
            FileOutputStream outputStream = new FileOutputStream(new File(filePath));
            XWPFTable table = document.createTable();

            // Encabezados
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("Funcion Genica");
            while (headerRow.getTableCells().size() < 2) {
                headerRow.addNewTableCell();
            }
            headerRow.getCell(1).setText("Probabilidad");

            XWPFTableRow dataRow;
            for (Map.Entry<String, Double> entry : predictions.entrySet()) {
//                System.out.println("Entry: "+ entry.getKey());
                dataRow = table.createRow();
                dataRow.getCell(0).setText(entry.getKey());
                dataRow.getCell(1).setText(String.format("%.4f", entry.getValue()));

                // Calcular el color de fondo basado en la probabilidad (de rojo a verde)
                float probability = entry.getValue().floatValue();
                Color backgroundColor = interpolateColor(Color.RED, Color.GREEN, probability);

                // Establecer el color de fondo de las celdas de la fila
                for (XWPFTableCell cell : dataRow.getTableCells()) {
                    XSSFColor color = new XSSFColor(backgroundColor, null);
                    cell.setColor(color.getARGBHex().substring(2));
                }
            }
            dataRow = table.createRow();
            document.write(outputStream);
        } catch (IOException e) {
            System.err.println("Error al generar la tabla de predicciones en DOCX: " + e.getMessage());
        }
    }

//    private void generatePredictionTablePDF(Map<String, Double> predictions, String filePath) {
//        try (PDDocument document = new PDDocument()) {
//            PDPage page = new PDPage();
//            document.addPage(page);
//
//            float margin = 50;
//            float yStart = page.getMediaBox().getHeight() - margin;
//            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
//            float yPosition = yStart;
//            float rowHeight = 20;
//            float cellMargin = 5;
//
//            float[] columnWidths = {tableWidth * 0.7f, tableWidth * 0.3f};
//            String[] headers = {"Función Génica", "Probabilidad"};
//
//            PDPageContentStream contentStream = new PDPageContentStream(document, page);
//            try {
//                // Título
//                contentStream.beginText();
//                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
//                contentStream.setFont(boldFont, 12);
//                String title = "Predicciones de Funciones Génicas";
//                float titleWidth = boldFont.getStringWidth(title) / 1000 * 12;
//                float titleX = margin + (tableWidth - titleWidth) / 2;
//                contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, titleX, yPosition));
//                contentStream.showText(title);
//                contentStream.endText();
//                yPosition -= 20;
//
//                // Encabezados
//                contentStream.setLineWidth(0.5f);
//                contentStream.setStrokingColor(Color.BLACK);
//                float nextX = margin;
//                contentStream.setFont(boldFont, 10);
//                PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
//                for (int i = 0; i < headers.length; i++) {
//                    contentStream.addRect(nextX, yPosition, columnWidths[i], rowHeight);
//                    contentStream.stroke();
//                    contentStream.beginText();
//                    float textWidth = boldFont.getStringWidth(headers[i]) / 1000 * 10;
//                    float textX = nextX + cellMargin;
//                    drawText(contentStream, textX, yPosition + 10, headers[i], contentStream);
//                    contentStream.endText();
//                    nextX += columnWidths[i];
//                }
//                yPosition -= rowHeight;
//
//                // Datos
//                contentStream.setFont(normalFont, 10);
//                for (Map.Entry<String, Double> entry : predictions.entrySet()) {
//                    float probability = entry.getValue().floatValue();
//                    Color backgroundColor = interpolateColor(Color.RED, Color.GREEN, probability);
//                    contentStream.setNonStrokingColor(backgroundColor);
//                    contentStream.addRect(margin, yPosition, tableWidth, rowHeight);
//                    contentStream.fill();
//                    contentStream.setNonStrokingColor(Color.BLACK);
//
//                    nextX = margin;
//                    contentStream.addRect(nextX, yPosition, columnWidths[0], rowHeight);
//                    contentStream.stroke();
//                    contentStream.beginText();
//                    float textWidth = normalFont.getStringWidth(entry.getKey()) / 1000 * 10;
//                    float textX = nextX + cellMargin;
//                    drawText(contentStream, textX, yPosition + 10, entry.getKey(), contentStream);
//                    contentStream.endText();
//                    nextX += columnWidths[0];
//
//                    contentStream.addRect(nextX, yPosition, columnWidths[1], rowHeight);
//                    contentStream.stroke();
//                    contentStream.beginText();
//                    String probabilityText = String.format("%.4f", entry.getValue());
//                    textWidth = normalFont.getStringWidth(probabilityText) / 1000 * 10;
//                    textX = nextX + cellMargin;
//                    drawText(contentStream, textX, yPosition + 10, probabilityText, contentStream);
//                    contentStream.endText();
//
//                    yPosition -= rowHeight;
//                    if (yPosition <= margin) {
//                        contentStream.close();
//                        page = new PDPage();
//                        document.addPage(page);
//                        contentStream = new PDPageContentStream(document, page);
//                        contentStream.setFont(normalFont, 10);
//                        yPosition = page.getMediaBox().getHeight() - margin - rowHeight;
//                    }
//                }
//            } finally {
//                if (contentStream != null) {
//                    contentStream.close();
//                }
//            }
//            document.save(new File(filePath));
//        } catch (IOException e) {
//            System.err.println("Error al generar la tabla de predicciones en PDF: " + e.getMessage());
//        }
//    }
//
//    private void generatePredictionTableDOCX(Map<String, Double> predictions, String filePath) {
//        try (XWPFDocument document = new XWPFDocument()) {
//            FileOutputStream outputStream = new FileOutputStream(new File(filePath));
//            XWPFTable table = document.createTable();
//
//            // Encabezados
//            XWPFTableRow headerRow = table.createRow();
//            headerRow.getCell(0).setText("Función Génica");
//            while (headerRow.getTableCells().size() < 2) {
//                headerRow.addNewTableCell();
//            }
//            headerRow.getCell(1).setText("Probabilidad");
//
//            for (Map.Entry<String, Double> entry : predictions.entrySet()) {
//                XWPFTableRow dataRow = table.createRow();
//                dataRow.getCell(0).setText(entry.getKey());
//                while (dataRow.getTableCells().size() < 2) {
//                    dataRow.addNewTableCell();
//                }
//                dataRow.getCell(1).setText(String.format("%.4f", entry.getValue()));
//
//                // Calcular el color de fondo basado en la probabilidad (de rojo a verde)
//                float probability = entry.getValue().floatValue();
//                Color backgroundColor = interpolateColor(Color.RED, Color.GREEN, probability);
//
//                // Establecer el color de fondo de las celdas de la fila
//                for (XWPFTableCell cell : dataRow.getTableCells()) {
//                    XSSFColor color = new XSSFColor(backgroundColor, null);
//                    cell.setColor(color.getARGBHex().substring(2));
//                }
//            }
//
//            document.write(outputStream);
//        } catch (IOException e) {
//            System.err.println("Error al generar la tabla de predicciones en DOCX: " + e.getMessage());
//        }
//    }

    public void generateEvaluationReport(double precision, double recall, double f1, double accuracy, String filePath, String rocImagePath,String rocMacroFilePath, String topNRocFilePath,String AUCFilePath) {
        File rocFile = new File(rocImagePath);
        File AUCFile = new File(AUCFilePath);
        File rocMacroFile = new File(rocMacroFilePath);
        File rocTopNFile = new File(topNRocFilePath);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDImageXObject pdImage = null;
            PDImageXObject pdImage_auc = null;
            PDImageXObject pdImage_rocMacro = null;
            PDImageXObject pdImage_topN = null;
            if(rocFile.exists()){
                pdImage = PDImageXObject.createFromFile(rocImagePath, document);
            }
            if(AUCFile.exists() && Main.R){
                pdImage_auc = PDImageXObject.createFromFile(AUCFilePath,document);
            }
            if(rocMacroFile.exists()){
                pdImage_rocMacro = PDImageXObject.createFromFile(rocMacroFilePath,document);
            }
            if(rocTopNFile.exists()){
                pdImage_topN = PDImageXObject.createFromFile(topNRocFilePath,document);
            }
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float margin = 75;
                float yOffset = page.getMediaBox().getUpperRightY() - margin;
                float leading = 15;
                contentStream.beginText();
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                contentStream.setFont(font, 12);
                contentStream.setTextMatrix(new Matrix(1, 0, 0, 1, 75, yOffset)); // Nueva matriz
                contentStream.showText("--- Reporte de Evaluacion del Modelo ---");
                contentStream.newLineAtOffset(0, -leading);
                yOffset -= leading;
                if(yOffset<=0){
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document,page);
                    yOffset = page.getMediaBox().getUpperRightY()-margin;
                }
                contentStream.showText("Precision: " + String.format("%.4f", precision));
                contentStream.newLineAtOffset(0, -leading);
                yOffset -= leading;
                if(yOffset<=0){
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document,page);
                    yOffset = page.getMediaBox().getUpperRightY()-margin;
                }
                contentStream.showText("Recall: " + String.format("%.4f", recall));
                contentStream.newLineAtOffset(0, -leading);
                yOffset -= leading;
                if(yOffset<=0){
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document,page);
                    yOffset = page.getMediaBox().getUpperRightY()-margin;
                }
                contentStream.showText("F1-Score: " + String.format("%.4f", f1));
                contentStream.newLineAtOffset(0, -leading);
                yOffset -= leading;
                if(yOffset<=0){
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document,page);
                    yOffset = page.getMediaBox().getUpperRightY()-margin;
                }
                contentStream.showText("Exactitud: " + String.format("%.4f", accuracy));
                contentStream.newLineAtOffset(0, -leading);
                yOffset -= leading;

                contentStream.endText();
                // Insertar la imagen ROC si la ruta es válida y el archivo existe
                if (rocFile.exists() && pdImage!=null) {
                    float scale = 0.5f; // Ajusta la escala según sea necesario
                    float imageWidth = pdImage.getWidth() * scale;
                    float imageHeight = pdImage.getHeight() * scale;
                    yOffset -= imageHeight;
                    if(yOffset<=0){
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document,page);
                        yOffset = page.getMediaBox().getHeight()-imageHeight;
                    }
                    contentStream.drawImage(pdImage, page.getMediaBox().getLowerLeftX() + margin, yOffset, imageWidth,imageHeight ); // Ajusta la posición según sea necesario
                }
                if (AUCFile.exists() && pdImage_auc!=null && Main.R) {
                    float scale = 0.5f; // Ajusta la escala según sea necesario
                    float imageWidth = pdImage_auc.getWidth() * scale;
                    float imageHeight = pdImage_auc.getHeight() * scale;
                    yOffset -= imageHeight;
                    if(yOffset<=0){
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document,page);
                        yOffset = page.getMediaBox().getHeight()-imageHeight;
                    }
                    contentStream.drawImage(pdImage_auc, page.getMediaBox().getLowerLeftX() + margin, yOffset, imageWidth,imageHeight ); // Ajusta la posición según sea necesario
                }
                if (rocMacroFile.exists() && pdImage_rocMacro!=null) {
                    float scale = 0.5f; // Ajusta la escala según sea necesario
                    float imageWidth = pdImage_rocMacro.getWidth() * scale;
                    float imageHeight = pdImage_rocMacro.getHeight() * scale;
                    yOffset -= imageHeight;
                    if(yOffset<=0){
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document,page);
                        yOffset = page.getMediaBox().getHeight()-imageHeight;
                    }
                    contentStream.drawImage(pdImage_rocMacro, page.getMediaBox().getLowerLeftX() + margin, yOffset, imageWidth,imageHeight ); // Ajusta la posición según sea necesario
                }
                if (rocTopNFile.exists() && pdImage_topN!=null) {
                    float scale = 0.5f; // Ajusta la escala según sea necesario
                    float imageWidth = pdImage_topN.getWidth() * scale;
                    float imageHeight = pdImage_topN.getHeight() * scale;
                    yOffset -= imageHeight;
                    if(yOffset<=0){
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document,page);
                        yOffset = page.getMediaBox().getHeight()-imageHeight;
                    }
                    contentStream.drawImage(pdImage_topN, page.getMediaBox().getLowerLeftX() + margin, yOffset, imageWidth,imageHeight ); // Ajusta la posición según sea necesario
                }
            } finally {
                contentStream.close();
            }
            document.save(new File(filePath + ".pdf"));
        } catch (IOException e) {
            System.err.println("Error al generar el reporte de evaluación en PDF: " + e.getMessage());
        }

        try (XWPFDocument document = new XWPFDocument()) {
            FileOutputStream outputStream = new FileOutputStream(new File(filePath+".docx"));
            XWPFParagraph title = document.createParagraph();
            XWPFRun runTitle = title.createRun();
            runTitle.setText("--- Reporte de Evaluacion del Modelo ---");
            runTitle.setBold(true);
            runTitle.setFontSize(14);

            XWPFParagraph precisionParagraph = document.createParagraph();
            precisionParagraph.createRun().setText("Precision: " + String.format("%.4f", precision));

            XWPFParagraph recallParagraph = document.createParagraph();
            recallParagraph.createRun().setText("Recall: " + String.format("%.4f", recall));

            XWPFParagraph f1Paragraph = document.createParagraph();
            f1Paragraph.createRun().setText("F1-Score: " + String.format("%.4f", f1));

            XWPFParagraph accuracyParagraph = document.createParagraph();
            accuracyParagraph.createRun().setText("Exactitud: " + String.format("%.4f", accuracy));
            document.createParagraph().createRun().addBreak();

            // Insertar la imagen ROC si la ruta es válida y el archivo existe
            if (rocFile.exists()) {
                try (FileInputStream fis = new FileInputStream(rocFile)) {
                    document.createParagraph().createRun().addPicture(fis, XWPFDocument.PICTURE_TYPE_PNG, "ROC Curve", Units.toEMU(400), Units.toEMU(300)); // Ajusta dimensiones según sea necesario
                } catch (InvalidFormatException e) {
                    System.err.println("Error al insertar la ROC curve en el archivo .docx"+e);
                }
            }
            if (AUCFile.exists() && Main.R) {
                try (FileInputStream fis = new FileInputStream(AUCFile)) {
                    document.createParagraph().createRun().addPicture(fis, XWPFDocument.PICTURE_TYPE_PNG, "AUC Heatmap", Units.toEMU(400), Units.toEMU(300)); // Ajusta dimensiones según sea necesario
                } catch (InvalidFormatException e) {
                    System.err.println("Error al insertar el AUC Heatmap en el archivo .docx"+e);
                }
            }
            if (rocMacroFile.exists()) {
                try (FileInputStream fis = new FileInputStream(rocMacroFile)) {
                    document.createParagraph().createRun().addPicture(fis, XWPFDocument.PICTURE_TYPE_PNG, "AUC Heatmap", Units.toEMU(400), Units.toEMU(300)); // Ajusta dimensiones según sea necesario
                } catch (InvalidFormatException e) {
                    System.err.println("Error al insertar el AUC Heatmap en el archivo .docx"+e);
                }
            }
            if (rocTopNFile.exists()) {
                try (FileInputStream fis = new FileInputStream(rocTopNFile)) {
                    document.createParagraph().createRun().addPicture(fis, XWPFDocument.PICTURE_TYPE_PNG, "AUC Heatmap", Units.toEMU(400), Units.toEMU(300)); // Ajusta dimensiones según sea necesario
                } catch (InvalidFormatException e) {
                    System.err.println("Error al insertar el AUC Heatmap en el archivo .docx"+e);
                }
            }

            document.write(outputStream);
        } catch (IOException e) {
            System.err.println("Error al generar el reporte de evaluación en DOCX: " + e.getMessage());
        }
    }

    private String formatTrainingTime(long trainingTimeMillis) {
        long seconds = (trainingTimeMillis / 1000) % 60;
        long minutes = (trainingTimeMillis / (1000 * 60)) % 60;
        long hours = trainingTimeMillis / (1000 * 60 * 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void drawText(PDPageContentStream contentStream, float x, float y, String text, PDPageContentStream cs) throws IOException {
        cs.setTextMatrix(new Matrix(1, 0, 0, 1, x, y));
        cs.showText(text);
    }

    // Función para interpolar entre dos colores basado en un valor (0 a 1)
    private Color interpolateColor(Color color1, Color color2, float ratio) {
        int r = (int) (color1.getRed() * (1 - ratio) + color2.getRed() * ratio);
        int g = (int) (color1.getGreen() * (1 - ratio) + color2.getGreen() * ratio);
        int b = (int) (color1.getBlue() * (1 - ratio) + color2.getBlue() * ratio);
        return new Color(r, g, b);
    }
}
