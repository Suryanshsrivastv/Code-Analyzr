package com.example.CodeAnalyzr.service;

import com.example.CodeAnalyzr.Models.HuggingFaceRequest;
import com.example.CodeAnalyzr.Models.HuggingFaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class ResumeService {
    @Value("${huggingface.api.key")
    private String apiKey;
    private final String url = "https://huggingface.co/api/inference-proxy/together/v1/chat/completions";

    @Autowired
    private RestTemplate callapi;

    public ResumeService(RestTemplate restTemplate) {
        this.callapi = restTemplate;
    }

    public String getChatResponse(String userMessage) {
        // Create request payload
        String query = "just simply reduce the total words size and reduce it to 120 words,make sure not to miss any point and all information is important,properly structure your response and limit it under 1000 tokens without truncating any information";
        HuggingFaceRequest.Message message = new HuggingFaceRequest.Message("user", userMessage + query);
        HuggingFaceRequest request = new HuggingFaceRequest("deepseek-ai/DeepSeek-V3",
                Collections.singletonList(message), 500, false);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);

        HttpEntity<HuggingFaceRequest> entity = new HttpEntity<>(request, headers);

        // Make API call
        ResponseEntity<HuggingFaceResponse> response = callapi.exchange(
                url, HttpMethod.POST, entity, HuggingFaceResponse.class);

        if (response.getBody() != null && response.getBody().getChoices() != null) {
            return response.getBody().getChoices().get(0).getMessage().getContent();
        }

        return "No response from API";
    }
}
