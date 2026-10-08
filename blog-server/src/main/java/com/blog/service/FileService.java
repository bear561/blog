package com.blog.service;

import com.blog.common.AppConfig;
import com.blog.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    /** WebP 质量（0-100）。82 在照片上实测 PSNR 44dB+，肉眼无损 */
    private static final int WEBP_QUALITY = 82;
    /** WebP 长边上限，超过则等比缩小（和已有存量图一致的 1600） */
    private static final int WEBP_MAX_DIMENSION = 1600;
    /** 生成 WebP 的超时秒数，避免 cwebp 异常时拖死上传请求 */
    private static final int WEBP_TIMEOUT_SECONDS = 30;
    /** 生成 WebP 的源格式白名单：只处理封面实际在用的两种位图 */
    private static final List<String> WEBP_SOURCE_EXTS = List.of(".png", ".jpg", ".jpeg");

    private final AppConfig appConfig;

    public Map<String, String> upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }

        // 限制文件大小 10MB（与 application.yml 的 multipart.max-file-size 对齐）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过10MB");
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

            // 生成同名 .webp 副本，由 nginx 按 Accept 头内容协商返回。
            // 数据库里存的 url 仍然指向原文件，所以这一步失败不影响功能，只是图片没被压缩
            generateWebp(filePath);

            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + relativePath);
            result.put("filename", originalName);
            return result;
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 调 cwebp 生成 {@code <原文件名>.webp} 副本，与 nginx 的
     * {@code try_files $uri$webp_suffix $uri} 约定一一对应。
     * 任何失败都只记日志、不抛异常——上传本身不能因为压缩失败而挂掉。
     */
    private void generateWebp(Path original) {
        String name = original.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return;
        }
        String ext = name.substring(dot).toLowerCase(Locale.ROOT);
        // GIF（动图会丢帧）、SVG、以及 webp 本身等一律跳过
        if (!WEBP_SOURCE_EXTS.contains(ext)) {
            return;
        }

        Path target = original.resolveSibling(name + ".webp");
        try {
            List<String> cmd = new ArrayList<>(List.of(
                    "cwebp", "-quiet", "-q", String.valueOf(WEBP_QUALITY), "-metadata", "none"));

            // 按长边等比缩小；读尺寸只解文件头，不把整张图解进内存
            int[] size = readImageSize(original);
            if (size != null && Math.max(size[0], size[1]) > WEBP_MAX_DIMENSION) {
                double scale = (double) WEBP_MAX_DIMENSION / Math.max(size[0], size[1]);
                cmd.add("-resize");
                cmd.add(String.valueOf(Math.max(1, (int) Math.round(size[0] * scale))));
                cmd.add(String.valueOf(Math.max(1, (int) Math.round(size[1] * scale))));
            }

            cmd.add(original.toString());
            cmd.add("-o");
            cmd.add(target.toString());

            // 丢弃 cwebp 输出，避免管道写满导致 waitFor 死锁（-quiet 之后输出也很少）
            Process process = new ProcessBuilder(cmd)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();

            if (!process.waitFor(WEBP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                Files.deleteIfExists(target);
                log.warn("生成 WebP 超时，已跳过: {}", name);
                return;
            }
            if (process.exitValue() != 0 || !Files.exists(target)) {
                Files.deleteIfExists(target);
                log.warn("生成 WebP 失败(exit={})，已跳过: {}", process.exitValue(), name);
                return;
            }
            // 没变小（比如原图本来就只有几十 KB）就丢掉，保证 nginx 只会在有收益时才用副本
            long originalSize = Files.size(original);
            long targetSize = Files.size(target);
            if (targetSize >= originalSize) {
                Files.deleteIfExists(target);
                return;
            }
            log.info("生成 WebP: {} {}B -> {}B", name, originalSize, targetSize);
        } catch (IOException e) {
            // cwebp 未安装 / 不可执行：只记日志，上传仍然成功
            log.warn("调用 cwebp 失败，跳过 WebP 生成: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("生成 WebP 被中断: {}", name);
        }
    }

    /** 读文件头拿宽高，读不出（非图片 / 格式不支持）返回 null */
    private int[] readImageSize(Path file) {
        try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
            if (in == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                return new int[]{reader.getWidth(0), reader.getHeight(0)};
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return null;
        }
    }
}