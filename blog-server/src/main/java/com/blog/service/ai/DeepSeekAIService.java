package com.blog.service.ai;

import com.blog.common.AppConfig;
import com.blog.dto.AIChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "deepseek")
public class DeepSeekAIService implements AIService {

    private final AppConfig appConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public DeepSeekAIService(AppConfig appConfig, ObjectMapper objectMapper) {
        this.appConfig = appConfig;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.deepseek.com")
                .defaultHeader("Authorization", "Bearer " + appConfig.getAi().getApiKey())
                .build();
    }

    @Override
    public Flux<String> chatStream(AIChatRequest request, String context) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt(context));
        messages.add(systemMsg);

        // History
        if (request.getHistory() != null) {
            for (AIChatRequest.ChatMessage msg : request.getHistory()) {
                Map<String, String> m = new HashMap<>();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent());
                messages.add(m);
            }
        }

        // Current question
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getQuestion());
        messages.add(userMsg);

        Map<String, Object> body = new HashMap<>();
        body.put("model", appConfig.getAi().getModel());
        body.put("messages", messages);
        body.put("stream", true);
        body.put("max_tokens", appConfig.getAi().getMaxTokens());

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: ") && !line.equals("data: [DONE]"))
                .map(line -> {
                    try {
                        String json = line.substring(6).trim();
                        if (json.isEmpty()) return "";
                        JsonNode node = objectMapper.readTree(json);
                        JsonNode choices = node.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                return delta.get("content").asText();
                            }
                        }
                        return "";
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(s -> !s.isEmpty());
    }

    private String buildSystemPrompt(String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是博客「" + appConfig.getAi().getModel() + "」的AI助手。");
        sb.append("你的任务是帮助访客了解博客内容、查找文章、解答相关问题。");

        if (context != null && !context.isEmpty()) {
            sb.append("\n\n以下是与用户问题相关的博客文章内容，请基于这些内容回答问题：\n\n");
            sb.append(context);
            sb.append("\n\n请根据以上文章内容回答用户的问题。如果文章内容不足以回答问题，可以结合你的知识进行补充，但要说明哪些是博客内容、哪些是补充知识。");
        } else {
            sb.append("\n\n博客目前没有找到与问题直接相关的文章。你可以基于你的知识回答用户问题，并建议用户浏览博客的搜索或归档功能。");
        }

        return sb.toString();
    }
}
