package com.example.demo.Service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaEmbeddingService {
    @Value("${ollama.api.url}")
    private String ollamaApiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ObjectMapper objectMapper;

    public List<Double> getEmbedding(String text) {
        try {
            // API URL đúng
            String url = ollamaApiUrl + "/api/embeddings";

            // Sử dụng ObjectMapper để tránh lỗi JSON
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("model", "nomic-embed-text");
            payloadMap.put("prompt", text);

            String payload = objectMapper.writeValueAsString(payloadMap); // Chuyển Map thành JSON

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return parseEmbedding(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private List<Double> parseEmbedding(String responseBody) {
        List<Double> embedding = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode embeddingNode = root.path("embedding");

            for (JsonNode value : embeddingNode) {
                embedding.add(value.asDouble());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return embedding;
    }


}
