package com.example.CodeAnalyzr.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class HuggingFaceRequest {
    public HuggingFaceRequest(String model, List<Message> messages, int max_tokens, boolean stream) {
        this.model = model;
        this.messages = messages;
        this.max_tokens = max_tokens;
        this.stream = stream;
    }
    private String model;
    private List<Message> messages;
    private int max_tokens;
    private boolean stream;

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }


    }
}