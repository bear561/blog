package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.dto.CategoryDTO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.vo.CategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    @Cacheable(value = "category:all")
    public List<CategoryVO> getAllCategories() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));

        return categories.stream().map(cat -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(cat.getId());
            vo.setName(cat.getName());
            vo.setSlug(cat.getSlug());
            vo.setDescription(cat.getDescription());
            vo.setSortOrder(cat.getSortOrder());
            Long count = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getCategoryId, cat.getId())
                            .eq(Article::getIsPublished, 1));
            vo.setArticleCount(count);
            return vo;
        }).collect(Collectors.toList());
    }

    @CacheEvict(value = "category:all", allEntries = true)
    public CategoryVO createCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        categoryMapper.insert(category);

        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());
        vo.setArticleCount(0L);
        return vo;
    }

    @CacheEvict(value = "category:all", allEntries = true)
    public CategoryVO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        categoryMapper.updateById(category);

        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());
        return vo;
    }

    @CacheEvict(value = "category:all", allEntries = true)
    public void deleteCategory(Long id) {
        // 检查是否有文章关联
        Long count = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException(400, "该分类下还有文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
