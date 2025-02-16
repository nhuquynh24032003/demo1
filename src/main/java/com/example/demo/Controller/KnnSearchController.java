package com.example.demo.Controller;

import com.example.demo.Service.ElasticsearchService;
import com.example.demo.Service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knn")
public class KnnSearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private OpenAIService openAIService;
    // API để tìm kiếm KNN với vector truy vấn

    @GetMapping("/search2")
    public ResponseEntity<String> searchLegalDocuments(@RequestParam String userQuery) {
        try {
            String k = openAIService.generateAnswer(userQuery);
            System.out.println(k);
            String result = elasticsearchService.searchLegalDocuments(userQuery);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @GetMapping("/")
    public List<Map<String, Object>> search(@RequestParam String query) {
        try {
            return elasticsearchService.searchDocuments(query);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tìm kiếm", e);
        }
    }
    @GetMapping("/filter")
    public List<Map<String, Object>> filterDocuments(
            @RequestParam(required = false) String issueFrom,
            @RequestParam(required = false) String issueTo,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String issuingAgency,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String signer
    ) throws IOException {
        return elasticsearchService.filterDocuments(issueFrom, issueTo, documentType, issuingAgency, field, signer);
    }
}
