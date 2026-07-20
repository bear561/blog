package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/site-config")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    @GetMapping
    public Result<Map<String, String>> getAll() {
        return Result.success(siteConfigService.getAllConfigs());
    }
}
