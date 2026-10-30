package com.school.dismissal.service.impl;

import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.ResultCode;
import com.school.dismissal.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

    /** 头像/图片最大 5MB（与 application.yml 中 multipart 配置对齐） */
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    /** 只允许真实图片 MIME（看 Content-Type，不看后缀） */
    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Override
    public String uploadImage(MultipartFile file, String dirPrefix) {
        // 1. 非空
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        // 2. 大小
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE, "图片不能超过 2MB");
        }
        // 3. MIME 类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED, "只允许 jpg/png/webp 图片");
        }
        // 4. 真实图片校验：能被 ImageIO 解码才是图，挡住改后缀的伪装文件
        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED, "文件不是有效图片");
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.UPLOAD_FAIL, "文件读取失败");
        }

        // 5. 扩展名由 MIME 决定，绝不使用原始文件名
        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };

        // 6. 路径：业务目录/yyyy/MM/dd/uuid.ext
        String relativePath = dirPrefix + "/"
                + LocalDate.now().format(DATE_DIR) + "/"
                + UUID.randomUUID().toString().replace("-", "") + ext;

        // 7. 写入磁盘
        try {
            Path target = Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("文件写入失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.UPLOAD_FAIL, "文件保存失败");
        }

        // 8. 返回完整 URL（含协议+域名）
        return publicBaseUrl + urlPrefix + "/" + relativePath;
    }

    @Override
    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) return;
        // 只删本服务管理的路径
        String marker = urlPrefix + "/";
        int idx = url.indexOf(marker);
        if (idx < 0) return;
        String relative = url.substring(idx + marker.length());
        try {
            Path target = Paths.get(uploadDir, relative).toAbsolutePath().normalize();
            // 安全校验：禁止路径穿越
            if (!target.startsWith(Paths.get(uploadDir).toAbsolutePath().normalize())) {
                log.warn("非法删除路径被拒绝: {}", relative);
                return;
            }
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("删除旧文件失败: {}, 原因: {}", url, e.getMessage());
        }
    }
}
