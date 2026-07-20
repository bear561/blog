package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.vo.DashboardVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @GetMapping
    public Result<DashboardVO> getStats() {
        DashboardVO vo = new DashboardVO();
        vo.setArticleCount(articleMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(null));
        vo.setPendingCommentCount(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getIsReviewed, 0)));
        vo.setCategoryCount(categoryMapper.selectCount(null));
        vo.setTagCount(tagMapper.selectCount(null));
        return Result.success(vo);
    }
}
