package com.blog.service.ai;

import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 简化的 RAG 上下文构建器
 * 使用 MySQL LIKE 搜索匹配相关文章，构建 AI 对话上下文
 */
@Component
@RequiredArgsConstructor
public class ArticleContextBuilder {

    private final ArticleMapper articleMapper;

    private static final int MAX_ARTICLES = 3;
    private static final int MAX_CONTENT_LENGTH = 2000;

    /**
     * 根据用户问题检索相关文章并构建上下文
     */
    public String buildContext(String question) {
        // 提取关键词（简单分词：按空格、标点分割）
        String[] keywords = question.split("[\\s，。！？,.!?]+");

        List<Article> relevantArticles = searchArticles(keywords);

        if (relevantArticles.isEmpty()) {
            return "";
        }

        return relevantArticles.stream()
                .map(article -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("【").append(article.getTitle()).append("】\n");
                    // 截取内容
                    String content = article.getContent() != null ? article.getContent() : "";
                    // 去掉 markdown 标记的简单处理
                    content = content.replaceAll("[#*`>\\-\\[\\]()]", " ");
                    if (content.length() > MAX_CONTENT_LENGTH) {
                        content = content.substring(0, MAX_CONTENT_LENGTH) + "...";
                    }
                    sb.append(content);
                    return sb.toString();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private List<Article> searchArticles(String[] keywords) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsPublished, 1);

        // 构建 OR 条件
        if (keywords.length > 0) {
            wrapper.and(w -> {
                for (int i = 0; i < keywords.length; i++) {
                    String kw = keywords[i].trim();
                    if (kw.isEmpty()) continue;
                    if (i == 0) {
                        w.like(Article::getTitle, kw).or().like(Article::getSummary, kw);
                    } else {
                        w.or().like(Article::getTitle, kw).or().like(Article::getSummary, kw);
                    }
                }
            });
        }

        wrapper.orderByDesc(Article::getCreatedAt).last("LIMIT " + MAX_ARTICLES);
        return articleMapper.selectList(wrapper);
    }
}
