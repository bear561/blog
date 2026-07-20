package com.blog.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.blog.entity.Article;
import com.blog.vo.ArchiveVO;
import com.blog.vo.ArticleListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT a.id, a.title, a.summary, a.cover_image, a.is_top, a.view_count, a.created_at, " +
            "c.name AS category_name " +
            "FROM t_article a " +
            "LEFT JOIN t_category c ON a.category_id = c.id " +
            "${ew.customSqlSegment}")
    IPage<ArticleListVO> selectArticlePage(IPage<ArticleListVO> page,
                                           @Param(Constants.WRAPPER) Wrapper<Article> wrapper);

    @Select("SELECT * FROM t_article WHERE is_published = 1 AND " +
            "(title LIKE CONCAT('%', #{keyword}, '%') OR summary LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%'))")
    List<Article> selectByKeyword(@Param("keyword") String keyword);

    @Select("SELECT YEAR(created_at) AS year, MONTH(created_at) AS month, " +
            "COUNT(*) AS count " +
            "FROM t_article WHERE is_published = 1 " +
            "GROUP BY YEAR(created_at), MONTH(created_at) " +
            "ORDER BY year DESC, month DESC")
    List<ArchiveVO> selectArchive();

    @Select("SELECT * FROM t_article WHERE is_published = 1 ORDER BY view_count DESC LIMIT #{limit}")
    List<Article> selectHot(@Param("limit") int limit);

    @Update("UPDATE t_article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
}
