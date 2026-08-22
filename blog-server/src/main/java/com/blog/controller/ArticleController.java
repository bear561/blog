package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.ArticleQueryDTO;
import com.blog.service.ArticleService;
import com.blog.vo.ArticleListVO;
import com.blog.vo.ArticleVO;
import com.blog.vo.ArchiveVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<?> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String tagSlug,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        // 前端统一传 pageSize，后台 API 参数名为 size，这里做兼容（pageSize 优先）
        if (pageSize != null) size = pageSize;

        ArticleQueryDTO query = new ArticleQueryDTO();
        query.setPage(page);
        query.setSize(size);
        query.setCategoryId(categoryId);
        query.setTagId(tagId);
        query.setCategorySlug(categorySlug);
        query.setTagSlug(tagSlug);
        query.setSortBy(sortBy);
        query.setOrder(order);
        query.setKeyword(keyword);
        query.setYear(year);
        query.setMonth(month);

        // 有关键词时走搜索
        if (keyword != null && !keyword.isEmpty()) {
            return Result.success(articleService.search(keyword, page, size));
        }
        return Result.success(articleService.getArticleList(query));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        // ① 校验已发布（草稿在此抛 404，不计数）→ ② 原子自增 → ③ 再取详情（返回最新浏览量）
        articleService.checkPublished(id);
        articleService.incrementViewCount(id);
        return Result.success(articleService.getArticleDetail(id));
    }

    @GetMapping("/archive")
    public Result<List<ArchiveVO>> getArchive() {
        return Result.success(articleService.getArchive());
    }

    @GetMapping("/hot")
    public Result<List<ArticleListVO>> getHot(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(articleService.getHotArticles(limit));
    }

    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) Integer pageSize) {
        if (pageSize != null) size = pageSize;
        return Result.success(articleService.search(keyword, page, size));
    }
}
