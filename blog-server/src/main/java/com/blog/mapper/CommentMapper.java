package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM t_comment WHERE article_id = #{articleId} AND is_reviewed = 1 " +
            "ORDER BY created_at ASC")
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);
}
