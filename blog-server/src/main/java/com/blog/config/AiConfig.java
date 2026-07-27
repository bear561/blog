package com.blog.config;

import com.blog.service.ai.BlogAiAssistant;
import com.blog.service.ai.BlogTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String modelName;

    @Bean
    OpenAiStreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl("https://api.deepseek.com/v1")
                .maxTokens(2000)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }

    @Bean
    BlogAiAssistant blogAiAssistant(OpenAiStreamingChatModel streamingChatModel,
                                     ChatMemoryProvider chatMemoryProvider,
                                     BlogTools blogTools) {
        return AiServices.builder(BlogAiAssistant.class)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(blogTools)
                .build();
    }
}
