package com.example.demo.Controller;

import com.example.demo.Service.ElasticsearchService;
import com.example.demo.Service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Object> searchKnn(@RequestBody String knnRequest) throws IOException {
        String k = openAIService.generateAnswer(knnRequest);
        System.out.println(k);
        return elasticsearchService.searchKnn(openAIService.generateAnswer(knnRequest));
    }


}
