package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.dto.ArticleQueryDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.vo.ArticleListVO;
import com.blog.vo.ArticleVO;
import com.blog.vo.ArchiveVO;
import com.blog.vo.TagVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    static final Parser MD_PARSER;
    static final HtmlRenderer HTML_RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(TablesExtension.create()));
        MD_PARSER = Parser.builder(options).build();
        HTML_RENDERER = HtmlRenderer.builder(options).build();
    }

    // ===== 公开接口 =====

    @Cacheable(value = "article:list", key = "#query")
    public PageResult<ArticleListVO> getArticleList(ArticleQueryDTO query) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsPublished, 1)
                .orderByDesc(Article::getIsTop);

        boolean isAsc = "asc".equalsIgnoreCase(query.getOrder());
        if ("viewCount".equalsIgnoreCase(query.getSortBy())) {
            wrapper.orderBy(true, isAsc, Article::getViewCount);
        } else {
            wrapper.orderBy(true, isAsc, Article::getCreatedAt);
        }

        Long categoryId = query.getCategoryId();
        if (categoryId == null && StringUtils.hasText(query.getCategorySlug())) {
            Category cat = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                    .eq(Category::getSlug, query.getCategorySlug()).last("LIMIT 1"));
            if (cat == null) {
                cat = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                        .eq(Category::getName, query.getCategorySlug()).last("LIMIT 1"));
            }
            categoryId = cat != null ? cat.getId() : -1L;
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        Long tagId = query.getTagId();
        if (tagId == null && StringUtils.hasText(query.getTagSlug())) {
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getSlug, query.getTagSlug()).last("LIMIT 1"));
            if (tag == null) {
                tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getName, query.getTagSlug()).last("LIMIT 1"));
            }
            tagId = tag != null ? tag.getId() : -1L;
        }
        if (tagId != null) {
            wrapper.exists("SELECT 1 FROM t_article_tag at WHERE at.article_id = t_article.id AND at.tag_id = " + tagId);
        }

        if (query.getYear() != null) {
            wrapper.apply("YEAR(created_at) = {0}", query.getYear());
        }
        if (query.getMonth() != null) {
            wrapper.apply("MONTH(created_at) = {0}", query.getMonth());
        }

        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        IPage<Article> result = articleMapper.selectPage(page, wrapper);

        List<ArticleListVO> records = result.getRecords().stream()
                .map(this::toArticleListVO)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    // 详情缓存 30 分钟；浏览量由 incrementViewCount 原子自增（在缓存外），两者互不干扰。
    // 发布状态校验由调用方先执行 checkPublished，这里不做重复校验。
    @Cacheable(value = "article:detail", key = "#id")
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        return toArticleVO(article);
    }

    // 仅校验文章是否已发布（未发布抛 404），供浏览量计数前拦截草稿
    public void checkPublished(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsPublished() == 0) {
            throw new BusinessException(404, "文章不存在");
        }
    }

    // 原子自增浏览量，与详情读取解耦
    public void incrementViewCount(Long id) {
        articleMapper.incrementViewCount(id);
    }

    @Cacheable(value = "article:archive")
    public List<ArchiveVO> getArchive() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getIsPublished, 1)
                        .orderByDesc(Article::getCreatedAt));

        DateTimeFormatter yearFmt = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MM");

        Map<String, List<ArticleListVO>> grouped = articles.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCreatedAt().format(yearFmt) + "-" + a.getCreatedAt().format(monthFmt),
                        LinkedHashMap::new,
                        Collectors.mapping(this::toArticleListVO, Collectors.toList())
                ));

        return grouped.entrySet().stream().map(entry -> {
            ArchiveVO vo = new ArchiveVO();
            String[] parts = entry.getKey().split("-");
            vo.setYear(Integer.parseInt(parts[0]));
            vo.setMonth(Integer.parseInt(parts[1]));
            vo.setArticles(entry.getValue());
            return vo;
        }).collect(Collectors.toList());
    }

    @Cacheable(value = "article:hot", key = "#limit")
    public List<ArticleListVO> getHotArticles(int limit) {
        return articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getIsPublished, 1)
                        .orderByDesc(Article::getViewCount)
                        .last("LIMIT " + limit))
                .stream().map(this::toArticleListVO).collect(Collectors.toList());
    }

    public PageResult<ArticleListVO> search(String keyword, int page, int size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsPublished, 1)
                .and(w -> w.like(Article::getTitle, keyword)
                        .or().like(Article::getSummary, keyword)
                        .or().like(Article::getContent, keyword))
                .orderByDesc(Article::getCreatedAt);

        Page<Article> p = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPage(p, wrapper);

        List<ArticleListVO> records = result.getRecords().stream()
                .map(this::toArticleListVO)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), page, size);
    }

    // ===== 包级共享方法（供 AdminArticleService 调用） =====

    ArticleVO toArticleVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setContentHtml(article.getContentHtml());
        vo.setReadMinutes(estimateReadMinutes(article.getContentHtml()));
        vo.setCoverImage(article.getCoverImage());
        vo.setCategoryId(article.getCategoryId());
        vo.setIsTop(article.getIsTop());
        vo.setViewCount(article.getViewCount());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            vo.setCategoryName(category != null ? category.getName() : null);
        }

        vo.setTags(getArticleTags(article.getId()));
        return vo;
    }

    void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    String renderMarkdown(String content) {
        if (content == null) return "";
        return HTML_RENDERER.render(MD_PARSER.parse(content));
    }

    /**
     * 估算阅读分钟数：去除 HTML 标签后按中文字数估算，纯中文约 350 字/分钟，
     * 最少 1 分钟。规则与前端 readingTime.js 保持一致，保证列表/详情同一来源。
     */
    public static int estimateReadMinutes(String html) {
        if (html == null || html.isBlank()) {
            return 1;
        }
        String text = html.replaceAll("<[^>]+>", "").replaceAll("\\s+", "");
        return Math.max(1, (int) Math.round(text.length() / 350.0));
    }

    // ===== 私有方法 =====

    // 供 AdminArticleService 构建后台列表复用（含 categoryName/tags）
    ArticleListVO toArticleListVO(Article article) {
        ArticleListVO vo = new ArticleListVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setIsTop(article.getIsTop());
        vo.setViewCount(article.getViewCount());
        vo.setReadMinutes(estimateReadMinutes(article.getContentHtml()));
        vo.setCreatedAt(article.getCreatedAt());

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        vo.setTags(getArticleTags(article.getId()));
        return vo;
    }

    private List<TagVO> getArticleTags(Long articleId) {
        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));
        if (relations.isEmpty()) return Collections.emptyList();

        return relations.stream().map(at -> {
            Tag tag = tagMapper.selectById(at.getTagId());
            TagVO vo = new TagVO();
            if (tag != null) {
                vo.setId(tag.getId());
                vo.setName(tag.getName());
                vo.setSlug(tag.getSlug());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
