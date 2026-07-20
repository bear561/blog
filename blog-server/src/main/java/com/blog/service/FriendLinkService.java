package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.dto.FriendLinkDTO;
import com.blog.entity.FriendLink;
import com.blog.mapper.FriendLinkMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendLinkService {

    private final FriendLinkMapper friendLinkMapper;

    @Cacheable(value = "friend:links")
    public List<FriendLink> getVisibleLinks() {
        return friendLinkMapper.selectList(
                new LambdaQueryWrapper<FriendLink>()
                        .eq(FriendLink::getIsVisible, 1)
                        .orderByAsc(FriendLink::getSortOrder));
    }

    public List<FriendLink> getAllLinks() {
        return friendLinkMapper.selectList(
                new LambdaQueryWrapper<FriendLink>().orderByAsc(FriendLink::getSortOrder));
    }

    @CacheEvict(value = "friend:links", allEntries = true)
    public FriendLink createLink(FriendLinkDTO dto) {
        FriendLink link = new FriendLink();
        link.setName(dto.getName());
        link.setUrl(dto.getUrl());
        link.setAvatar(dto.getAvatar());
        link.setDescription(dto.getDescription());
        link.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        link.setIsVisible(dto.getIsVisible() != null ? dto.getIsVisible() : 1);
        friendLinkMapper.insert(link);
        return link;
    }

    @CacheEvict(value = "friend:links", allEntries = true)
    public FriendLink updateLink(Long id, FriendLinkDTO dto) {
        FriendLink link = friendLinkMapper.selectById(id);
        if (link == null) {
            throw new BusinessException(404, "友链不存在");
        }
        link.setName(dto.getName());
        link.setUrl(dto.getUrl());
        link.setAvatar(dto.getAvatar());
        link.setDescription(dto.getDescription());
        link.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        link.setIsVisible(dto.getIsVisible() != null ? dto.getIsVisible() : 1);
        friendLinkMapper.updateById(link);
        return link;
    }

    @CacheEvict(value = "friend:links", allEntries = true)
    public void deleteLink(Long id) {
        friendLinkMapper.deleteById(id);
    }
}
