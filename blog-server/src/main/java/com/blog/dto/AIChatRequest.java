package com.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class AIChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    private String sessionId;

    private List<ChatMessage> history;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
