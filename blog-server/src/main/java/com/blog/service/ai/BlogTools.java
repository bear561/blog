package com.blog.service.ai;

import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.SiteConfig;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.SiteConfigMapper;
import com.blog.mapper.TagMapper;
import com.blog.vo.CategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BlogTools {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final SiteConfigMapper siteConfigMapper;

    @Tool("获取最新发布的文章列表，返回标题（含链接）、日期和摘要")
    public String getRecentArticles(
            @P("返回数量，默认5，上限20") int count) {
        if (count <= 0) count = 5;
        if (count > 20) count = 20;

        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getIsPublished, 1)
                        .orderByDesc(Article::getCreatedAt)
                        .last("LIMIT " + count));

        if (articles.isEmpty()) return "暂无已发布文章。";

        return "共 " + articles.size() + " 篇\n\n" +
                articles.stream()
                        .map(a -> formatArticle(a))
                        .collect(Collectors.joining("\n\n"));
    }

    @Tool("搜索博客文章，支持关键词、分类、标签筛选。返回标题（含链接）、日期和摘要")
    public String searchArticles(
            @P("搜索关键词，可选") String keyword,
            @P("分类名称，可选") String category,
            @P("标签名称，可选") String tag) {

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsPublished, 1);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
        if (category != null && !category.isBlank()) {
            Category cat = categoryMapper.selectOne(
                    new LambdaQueryWrapper<Category>().eq(Category::getName, category));
            if (cat != null) wrapper.eq(Article::getCategoryId, cat.getId());
        }
        if (tag != null && !tag.isBlank()) {
            Tag tagEntity = tagMapper.selectOne(
                    new LambdaQueryWrapper<Tag>().eq(Tag::getName, tag));
            if (tagEntity != null) {
                wrapper.exists("SELECT 1 FROM t_article_tag at WHERE at.article_id = t_article.id AND at.tag_id = "
                        + tagEntity.getId());
            }
        }

        wrapper.orderByDesc(Article::getCreatedAt).last("LIMIT 10");
        List<Article> articles = articleMapper.selectList(wrapper);

        if (articles.isEmpty()) return "未找到匹配文章。";

        return "找到 " + articles.size() + " 篇\n\n" +
                articles.stream()
                        .map(a -> formatArticle(a))
                        .collect(Collectors.joining("\n\n"));
    }

    @Tool("列出全部分类及各分类文章数量")
    public String listCategories() {
        List<CategoryVO> categories = categoryMapper.selectAllWithArticleCount();
        if (categories.isEmpty()) return "暂无分类。";

        return "共 " + categories.size() + " 个分类\n\n" +
                categories.stream()
                        .map(c -> "- **" + c.getName() + "** · " + c.getArticleCount() + " 篇"
                                + (c.getDescription() != null && !c.getDescription().isEmpty()
                                        ? "\n  " + c.getDescription() : ""))
                        .collect(Collectors.joining("\n\n"));
    }

    @Tool("获取博客基本信息（名称、简介等）")
    public String getSiteInfo() {
        Map<String, SiteConfig> configMap = siteConfigMapper.selectAllAsMap();
        if (configMap.isEmpty()) return "暂无站点信息。";

        StringBuilder sb = new StringBuilder();
        SiteConfig name = configMap.get("site_name");
        if (name != null) sb.append("- 名称：**").append(name.getConfigValue()).append("**\n");
        SiteConfig desc = configMap.get("site_description");
        if (desc != null) sb.append("- 简介：").append(desc.getConfigValue()).append("\n");

        if (sb.isEmpty()) {
            configMap.forEach((key, cfg) ->
                    sb.append("- ").append(key).append(": ").append(cfg.getConfigValue()).append("\n"));
        }
        return sb.toString().trim();
    }

    // --- private helpers ---

    private String formatArticle(Article a) {
        String title = "- [" + a.getTitle() + "](/article/" + a.getId() + ")";
        String date = a.getCreatedAt() != null ? a.getCreatedAt().toLocalDate().toString() : "";
        StringBuilder sb = new StringBuilder(title);
        if (!date.isEmpty()) sb.append("  \n  `").append(date).append("`");
        String summary = a.getSummary();
        if (summary != null && !summary.isBlank()) {
            sb.append("  \n  ").append(summary.length() > 60 ? summary.substring(0, 60) + "…" : summary);
        }
        return sb.toString();
    }
}
