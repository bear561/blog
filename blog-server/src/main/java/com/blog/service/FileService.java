package com.blog.service;

import com.blog.common.AppConfig;
import com.blog.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final AppConfig appConfig;

    public Map<String, String> upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }

        // 限制文件大小 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过5MB");
        }

        // 获取文件扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        // 按日期分目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFileName = UUID.randomUUID().toString() + ext;
        String relativePath = dateDir + "/" + newFileName;

        try {
            Path uploadPath = Paths.get(appConfig.getUpload().getPath(), dateDir);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(newFileName);
            file.transferTo(filePath.toFile());

            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + relativePath);
            result.put("filename", originalName);
            return result;
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }
}
