package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.entity.SiteConfig;
import com.blog.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/site-config")
@RequiredArgsConstructor
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;

    @GetMapping
    public Result<List<SiteConfig>> getAll() {
        return Result.success(siteConfigService.getAllAsList());
    }

    @PutMapping
    public Result<?> update(@RequestBody Map<String, String> configs) {
        siteConfigService.updateConfigs(configs);
        return Result.success();
    }
}
