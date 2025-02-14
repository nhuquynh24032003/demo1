package com.example.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {
    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    /*(public String generateAnswer(String context) {

        // Tạo payload JSON với yêu cầu phân tích và trích xuất nội dung chính
        String payload = "{"
                + "\"model\": \"gpt-3.5-turbo\","
                + "\"messages\": [{\"role\": \"system\", \"content\": \"Bạn là một trợ lý pháp lý và bạn sẽ phân tích các câu hỏi để trích xuất nội dung chính.\"},"
                + "{\"role\": \"user\", \"content\": \"Dưới đây là một số điều luật pháp lý: " + context + ". Hãy phân tích và tóm tắt các nội dung chính.\"}]}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Gọi OpenAI API
            ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, entity, String.class);

            if (response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                // Kiểm tra response và lấy câu trả lời phân tích
                if (jsonNode.has("choices") && jsonNode.get("choices").isArray()) {
                    return jsonNode.get("choices").get(0).get("message").get("content").asText();
                } else {
                    return "Lỗi: Dữ liệu trả về không đúng định dạng.";
                }
            } else {
                return "Lỗi: OpenAI API không trả về dữ liệu.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }*/
    public String generateAnswer(String userInput) {
        // Tạo payload JSON
        String payload = "{"
                + "\"model\": \"gpt-4o-mini\","
                + "\"messages\": ["
               + "{\"role\": \"user\", \"content\": \"Bạn là một trợ lý pháp lý. Khi tôi đưa ra một câu hỏi trích xuất nội dung chính câu hỏi đó lấy những keyword" + userInput + "\"}"
                + "]}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, entity, String.class);

            if (response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                if (jsonNode.has("choices") && jsonNode.get("choices").isArray()) {
                    return jsonNode.get("choices").get(0).get("message").get("content").asText();
                } else {
                    return "Lỗi: Dữ liệu trả về không đúng định dạng.";
                }
            } else {
                return "Lỗi: OpenAI API không trả về dữ liệu.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
}
