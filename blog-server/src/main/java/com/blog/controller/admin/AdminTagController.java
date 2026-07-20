package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.TagDTO;
import com.blog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @GetMapping
    public Result<?> getAll() {
        return Result.success(tagService.getAllTags());
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody TagDTO dto) {
        return Result.success(tagService.createTag(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody TagDTO dto) {
        return Result.success(tagService.updateTag(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
