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
    private static final String OPENAI_API_KEY = "";
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
                            "content", "Bạn là một trợ lý pháp lý. Khi tôi đưa ra một câu hỏi, trích xuất nội dung chính của câu hỏi đó, lấy những keyword và tóm tắt nội dung chính xác để tìm kiếm embedding. Câu hỏi: " + userInput
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
