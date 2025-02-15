package com.example.demo.Controller;

import com.example.demo.Model.LegalDocumentDetail;
import com.example.demo.Repository.DocumentDetailEmbeddingRepository;
import com.example.demo.Repository.LegalDocumentDetailRepository;
import com.example.demo.Service.DocumentEmbeddingService;
import com.example.demo.Service.OllamaEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/embeddings")
public class DocumentEmbeddingController {
    @Autowired
    private DocumentEmbeddingService documentEmbeddingService;
    @Autowired
    private LegalDocumentDetailRepository legalDocumentDetailRepository;
    @PostMapping("/process/all")
    public ResponseEntity<String> processEmbeddingForAll() {
        try {
            List<LegalDocumentDetail> allDocuments = legalDocumentDetailRepository.findAll();
            System.out.println(allDocuments);
            for (LegalDocumentDetail document : allDocuments) {
                documentEmbeddingService.processAndSaveEmbeddings(document.getId());
            }
            return ResponseEntity.ok("Embedding cho tất cả tài liệu đã được xử lý thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi xử lý embedding: " + e.getMessage());
        }
    }
}
