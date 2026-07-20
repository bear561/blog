package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.CategoryDTO;
import com.blog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<?> getAll() {
        return Result.success(categoryService.getAllCategories());
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody CategoryDTO dto) {
        return Result.success(categoryService.createCategory(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return Result.success(categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
