package com.example.demo.Controller;


import com.example.demo.Service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/legal-chat")
public class ChatGPTController {
    @Autowired
    private  OpenAIService openAIService;

    @PostMapping("/ask")
    public String askQuestion(@RequestBody String question) {
        try {
            // Gửi câu hỏi từ người dùng đến ChatGPT service
            return openAIService.generateAnswer(question);
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            return "Đã xảy ra lỗi khi lấy câu trả lời từ OpenAI.";
        }
    }
}
