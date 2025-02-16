package com.example.demo.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {
   
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    public String rerankChunks(String userInput, List<String> chunks) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String chunkText = String.join("\n\n", chunks); // Gộp các chunk lại thành một chuỗi
            // Tạo messages
            List<Map<String, String>> messages = List.of(
                    Map.of(
                            "role", "user",
                            "content", "Bạn là một trợ lý pháp lý. Tôi có câu hỏi sau:" +  userInput +
                                    "Dưới đây là các đoạn văn bản có thể liên quan: " + chunkText +
                                    "Hãy sắp xếp mức độ liên quan với câu hỏi, sau đó loại bỏ những không liên quan sau đó Hãy tổng hợp câu trả lời dễ hiểu nhất cho người dùng (chỉ cung cấp nội dung trả lời)"
                    )
            );

            // Tạo payload
            Map<String, Object> payload = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", messages
            );

            // Chuyển sang JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);

            return callOpenAI(jsonPayload);
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
    public String generateAnswer(String userInput) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Tạo messages
            List<Map<String, String>> messages = List.of(
                    Map.of(
                            "role", "user",
                            "content", "Trích xuất các từ khóa quan trọng về nội dung để tìm kiếm luật từ đoạn sau và trả về dưới dạng danh sách JSON (chỉ json):\\n\\n" + userInput
                    )
            );

            // Tạo payload
            Map<String, Object> payload = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", messages
            );

            // Chuyển sang JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);

            return callOpenAI(jsonPayload);
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String callOpenAI(String jsonPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(OPENAI_API_KEY); // Dùng biến môi trường

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "Lỗi: API Key không hợp lệ hoặc hết hạn.";
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            return "Lỗi API: " + e.getMessage();
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }


}
