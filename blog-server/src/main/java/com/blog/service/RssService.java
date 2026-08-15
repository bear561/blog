package com.blog.service;

import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RssService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final SiteConfigService siteConfigService;

    public String generateRssFeed(String baseUrl) {
        try {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");

        String siteName = siteConfigService.getConfigValue("site_name");
        String siteDesc = siteConfigService.getConfigValue("site_description");
        feed.setTitle(siteName != null ? siteName : "My Blog");
        feed.setDescription(siteDesc != null ? siteDesc : "");
        feed.setLink(baseUrl);
        feed.setPublishedDate(new Date());

        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getIsPublished, 1)
                        .orderByDesc(Article::getCreatedAt)
                        .last("LIMIT 20"));

        List<SyndEntry> entries = new ArrayList<>();
        for (Article article : articles) {
            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle(article.getTitle());
            entry.setLink(baseUrl + "/article/" + article.getId());
            entry.setPublishedDate(java.sql.Timestamp.valueOf(article.getCreatedAt()));
            entry.setAuthor(siteName);

            SyndContent desc = new SyndContentImpl();
            desc.setType("text/html");
            desc.setValue(article.getSummary() != null ? article.getSummary() : "");
            entry.setDescription(desc);

            if (article.getCategoryId() != null) {
                Category cat = categoryMapper.selectById(article.getCategoryId());
                if (cat != null) {
                    SyndCategory syndCat = new SyndCategoryImpl();
                    syndCat.setName(cat.getName());
                    entry.setCategories(List.of(syndCat));
                }
            }

            entries.add(entry);
        }
        feed.setEntries(entries);

        return new SyndFeedOutput().outputString(feed);
        } catch (Exception e) {
            return "<error>RSS generation failed</error>";
        }
    }
}
