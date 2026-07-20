package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.TagService;
import com.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagVO>> getAll() {
        return Result.success(tagService.getAllTags());
    }
}
