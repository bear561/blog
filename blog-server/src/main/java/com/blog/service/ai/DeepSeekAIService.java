package com.blog.service.ai;

import com.blog.common.AppConfig;
import com.blog.dto.AIChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "deepseek")
public class DeepSeekAIService implements AIService {

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;

    public DeepSeekAIService(AppConfig appConfig, ObjectMapper objectMapper) {
        this.appConfig = appConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<String> chatStream(AIChatRequest request, String context) {
        return Flux.create(sink -> {
            try {
                List<Map<String, String>> messages = new ArrayList<>();

                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", buildSystemPrompt(context));
                messages.add(systemMsg);

                if (request.getHistory() != null) {
                    for (AIChatRequest.ChatMessage msg : request.getHistory()) {
                        Map<String, String> m = new HashMap<>();
                        m.put("role", msg.getRole());
                        m.put("content", msg.getContent());
                        messages.add(m);
                    }
                }

                Map<String, String> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", request.getQuestion());
                messages.add(userMsg);

                Map<String, Object> body = new HashMap<>();
                body.put("model", appConfig.getAi().getModel());
                body.put("messages", messages);
                body.put("stream", true);
                body.put("max_tokens", appConfig.getAi().getMaxTokens());

                String jsonBody = objectMapper.writeValueAsString(body);
                URI uri = new URI("https://api.deepseek.com/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + appConfig.getAi().getApiKey());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(60000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status != 200) {
                    sink.error(new RuntimeException("AI API returned status " + status));
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                            String json = line.substring(6).trim();
                            if (json.isEmpty()) continue;
                            try {
                                JsonNode node = objectMapper.readTree(json);
                                JsonNode choices = node.get("choices");
                                if (choices != null && choices.size() > 0) {
                                    JsonNode delta = choices.get(0).get("delta");
                                    if (delta != null && delta.has("content") && !delta.get("content").isNull()) {
                                        String content = delta.get("content").asText();
                                        if (!content.isEmpty()) {
                                            sink.next(content);
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
                sink.complete();
            } catch (Exception e) {
                log.error("AI chat error", e);
                sink.error(e);
            }
        });
    }

    private String buildSystemPrompt(String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是博客的AI助手。你的任务是帮助访客了解博客内容、查找文章、解答相关问题。");

        if (context != null && !context.isEmpty()) {
            sb.append("\n\n以下是与用户问题相关的博客文章内容，请基于这些内容回答问题：\n\n");
            sb.append(context);
            sb.append("\n\n请根据以上文章内容回答用户的问题。");
        }

        return sb.toString();
    }
}
