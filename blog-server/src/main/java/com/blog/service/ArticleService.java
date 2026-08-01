package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.dto.ArticleDTO;
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
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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

    private static final Parser MD_PARSER;
    private static final HtmlRenderer HTML_RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        MD_PARSER = Parser.builder(options).build();
        HTML_RENDERER = HtmlRenderer.builder(options).build();
    }

    // ===== 公开接口 =====

    public PageResult<ArticleListVO> getArticleList(ArticleQueryDTO query) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getIsPublished, 1)
                .orderByDesc(Article::getIsTop); // 置顶永远优先

        // 次级排序：支持按 时间 / 阅读数 的 升/降序（默认时间倒序）。
        // sortBy 走白名单（仅 viewCount 否则 createdAt），用方法引用，无注入风险。
        boolean isAsc = "asc".equalsIgnoreCase(query.getOrder());
        if ("viewCount".equalsIgnoreCase(query.getSortBy())) {
            wrapper.orderBy(true, isAsc, Article::getViewCount);
        } else {
            wrapper.orderBy(true, isAsc, Article::getCreatedAt);
        }

        // 分类过滤：优先用 id；否则按 slug 解析（slug 查不到回退 name）。
        // 显式传了 slug 却解析不到 → 用 -1 哨兵，使结果为空（而非返回全部）。
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

        // 标签过滤：优先用 id；否则按 slug 解析（slug 查不到回退 name）；查不到 → 空结果。
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

    @Cacheable(value = "article:detail", key = "#id")
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getIsPublished() == 0) {
            throw new BusinessException(404, "文章不存在");
        }
        // 异步增加浏览量 (简化: 同步)
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);

        return toArticleVO(article);
    }

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

    public List<ArticleListVO> getHotArticles(int limit) {
        return articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getIsPublished, 1)
                        .orderByDesc(Article::getViewCount)
                        .last("LIMIT " + limit))
                .stream().map(this::toArticleListVO).collect(Collectors.toList());
    }

    public PageResult<ArticleListVO> search(String keyword, int page, int size) {
        // 使用 LIKE 搜索
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

    // ===== 管理接口 =====

    public PageResult<ArticleListVO> adminGetArticles(int page, int size, Integer isPublished) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getCreatedAt);
        if (isPublished != null) {
            wrapper.eq(Article::getIsPublished, isPublished);
        }

        Page<Article> p = new Page<>(page, size);
        IPage<Article> result = articleMapper.selectPage(p, wrapper);

        List<ArticleListVO> records = result.getRecords().stream()
                .map(this::toAdminArticleListVO)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), page, size);
    }

    @Transactional
    @CacheEvict(value = {"article:list", "article:hot", "article:archive"}, allEntries = true)
    public ArticleVO createArticle(ArticleDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setContentHtml(MD_PARSER.parse(dto.getContent()) != null
                ? HTML_RENDERER.render(MD_PARSER.parse(dto.getContent())) : "");
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsPublished(dto.getIsPublished() != null ? dto.getIsPublished() : 0);
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        article.setViewCount(0L);
        articleMapper.insert(article);

        saveArticleTags(article.getId(), dto.getTagIds());

        return toArticleVO(article);
    }

    @Transactional
    @CacheEvict(value = {"article:list", "article:hot", "article:archive", "article:detail"}, allEntries = true)
    public ArticleVO updateArticle(Long id, ArticleDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setContentHtml(MD_PARSER.parse(dto.getContent()) != null
                ? HTML_RENDERER.render(MD_PARSER.parse(dto.getContent())) : "");
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsPublished(dto.getIsPublished());
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        articleMapper.updateById(article);

        // 更新标签
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        saveArticleTags(id, dto.getTagIds());

        return toArticleVO(article);
    }

    @CacheEvict(value = {"article:list", "article:hot", "article:archive", "article:detail"}, allEntries = true)
    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
    }

    @CacheEvict(value = {"article:list", "article:hot", "article:archive", "article:detail"}, allEntries = true)
    public void publishArticle(Long id, boolean publish) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        article.setIsPublished(publish ? 1 : 0);
        articleMapper.updateById(article);
    }

    // ===== 私有方法 =====

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    private ArticleVO toArticleVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setContentHtml(article.getContentHtml());
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

    private ArticleListVO toArticleListVO(Article article) {
        ArticleListVO vo = new ArticleListVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setIsTop(article.getIsTop());
        vo.setViewCount(article.getViewCount());
        vo.setCreatedAt(article.getCreatedAt());

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        vo.setTags(getArticleTagVOs(article.getId()));
        return vo;
    }

    private ArticleListVO toAdminArticleListVO(Article article) {
        ArticleListVO vo = toArticleListVO(article);
        vo.setIsPublished(article.getIsPublished());
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

    private List<TagVO> getArticleTagVOs(Long articleId) {
        return getArticleTags(articleId);
    }
}
