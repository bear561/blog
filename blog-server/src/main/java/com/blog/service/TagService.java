package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.dto.TagDTO;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.vo.TagVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Cacheable(value = "tag:all")
    public List<TagVO> getAllTags() {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getId));

        return tags.stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setSlug(tag.getSlug());
            Long count = articleTagMapper.selectCount(
                    new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tag.getId()));
            vo.setArticleCount(count);
            return vo;
        }).collect(Collectors.toList());
    }

    @CacheEvict(value = "tag:all", allEntries = true)
    public TagVO createTag(TagDTO dto) {
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setSlug(dto.getSlug() != null ? dto.getSlug() : dto.getName());
        tagMapper.insert(tag);

        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setSlug(tag.getSlug());
        vo.setArticleCount(0L);
        return vo;
    }

    @CacheEvict(value = "tag:all", allEntries = true)
    public TagVO updateTag(Long id, TagDTO dto) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(404, "标签不存在");
        }
        tag.setName(dto.getName());
        tag.setSlug(dto.getSlug() != null ? dto.getSlug() : dto.getName());
        tagMapper.updateById(tag);

        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setSlug(tag.getSlug());
        return vo;
    }

    @CacheEvict(value = "tag:all", allEntries = true)
    public void deleteTag(Long id) {
        tagMapper.deleteById(id);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
    }
}
