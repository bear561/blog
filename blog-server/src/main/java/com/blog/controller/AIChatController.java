package com.blog.controller;

import com.blog.dto.AIChatRequest;
import com.blog.service.ai.AIService;
import com.blog.service.ai.ArticleContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final AIService aiService;
    private final ArticleContextBuilder contextBuilder;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AIChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        String sessionId = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        // 构建上下文
        String context = contextBuilder.buildContext(request.getQuestion());

        // 流式返回
        StringBuilder fullAnswer = new StringBuilder();
        aiService.chatStream(request, context)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(chunk));
                                fullAnswer.append(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("AI chat error", error);
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("抱歉，AI服务暂时不可用，请稍后再试。"));
                            } catch (IOException e) {
                                // ignore
                            }
                            emitter.complete();
                        },
                        () -> {
                            try {
                                // 发送完成事件，包含 sessionId
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("{\"sessionId\":\"" + sessionId + "\"}"));
                            } catch (IOException e) {
                                // ignore
                            }
                            emitter.complete();
                        }
                );

        return emitter;
    }

    @GetMapping("/suggestions")
    public String[] getSuggestions() {
        return new String[]{
                "最近发布了哪些文章？",
                "介绍一下这个博客",
                "有哪些技术分类的文章？",
                "如何联系站长？"
        };
    }
}
