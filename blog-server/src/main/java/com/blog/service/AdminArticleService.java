package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.common.PageResult;
import com.blog.dto.ArticleDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.vo.ArticleListVO;
import com.blog.vo.ArticleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;

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

    // 后台详情：不分发布状态（草稿也能打开），且不加浏览量
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        return articleService.toArticleVO(article);
    }

    @Transactional
    public ArticleVO createArticle(ArticleDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setContentHtml(articleService.renderMarkdown(dto.getContent()));
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsPublished(dto.getIsPublished() != null ? dto.getIsPublished() : 0);
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        article.setViewCount(0L);
        articleMapper.insert(article);

        articleService.saveArticleTags(article.getId(), dto.getTagIds());

        return articleService.toArticleVO(article);
    }

    @Transactional
    @CacheEvict(value = "article:detail", allEntries = true)
    public ArticleVO updateArticle(Long id, ArticleDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setContentHtml(articleService.renderMarkdown(dto.getContent()));
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsPublished(dto.getIsPublished());
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        articleMapper.updateById(article);

        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        articleService.saveArticleTags(id, dto.getTagIds());

        return articleService.toArticleVO(article);
    }

    @CacheEvict(value = "article:detail", allEntries = true)
    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
    }

    @CacheEvict(value = "article:detail", allEntries = true)
    public void publishArticle(Long id, boolean publish) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        article.setIsPublished(publish ? 1 : 0);
        articleMapper.updateById(article);
    }

    // ===== 私有方法 =====

    private ArticleListVO toAdminArticleListVO(Article article) {
        // 复用公共的 toArticleListVO（含 categoryName/tags），再补上 isPublished
        ArticleListVO vo = articleService.toArticleListVO(article);
        vo.setIsPublished(article.getIsPublished());
        return vo;
    }
}