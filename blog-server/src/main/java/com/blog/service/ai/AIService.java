package com.blog.service.ai;

import com.blog.dto.AIChatRequest;
import reactor.core.publisher.Flux;

public interface AIService {

    /**
     * 流式对话
     * @param request 请求（问题 + 历史 + 上下文）
     * @return 流式返回的字符串 Flux
     */
    Flux<String> chatStream(AIChatRequest request, String context);
}
