package com.example.CodeAnalyzr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

@Service
public class CodeAnalysisService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    public CodeAnalysisService(WebClient.Builder webClient) {
        this.webClient = webClient
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(10))))
                .build();
    }


//    private String quest = "As a professional developer review this code and suggest some points to improve the code quality only give me important and required points and be specific for each modification keep it concise. import java.util.HashMap;\nimport java.util.ArrayList;\n\npublic class Test {\n\n    // Simple method\n    public void simpleMethod() {\n        System.out.println(\"This is a simple method.\");\n    }\n\n    public void longMethod() {\n        int sum = 0;\n        for (int i = 0; i < 10; i++) { \n            for (int j = 0; j < 10; j++) {\n                sum += i * j;\n            }\n        }\n        System.out.println(\"Sum is: \" + sum);\n    }\n\n    // Method with nested loops\n    public void nestedLoops() {\n        for (int i = 0; i < 5; i++) {\n            for (int j = 0; j < 5; j++) {\n                for (int k = 0; k < 5; k++) {\n                    System.out.println(\"Nested loop: \" + i + \", \" + j + \", \" + k);\n                }\n            }\n        }\n    }\n\n    // A method with potential refactoring opportunities\n    public void repetitiveCode() {\n        int a = 10;\n        int b = 20;\n        int c = 30;\n\n        System.out.println(\"Value of a: \" + a);\n        System.out.println(\"Value of b: \" + b);\n        System.out.println(\"Value of c: \" + c);\n    }\n}";
    public String getAnswer(String question) {
        // Construct the request payload
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", question)
                        } )
                }
        );

        // Make API Call
        try {
            String response = webClient.post()
                    .uri(geminiApiUrl + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response;
        } catch (Exception ex) {
            throw new RuntimeException("Error while calling Gemini API: " + ex.getMessage(), ex);
        }
    }

    public String convertCode(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
//        List<String> supportedTypes = List.of("java", "py", "js", "cpp", "cs");
//        if (!supportedTypes.contains(fileName.toLowerCase())) {
//            throw new IllegalArgumentException("Unsupported file type: " + fileName);
//        }
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file name: Unable to detect file type.");
        }
        String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);

        // Step 2: Read the file content
        String code = new String(file.getBytes(), StandardCharsets.UTF_8);

        // Step 3: Escape the code content for JSON
        String escapedCode = code.replace("\\", "\\\\")  // Escape backslashes
                .replace("\"", "\\\"") // Escape double quotes
                .replace("\n", "\\n") // Escape newlines
                .replace("\r", "");   // Remove carriage returns

        // Step 4: Format the question
        String question = String.format(
                "As a professional developer, review this %s code and suggest some points to improve the code quality answer concisely and give me only important points which are required to be made. %s",
                fileType.toUpperCase(),
                escapedCode
        );

        return String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                question
        );
    }
}
