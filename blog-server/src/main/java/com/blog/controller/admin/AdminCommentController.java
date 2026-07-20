package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public Result<?> getComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer isReviewed) {
        return Result.success(commentService.adminGetComments(page, size, isReviewed));
    }

    @PutMapping("/{id}/review")
    public Result<?> review(@PathVariable Long id, @RequestParam boolean approved) {
        commentService.reviewComment(id, approved);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}
