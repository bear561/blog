package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final FileService fileService;

    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.upload(file));
    }
}
