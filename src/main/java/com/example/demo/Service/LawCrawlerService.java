package com.example.demo.Service;
import com.example.demo.Model.LegalDocument;
import com.example.demo.Model.LegalDocumentDetail;
import com.example.demo.Repository.LegalDocumentDetailRepository;
import com.example.demo.Repository.LegalDocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class LawCrawlerService {
    @Autowired
    LegalDocumentRepository legalDocumentRepository;
    @Autowired
    LegalDocumentDetailRepository legalDocumentDetailRepository;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int TIMEOUT = 15000;
    private static final String BASE_URL = "https://luatvietnam.vn/tim-van-ban.html?Keywords=&SearchOptions=1&SearchByDate=issueDate&DateFrom=&DateTo=";
    @Autowired
    private OCRPdfService ocrPdfService;
    public void crawlLawList() {
        try {
            Set<String> existingUrls = new HashSet<>();
           // legalDocumentRepository.findAll().forEach(doc -> existingUrls.add(doc.getDetailUrl()));

            List<LegalDocument> newDocuments = new ArrayList<>();

            for (int page = 1; page <= 10; page++) {
                String url = BASE_URL + "&page=" + page;
                Document doc = Jsoup.connect(url).get();
                Elements articles = doc.select(".doc-article");

                for (Element article : articles) {
                    String title = article.select(".doc-title a").text();
                    String detailUrl = "https://luatvietnam.vn" + article.select(".doc-title a").attr("href");

                    Element dateElement = article.select(".w-doc-dmy2").first();
                    String issueDate = (dateElement != null) ? dateElement.text() : "N/A";

                    if (!existingUrls.contains(detailUrl)) {
                        newDocuments.add(new LegalDocument(title, detailUrl, issueDate));
                        existingUrls.add(detailUrl);
                    }
                }
            }

            if (!newDocuments.isEmpty()) {
                legalDocumentRepository.saveAll(newDocuments);
                System.out.println("Đã lưu " + newDocuments.size() + " văn bản mới.");
                crawlLawDetails();
            } else {
                System.out.println("Không có văn bản mới.");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi crawl dữ liệu: " + e.getMessage());
        }
    }
    public void crawlLawDetails() {
        List<LegalDocument> documents = legalDocumentRepository.findAll()
                .stream()
                .filter(doc -> !legalDocumentDetailRepository.existsByDetailUrl(doc.getDetailUrl()))
                .collect(Collectors.toList());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        for (LegalDocument doc : documents) {
            executor.execute(() -> processDocumentDetail(doc));
        }

        executor.shutdown();
        while (!executor.isTerminated()) {}
        System.out.println("✅ Completed document detail crawling.");
    }
    private void processDocumentDetail(LegalDocument doc) {
        if (legalDocumentDetailRepository.existsByDetailUrl(doc.getDetailUrl())) return;

        try {
            Document detailPage = Jsoup.connect(doc.getDetailUrl()).timeout(TIMEOUT).get();
            Element contentElement = detailPage.select("div.the-document-body.noidungtracuu").first();
            Element titleElement = detailPage.selectFirst("h1.the-document-title");
            String title = titleElement != null ? titleElement.text() : "Không tìm thấy tiêu đề";
            String content = (contentElement != null) ? contentElement.text() : "";
            String issuingAgency = extractTableText(detailPage, "Cơ quan ban hành:");
            String officialGazetteNumber = extractTableText(detailPage, "Số công báo:");
            String documentNumber = extractTableText(detailPage, "Số hiệu:");
            String publicationDate = extractTableText(detailPage, "Ngày đăng công báo:");
            String documentType = extractTableText(detailPage, "Loại văn bản:");
            String signer = extractTableText(detailPage, "Người ký:");
            String issuedDate = extractTableText(detailPage, "Ngày ban hành:");
         //   String effectiveDate = extractTableText(detailPage, "Ngày hết hiệu lực:");
            String fields = extractFields(detailPage);
            String pdfUrl = extractPdfUrl(detailPage);
        //    if (content.isEmpty() && pdfUrl != null && !pdfUrl.equals("Không có file PDF")) {
         //       content = PDFExtractor.extractTextFromPDF(pdfUrl);
        //    }
        //    if (content.isEmpty() && pdfUrl != null && !pdfUrl.equals("Không có file PDF")) {
          //      content = ocrPdfService.extractTextFromPdfUrl(pdfUrl);
          //  }
            if (content.isEmpty()) {
                System.out.println("⚠️ Không có nội dung hợp lệ, lưu 'Không có nội dung' cho: " + doc.getDetailUrl());
            }
            LegalDocumentDetail detail = new LegalDocumentDetail(doc.getDetailUrl(),content, issuingAgency, officialGazetteNumber, publicationDate, documentType, signer, title, parseDate(issuedDate), documentNumber, pdfUrl, fields);
            legalDocumentDetailRepository.save(detail);
            System.out.println("✅ Saved details for: " + doc.getDetailUrl());
        } catch (IOException e) {
            System.err.println("Lỗi khi lấy nội dung: " + doc.getDetailUrl() + " - " + e.getMessage());
        }
    }
    private String extractTableText(Document doc, String label) {
        Elements rows = doc.select("table.table-bordered tr");
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() >= 2 && cells.get(0).text().contains(label)) {
                return cells.get(1).text().trim();
            }
        }
        return "Đang cập nhật";
    }
    private String extractFields(Document doc) {
        Elements fieldElements = doc.select("td:has(a.tag-link) a.tag-link");
        List<String> fieldList = new ArrayList<>();
        for (Element field : fieldElements) {
            fieldList.add(field.text().trim());
        }
        return String.join(", ", fieldList);
    }
    private String extractPdfUrl(Document doc) {
        Element embedElement = doc.select("div.embedContent").first();
        if (embedElement != null) {
            return embedElement.attr("data-url"); // Hoặc "data-download" nếu muốn link tải về
        }
        return "Không có file PDF";
    }
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null; // Trả về null nếu không thể parse ngày
        }
    }
    public class PDFExtractor {
        public static String extractTextFromPDF(String pdfUrl) {
            String tempFilePath = "temp.pdf";
            File tempFile = new File(tempFilePath);

            try {
                System.out.println("📥 Đang tải PDF từ: " + pdfUrl);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                try (InputStream in = new URL(pdfUrl).openStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                byte[] pdfBytes = outputStream.toByteArray();
                if (pdfBytes.length == 0) {
                    System.err.println("❌ File PDF rỗng.");
                    return "";
                }

                // Kiểm tra file hợp lệ trước khi xử lý
                if (!tempFile.exists() || tempFile.length() == 0) {
                    System.err.println("❌ Lỗi: File PDF không tồn tại hoặc rỗng.");
                    return "";
                }

                System.out.println("📖 Đang trích xuất nội dung từ PDF...");

                // Trích xuất nội dung PDF
                String extractedText;
                try (PDDocument document = PDDocument.load(tempFile)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    extractedText = stripper.getText(document).trim();
                }

                System.out.println("✅ Trích xuất hoàn tất!" + pdfUrl);

                return extractedText;
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi trích xuất nội dung PDF: " + e.getMessage());
                return "";
            } finally {
                try {
                    // Xóa file tạm sau khi xử lý
                    if (tempFile.exists() && tempFile.delete()) {
                        System.out.println("🗑️ Đã xóa file tạm: " + tempFilePath);
                    } else {
                        System.err.println("⚠️ Không thể xóa file tạm: " + tempFilePath);
                    }
                } catch (Exception ex) {
                    System.err.println("⚠️ Lỗi khi xóa file tạm: " + ex.getMessage());
                }
            }
        }
    }
}
