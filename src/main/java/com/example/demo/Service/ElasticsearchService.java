package com.example.demo.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.demo.Service.OllamaEmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private OllamaEmbeddingService ollamaEmbeddingService;
    @Autowired
    OpenAIService openAIService;

    // Hàm thực hiện tìm kiếm KNN
    public List<String> searchKnn(String knnRequest) throws IOException {
        // Tạo truy vấn KNN
        List<Double> queryEmbedding = ollamaEmbeddingService.getEmbedding(knnRequest);
        List<Float> queryEmbeddingFloat = queryEmbedding.stream()
                .map(Double::floatValue)
                .toList();

        KnnSearch knnSearch = KnnSearch.of(q -> q
                .field("embedding") // Trường chứa vector embedding
                .k(10)
                .numCandidates(100)// Số lượng kết quả gần nhất
                .queryVector(queryEmbeddingFloat) // Vector truy vấn
        );
        Query fulltextQuery = Query.of(q -> q
                .match(m -> m
                        .field("content")
                        .query(knnRequest)
                )
        );
        SearchRequest request = new SearchRequest.Builder()
                .index("document_embedding") // Chỉ mục Elasticsearch
                .knn(knnSearch)
                .query(fulltextQuery)// Truy vấn KNN
                .sort(s -> s
                        .field(f -> f
                                .field("_score") // Sắp xếp theo độ tương đồng (score)
                        )
                )
                .build();


        // Gửi yêu cầu và nhận kết quả tìm kiếm
        SearchResponse<Map> response = client.search(request, Map.class);
        response.hits().hits().forEach(hit -> {
            System.out.println("Hit source: " + hit.source());
            System.out.println("Hit score: " + hit.score());
        });
        System.out.println("Raw Response: " + response.toString());
        // Trả về kết quả
        List<String> results = new ArrayList<>();
        for (Hit<Map> hit : response.hits().hits()) {
            Map source = hit.source();
            if (source != null && source.containsKey("chunkText")) {
                Object content = source.get("chunkText");
                if (content instanceof String) {
                    results.add((String) content);
                } else {
                    System.out.println("⚠️ chunkText không phải String: " + content.getClass());
                }
            } else {
                System.out.println("❌ Không tìm thấy 'chunkText' trong kết quả: " + source);
            }

        }


        return results;
    }
    public String searchLegalDocuments(String userQuery) throws IOException {
        // 🔍 1️⃣ Tìm kiếm các chunk liên quan từ Elasticsearch
        List<String> chunks = searchKnn(userQuery);
        System.out.println(chunks);
        if (chunks.isEmpty()) {
            return "Không tìm thấy văn bản nào phù hợp với câu hỏi của bạn.";
        }

        if (chunks.isEmpty()) {
            return "Không tìm thấy văn bản nào phù hợp với câu hỏi của bạn.";
        }
        // 🏆 2️⃣ Rerank các chunk bằng GPT
        String relevantText = openAIService.rerankChunks(userQuery, chunks);

        // 📝 3️⃣ Tóm tắt thông tin quan trọng
        //String summary = openAIService.summarizeChunks(userQuery, List.of(relevantText));

        return relevantText;
    }
}