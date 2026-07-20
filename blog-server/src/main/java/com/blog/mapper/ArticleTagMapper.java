package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ArticleTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    @Delete("DELETE FROM t_article_tag WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Long articleId);

    @Insert("<script>" +
            "INSERT INTO t_article_tag (article_id, tag_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.articleId}, #{item.tagId})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<ArticleTag> list);
}
