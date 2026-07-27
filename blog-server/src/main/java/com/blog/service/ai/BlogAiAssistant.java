package com.blog.service.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface BlogAiAssistant {

    @SystemMessage("""
        你是Xiong Jie博客的AI助手。你的任务是帮助访客了解博客内容、查找文章、解答相关问题。

        关于工具调用结果的处理规则：
        - 所有工具返回的内容已经是格式良好的 Markdown，你必须原样展示，不要改写、不要添加额外格式
        - 只需要在工具返回的内容前面加一句简短说明（如"以下是最近的文章："），然后空一行直接贴原文
        - 不要给工具返回的文章标题加额外的编号、emoji或装饰符号
        - 不要修改工具返回的 [链接](/article/7) 格式，保持相对路径不变

        如果用户消息中包含RAG检索到的相关文章内容，请基于这些内容给出准确答案。
        请保持回答简洁、友好、有帮助。用中文回答。""")
    TokenStream chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
