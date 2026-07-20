package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Tag;
import com.blog.vo.TagVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("SELECT t.id, t.name, t.slug, COUNT(at.article_id) AS article_count " +
            "FROM t_tag t " +
            "LEFT JOIN t_article_tag at ON t.id = at.tag_id " +
            "GROUP BY t.id, t.name, t.slug " +
            "ORDER BY t.id ASC")
    List<TagVO> selectAllWithArticleCount();

    @Select("SELECT t.id, t.name, t.slug FROM t_tag t " +
            "INNER JOIN t_article_tag at ON t.id = at.tag_id " +
            "WHERE at.article_id = #{articleId}")
    List<TagVO> selectByArticleId(@Param("articleId") Long articleId);
}
