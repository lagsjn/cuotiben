package com.cuotiben.service;

import com.cuotiben.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

/**
 * 图片上传服务：错题支持「文字 + 图片」录入。
 * 图片保存在本地目录（默认 ./uploads），通过 /uploads/** 静态映射访问。
 */
@Service
public class UploadService {

    private static final long MAX_SIZE = 10L * 1024 * 1024;

    private final Path uploadDir;

    public UploadService(@Value("${cuotiben.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40040, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(40041, "图片不能超过 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(40042, "只支持上传图片文件");
        }
        String extension = extensionOf(file.getOriginalFilename(), contentType);
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(fileName));
        } catch (IOException ex) {
            throw new BusinessException(50001, "图片保存失败，请稍后重试");
        }
        return "/uploads/" + fileName;
    }

    private String extensionOf(String originalName, String contentType) {
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.length() <= 6) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
