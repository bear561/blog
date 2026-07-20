package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.SiteConfig;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface SiteConfigMapper extends BaseMapper<SiteConfig> {

    @Select("SELECT config_key, config_value FROM t_site_config")
    @MapKey("configKey")
    Map<String, SiteConfig> selectAllAsMap();
}
