package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.FriendLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendLinkMapper extends BaseMapper<FriendLink> {

    @Select("SELECT * FROM t_friend_link WHERE is_visible = 1 ORDER BY sort_order ASC, id ASC")
    List<FriendLink> selectVisible();
}
