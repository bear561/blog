package com.blog.controller;

import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.service.SiteConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RssController {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final SiteConfigService siteConfigService;

    @GetMapping(value = "/rss", produces = MediaType.APPLICATION_XML_VALUE)
    public String rss() {
        try {
            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType("rss_2.0");

            String siteName = siteConfigService.getConfigValue("site_name");
            String siteDesc = siteConfigService.getConfigValue("site_description");
            feed.setTitle(siteName != null ? siteName : "My Blog");
            feed.setDescription(siteDesc != null ? siteDesc : "");
            feed.setLink("http://localhost");
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
                entry.setLink("http://localhost/article/" + article.getId());
                entry.setPublishedDate(java.sql.Timestamp.valueOf(article.getCreatedAt()));
                entry.setAuthor(siteName);

                SyndContent desc = new SyndContentImpl();
                desc.setType("text/html");
                desc.setValue(article.getSummary() != null ? article.getSummary() : "");
                entry.setDescription(desc);

                // 分类
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
