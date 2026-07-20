package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.CommentDTO;
import com.blog.service.CommentService;
import com.blog.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public Result<List<CommentVO>> getComments(@RequestParam Long articleId) {
        return Result.success(commentService.getCommentsByArticleId(articleId));
    }

    @PostMapping
    public Result<?> submitComment(@RequestBody CommentDTO dto, HttpServletRequest request) {
        commentService.submitComment(dto, request);
        return Result.success("评论提交成功，等待审核");
    }
}
