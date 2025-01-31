package com.example.CodeAnalyzr.controller;

import com.example.CodeAnalyzr.service.CodeAnalysisService;
import com.example.CodeAnalyzr.service.ResumeService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/files")
@CrossOrigin
public class FileUploadController {
    private CodeAnalysisService qnAService;
    @Autowired
    private ResumeService ResumeService;

    @GetMapping("/home")
    public String hello(){
        return "hello guuys the api is working";
    }
    @PostMapping("/upload")
    public ResponseEntity<String> askQuestion(@RequestParam("file") MultipartFile file) throws Exception {
        String answer = qnAService.getAnswer(qnAService.convertCode(file));
        return ResponseEntity.ok(answer);
    }

    @PostMapping("/uploadresume")
    private ResponseEntity<String> uploadResume(@RequestParam("resume") MultipartFile resume) throws IOException {
        String res = qnAService.getResumeAnalysys(resume);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/deepseek")
    public String askModel(@RequestParam String message) {
        return ResumeService.getChatResponse(message);
    }
}
