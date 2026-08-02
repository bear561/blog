package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.entity.SiteConfig;
import com.blog.service.FileService;
import com.blog.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/site-config")
@RequiredArgsConstructor
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;
    private final FileService fileService;

    @GetMapping
    public Result<List<SiteConfig>> getAll() {
        return Result.success(siteConfigService.getAllAsList());
    }

    @PutMapping
    public Result<?> update(@RequestBody Map<String, String> configs) {
        siteConfigService.updateConfigs(configs);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, String> uploadResult = fileService.upload(file);
        String url = uploadResult.get("url");
        // 将头像URL存入站点配置
        siteConfigService.updateConfigs(Map.of("site_avatar", url));
        return Result.success(Map.of("url", url));
    }
}
