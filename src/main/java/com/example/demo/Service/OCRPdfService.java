package com.example.demo.Service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

@Service
public class OCRPdfService {
    private static final String TESSERACT_PATH = "C:/Program Files/Tesseract-OCR"; // Windows
    // private static final String TESSERACT_PATH = "/usr/share/tesseract-ocr/"; // Linux

    public String extractTextFromPdfUrl(String pdfUrl) {
        try {
            // Tải file PDF từ URL về máy
            File tempPdf = downloadFileFromUrl(pdfUrl);

            // Xử lý OCR trên file PDF đã tải về
            String extractedText = extractTextFromScannedPDF(tempPdf);

            // Xóa file PDF tạm
            tempPdf.delete();

            return extractedText;
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Lỗi khi trích xuất văn bản từ PDF: " + e.getMessage();
        }
    }

    private File downloadFileFromUrl(String pdfUrl) throws IOException {
        File tempFile = File.createTempFile("downloaded", ".pdf");
        try (InputStream in = new URL(pdfUrl).openStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    public String extractTextFromScannedPDF(File pdfFile) {
        try {
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            StringBuilder extractedText = new StringBuilder();

            // Cấu hình Tesseract OCR
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSERACT_PATH + "/tessdata");
            tesseract.setLanguage("vie"); // Sử dụng tiếng Việt

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                // Lưu ảnh tạm thời
                File tempImage = File.createTempFile("temp_page_", ".png");
                ImageIO.write(bufferedImage, "png", tempImage);

                // OCR trên ảnh
                String ocrResult = tesseract.doOCR(tempImage);
                extractedText.append(ocrResult).append("\n");

                // Xóa ảnh tạm
                tempImage.delete();
            }

            document.close();
            return extractedText.toString().trim();
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "❌ Lỗi khi OCR file PDF: " + e.getMessage();
        }
    }
}
