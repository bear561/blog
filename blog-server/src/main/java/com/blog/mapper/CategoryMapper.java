package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Category;
import com.blog.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT c.id, c.name, c.slug, c.description, c.sort_order, " +
            "COUNT(a.id) AS article_count " +
            "FROM t_category c " +
            "LEFT JOIN t_article a ON c.id = a.category_id AND a.is_published = 1 " +
            "GROUP BY c.id, c.name, c.slug, c.description, c.sort_order " +
            "ORDER BY c.sort_order ASC, c.id ASC")
    List<CategoryVO> selectAllWithArticleCount();
}
