package com.blog.service;

import com.blog.entity.SiteConfig;
import com.blog.mapper.SiteConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteConfigService {

    private final SiteConfigMapper siteConfigMapper;

    @Cacheable(value = "site:config")
    public Map<String, String> getAllConfigs() {
        List<SiteConfig> configs = siteConfigMapper.selectList(
                new LambdaQueryWrapper<SiteConfig>().orderByAsc(SiteConfig::getId));
        Map<String, String> map = new LinkedHashMap<>();
        for (SiteConfig config : configs) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    public String getConfigValue(String key) {
        Map<String, String> configs = getAllConfigs();
        return configs.getOrDefault(key, "");
    }

    @CacheEvict(value = "site:config", allEntries = true)
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SiteConfig config = siteConfigMapper.selectOne(
                    new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, entry.getKey()));
            if (config != null) {
                config.setConfigValue(entry.getValue());
                siteConfigMapper.updateById(config);
            } else {
                config = new SiteConfig();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                siteConfigMapper.insert(config);
            }
        }
    }

    public List<SiteConfig> getAllAsList() {
        return siteConfigMapper.selectList(
                new LambdaQueryWrapper<SiteConfig>().orderByAsc(SiteConfig::getId));
    }
}
