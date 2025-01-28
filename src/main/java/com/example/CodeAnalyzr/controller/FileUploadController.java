package com.example.CodeAnalyzr.controller;

import com.example.CodeAnalyzr.service.CodeAnalysisService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/files")
@CrossOrigin
public class FileUploadController {
    private CodeAnalysisService qnAService;

    @GetMapping("/home")
    public String hello(){
        return "hello guuys the api is working";
    }
    @PostMapping("/upload")
    public ResponseEntity<String> askQuestion(@RequestParam("file") MultipartFile file) throws IOException {
//        String question = payload.get("question");
        String answer = qnAService.getAnswer(qnAService.convertCode(file));
        return ResponseEntity.ok(answer);
    }

}
