package com.example.CodeAnalyzr.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
public class CodeAnalysisService {

    @Autowired
    private ResumeService resumeService;
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.fulluri}")
    String geminiApiFullUrl;
    @Value("api.key")
    private String apiKey;

    private final WebClient webClient;

    public CodeAnalysisService(WebClient.Builder webClient) {
        this.webClient = webClient
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(10))))
                .build();
    }


//    private String quest = "As a professional developer review this code and suggest some points to improve the code quality only give me important and required points and be specific for each modification keep it concise. import java.util.HashMap;\nimport java.util.ArrayList;\n\npublic class Test {\n\n    // Simple method\n    public void simpleMethod() {\n        System.out.println(\"This is a simple method.\");\n    }\n\n    public void longMethod() {\n        int sum = 0;\n        for (int i = 0; i < 10; i++) { \n            for (int j = 0; j < 10; j++) {\n                sum += i * j;\n            }\n        }\n        System.out.println(\"Sum is: \" + sum);\n    }\n\n    // Method with nested loops\n    public void nestedLoops() {\n        for (int i = 0; i < 5; i++) {\n            for (int j = 0; j < 5; j++) {\n                for (int k = 0; k < 5; k++) {\n                    System.out.println(\"Nested loop: \" + i + \", \" + j + \", \" + k);\n                }\n            }\n        }\n    }\n\n    // A method with potential refactoring opportunities\n    public void repetitiveCode() {\n        int a = 10;\n        int b = 20;\n        int c = 30;\n\n        System.out.println(\"Value of a: \" + a);\n        System.out.println(\"Value of b: \" + b);\n        System.out.println(\"Value of c: \" + c);\n    }\n}";
    public String getAnswer(String question) throws Exception {
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
            throw new RuntimeException("Error while calling API: " + ex.getMessage(), ex);
        }
    }

    public String convertCode(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file name: Unable to detect file type.");
        }
        String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);

        String code = new String(file.getBytes(), StandardCharsets.UTF_8);

        String escapedCode = code.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");

        // Step 4: Format the question
        String question = String.format(
                "As a professional developer, review this %s code and suggest some points to improve the code quality answer concisely and give me only important points which are required to be made properly format your response. %s",
                fileType.toUpperCase(),
                escapedCode
        );

        return String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                question
        );
    }
    public String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IOException("Invalid file");
        }

        if (fileName.endsWith(".pdf")) {
            return extractTextFromPDF(file);
        } else if (fileName.endsWith(".docx")) {
            return extractTextFromDOCX(file);
        } else {
            throw new IOException("Unsupported file type. Only PDF and DOCX are allowed.");
        }
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDOCX(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {
            StringBuilder text = new StringBuilder();
            document.getParagraphs().forEach(p -> text.append(p.getText()).append("\n"));
            return text.toString();
        }
    }
    public String getResumeAnalysys(MultipartFile resume) throws IOException {
        // Extract text from the resume file
        String extractedText = extractText(resume);
        System.out.println("Extracted Text: " + extractedText);

        // Get summary from DeepSeek
        String summary = getResumeSummary(extractedText);
        System.out.println("Summary from DeepSeek: " + summary);

        // Convert summary into request object for Gemini
        Map<String, Object> reqBody = getStringObjectMap(summary);

        // Make API Call to Gemini
        try {
            String res = webClient.post()
                    .uri(geminiApiFullUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(reqBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return res;
        } catch (Exception ex) {
            throw new RuntimeException("Error while calling Gemini API: " + ex.getMessage(), ex);
        }
    }
    private String getResumeSummary(String extractedText){
        return resumeService.getChatResponse(extractedText);
    }
    private static Map<String, Object> getStringObjectMap(String extractedText) {
        String prompt = "Analyze the following resume:\n\n" + extractedText +
                "\n\nProvide the following insights:\n" +
                "- **ATS Score** (0-100)\n" +
                "- **Suggestions for improvement** (layout, content, keyword optimization, etc.)\n" +
                "- **Grammar and spelling mistakes** (highlight errors and correct them)\n" +
                "- **Ways to make it stand out from other resumes**\n" +
                "Format your response clearly and concisely.";

        // Construct the request payload
        Map<String, Object> reqBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );
        System.out.println("reqBody = " + prompt);
        return reqBody;
    }
}
