package com.blog.service;

import com.blog.dto.AIChatRequest;
import com.blog.entity.AiConversation;
import com.blog.mapper.AiConversationMapper;
import com.blog.service.ai.ArticleContextBuilder;
import com.blog.service.ai.BlogAiAssistant;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final BlogAiAssistant assistant;
    private final ArticleContextBuilder contextBuilder;
    private final AiConversationMapper aiConversationMapper;

    @Value("${ai.model}")
    private String aiModel;

    public SseEmitter chat(AIChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);

        String sessionId = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        // 构建 RAG 上下文
        String context = contextBuilder.buildContext(request.getQuestion());

        // 把文章上下文拼到用户消息前面
        String enrichedMessage;
        if (context != null && !context.isEmpty()) {
            enrichedMessage = "以下是与用户问题相关的博客文章内容：\n\n"
                    + context
                    + "\n\n用户问题：" + request.getQuestion();
        } else {
            enrichedMessage = request.getQuestion();
        }

        TokenStream tokenStream = assistant.chat(sessionId, enrichedMessage);
        StringBuilder answer = new StringBuilder();

        tokenStream
                .onPartialResponse(chunk -> {
                    answer.append(chunk);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> {
                    try {
                        AiConversation record = new AiConversation();
                        record.setSessionId(sessionId);
                        record.setQuestion(request.getQuestion());
                        record.setAnswer(answer.toString());
                        record.setModel(aiModel);
                        if (response.tokenUsage() != null) {
                            record.setTokensUsed(response.tokenUsage().totalTokenCount());
                        }
                        aiConversationMapper.insert(record);
                    } catch (Exception e) {
                        log.error("AI conversation persist failed for session {}", sessionId, e);
                    }
                    try {
                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data("{\"sessionId\":\"" + sessionId + "\"}"));
                    } catch (IOException e) {
                        // ignore
                    }
                    emitter.complete();
                })
                .onError(error -> {
                    log.error("AI chat error for session {}", sessionId, error);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data("抱歉，AI服务暂时不可用，请稍后再试。"));
                    } catch (IOException e) {
                        // ignore
                    }
                    emitter.complete();
                })
                .start();

        return emitter;
    }

    public String[] getSuggestions() {
        return new String[]{
                "最近发布了哪些文章？",
                "介绍一下这个博客",
                "有哪些技术分类的文章？",
                "如何联系站长？"
        };
    }
}
