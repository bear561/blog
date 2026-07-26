package com.blog.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface BlogAiAssistant {

    @SystemMessage("""
        你是Xiong Jie博客的AI助手。你的任务是帮助访客了解博客内容、查找文章、解答相关问题。

        如果用户消息中包含相关文章内容，请基于这些内容给出准确答案。
        如果用户消息中没有相关文章内容，可以结合你的知识进行回答，但要说明信息来源。
        请保持回答简洁、友好、有帮助。用中文回答。""")
    TokenStream chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
