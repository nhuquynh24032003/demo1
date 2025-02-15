package com.example.demo.Controller;

import com.example.demo.Service.ElasticsearchService;
import com.example.demo.Service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/knn")
public class KnnSearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private OpenAIService openAIService;
    // API để tìm kiếm KNN với vector truy vấn
    @PostMapping("/search")
    public List<String> searchKnn(@RequestBody String knnRequest) throws IOException {
        String k = openAIService.generateAnswer(knnRequest);
        System.out.println(k);
        return elasticsearchService.searchKnn(openAIService.generateAnswer(knnRequest));
    }
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
}
