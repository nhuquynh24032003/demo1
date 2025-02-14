package com.example.demo.Service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class OpenAIService {
    private static final String OPENAI_API_KEY = "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";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/completions";

    public String generateAnswer(String context) throws Exception {
        JSONObject json = new JSONObject();
        json.put("model", "text-davinci-003");
        json.put("prompt", "Dưới đây là một số điều luật pháp lý: " + context + ". Hãy giải thích và trả lời câu hỏi cho người dùng.");
        json.put("max_tokens", 500);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(OPENAI_API_URL);
            post.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);
            post.setEntity(new StringEntity(json.toString()));

            org.apache.http.HttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            // Parse the response from OpenAI API
            JSONObject responseJson = new JSONObject(responseString);
            return responseJson.getJSONArray("choices").getJSONObject(0).getString("text").trim();
        }
    }
}
