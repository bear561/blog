package com.blog.controller;

import com.blog.dto.AIChatRequest;
import com.blog.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final AiChatService aiChatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AIChatRequest request) {
        return aiChatService.chat(request);
    }

    @GetMapping("/suggestions")
    public String[] getSuggestions() {
        return aiChatService.getSuggestions();
    }
}
