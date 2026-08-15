package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.ArticleDTO;
import com.blog.service.AdminArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final AdminArticleService adminArticleService;

    @GetMapping
    public Result<?> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer isPublished) {
        return Result.success(adminArticleService.adminGetArticles(page, size, isPublished));
    }

    @GetMapping("/{id}")
    public Result<?> getArticle(@PathVariable Long id) {
        return Result.success(adminArticleService.getArticleDetail(id));
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody ArticleDTO dto) {
        return Result.success(adminArticleService.createArticle(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        return Result.success(adminArticleService.updateArticle(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminArticleService.deleteArticle(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    public Result<?> publish(@PathVariable Long id, @RequestParam boolean publish) {
        adminArticleService.publishArticle(id, publish);
        return Result.success();
    }
}
