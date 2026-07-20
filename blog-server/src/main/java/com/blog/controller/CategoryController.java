package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.CategoryService;
import com.blog.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> getAll() {
        return Result.success(categoryService.getAllCategories());
    }
}
