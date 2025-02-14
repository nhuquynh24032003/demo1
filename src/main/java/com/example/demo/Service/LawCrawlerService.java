package com.example.demo.Service;
import com.example.demo.Model.LegalDocument;
import com.example.demo.Model.LegalDocumentDetail;
import com.example.demo.Repository.LegalDocumentDetailRepository;
import com.example.demo.Repository.LegalDocumentRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LawCrawlerService {
    @Autowired
    LegalDocumentRepository legalDocumentRepository;
    @Autowired
    LegalDocumentDetailRepository legalDocumentDetailRepository;
    private static final String BASE_URL = "https://luatvietnam.vn/tim-van-ban.html?Keywords=&SearchOptions=1&SearchByDate=issueDate&DateFrom=&DateTo=";

    public void crawlLawList() {
        try {
            Set<String> existingUrls = new HashSet<>();
           // legalDocumentRepository.findAll().forEach(doc -> existingUrls.add(doc.getDetailUrl()));

            List<LegalDocument> newDocuments = new ArrayList<>();

            for (int page = 1; page <= 15; page++) {
                String url = BASE_URL + "&page=" + page;
                Document doc = Jsoup.connect(url).get();
                Elements articles = doc.select(".doc-article");

                for (Element article : articles) {
                    String title = article.select(".doc-title a").text();
                    String detailUrl = "https://luatvietnam.vn" + article.select(".doc-title a").attr("href");

                    Element dateElement = article.select(".w-doc-dmy2").first();
                    String issueDate = (dateElement != null) ? dateElement.text() : "N/A";

                    if (!existingUrls.contains(detailUrl)) {
                        LegalDocument legalDoc = new LegalDocument();
                        legalDoc.setTitle(title);
                        legalDoc.setDetailUrl(detailUrl);
                        legalDoc.setIssueDate(issueDate);

                        newDocuments.add(legalDoc);
                        existingUrls.add(detailUrl); // Thêm vào danh sách kiểm tra
                        crawlAndSaveDetail(legalDoc);
                    }
                }
            }

            if (!newDocuments.isEmpty()) {
                legalDocumentRepository.saveAll(newDocuments);
              //  System.out.println("Đã lưu " + newDocuments.size() + " văn bản mới.");
            } else {
                System.out.println("Không có văn bản mới.");
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi crawl dữ liệu: " + e.getMessage());
        }
    }
    public void crawlLawDetails() {
        List<LegalDocument> documents = legalDocumentRepository.findAll();
        int savedCount = 0;

        for (LegalDocument doc : documents) {
            if (!legalDocumentDetailRepository.existsByDetailUrl(doc.getDetailUrl())) {
                try {
                    Document detailPage = Jsoup.connect(doc.getDetailUrl()).get();
                    Element contentElement = detailPage.select("div.the-document-body.noidungtracuu").first(); // Cần kiểm tra selector đúng không
                    String content = (contentElement != null) ? contentElement.text() : "Không tìm thấy nội dung";

                    LegalDocumentDetail detail = new LegalDocumentDetail();
                    detail.setDetailUrl(doc.getDetailUrl());
                    detail.setContent(content);

                    legalDocumentDetailRepository.save(detail);
                    savedCount++;
                    System.out.println("Đã lưu chi tiết: " + doc.getDetailUrl());
                } catch (IOException e) {
                    System.err.println("Lỗi khi lấy nội dung: " + doc.getDetailUrl() + " - " + e.getMessage());
                }
            }
        }

        System.out.println("Hoàn thành lưu chi tiết " + savedCount + " văn bản.");
    }
    private void crawlAndSaveDetail(LegalDocument doc) {
        try {
            if (!legalDocumentDetailRepository.existsByDetailUrl(doc.getDetailUrl())) {
                Document detailPage = Jsoup.connect(doc.getDetailUrl()).get();
                Element contentElement = detailPage.select("div.the-document-body.noidungtracuu").first();
                String content = (contentElement != null) ? contentElement.text() : "Không tìm thấy nội dung";
                String issuingAgency = extractTableText(detailPage, "Cơ quan ban hành:");
                String officialGazetteNumber = extractTableText(detailPage, "Số công báo:");
                String documentNumber = extractTableText(detailPage, "Số hiệu:");
                String publicationDate = extractTableText(detailPage, "Ngày đăng công báo:");
                String documentType = extractTableText(detailPage, "Loại văn bản:");
                String signer = extractTableText(detailPage, "Người ký:");
                String issuedDate = extractTableText(detailPage, "Ngày ban hành:");
                String effectiveDate = extractTableText(detailPage, "Ngày hết hiệu lực:");
                String fields = extractFields(detailPage);
                String pdfUrl = extractPdfUrl(detailPage);


                LegalDocumentDetail detail = new LegalDocumentDetail();
                detail.setPdfUrl(pdfUrl);
                detail.setDetailUrl(doc.getDetailUrl());
                detail.setContent(content);
                detail.setIssuingAgency(issuingAgency);
                detail.setOfficialGazetteNumber(officialGazetteNumber);
                detail.setDocumentNumber(documentNumber);
                detail.setPublicationDate(publicationDate);
                detail.setDocumentType(documentType);
                detail.setSigner(signer);
                detail.setIssuedDate(parseDate(issuedDate)); // Chuyển đổi sang LocalDate
                detail.setEffectiveDate(effectiveDate);
                detail.setFields(fields);

                legalDocumentDetailRepository.save(detail);
              //  System.out.println("Đã lưu chi tiết: " + doc.getDetailUrl());
            }
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

}
