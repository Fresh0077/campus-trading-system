package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.security.UserPrincipal;
import com.example.campussystem.service.FileUploadService;
import com.example.campussystem.service.MinIOService;
import com.example.campussystem.service.SystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/files")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 上传单个图片
     */
    @PostMapping("/upload/image")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "products") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.badRequest("文件不能为空"));
            }
            
            String filePath = fileUploadService.uploadImage(file, category);
            
            // 记录操作日志
            systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "上传图片",
                "分类: " + category + ", 文件: " + file.getOriginalFilename()
            );
            
            // 获取 MinIO 访问 URL
            String fileUrl = fileUploadService.getFileUrl(filePath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath); // MinIO 对象名称（应该保存到数据库）
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("url", fileUrl); // MinIO 访问 URL（仅用于预览）
            
            return ResponseEntity.ok(ApiResponse.success(result, "图片上传成功"));
            
        } catch (BusinessException e) {
            logger.error("图片上传业务异常: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("图片上传失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("图片上传系统异常: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("图片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/upload/images")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(defaultValue = "products") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String filePath = fileUploadService.uploadImage(file, category);
                
                // 获取 MinIO 访问 URL
                String fileUrl = fileUploadService.getFileUrl(filePath);
                
                Map<String, Object> result = new HashMap<>();
                result.put("filePath", filePath); // MinIO 对象名称
                result.put("fileName", file.getOriginalFilename());
                result.put("fileSize", file.getSize());
                result.put("url", fileUrl); // MinIO 访问 URL
                result.put("success", true);
                
                results.add(result);
                
            } catch (Exception e) {
                Map<String, Object> result = new HashMap<>();
                result.put("fileName", file.getOriginalFilename());
                result.put("error", e.getMessage());
                result.put("success", false);
                
                results.add(result);
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        // 记录操作日志
        systemLogService.logOperation(
            userPrincipal.getUserId().toString(),
            "批量上传图片",
            "分类: " + category + ", 文件数量: " + files.length + ", 成功: " + (files.length - errors.size())
        );
        
        if (errors.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(results, "所有图片上传成功"));
        } else {
            return ResponseEntity.ok(ApiResponse.success(results, "部分图片上传成功"));
        }
    }

    /**
     * 上传普通文件
     */
    @PostMapping("/upload/file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "documents") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        try {
            String filePath = fileUploadService.uploadFile(file, category);
            
            // 记录操作日志
            systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "上传文件",
                "分类: " + category + ", 文件: " + file.getOriginalFilename()
            );
            
            // 获取 MinIO 访问 URL
            String fileUrl = fileUploadService.getFileUrl(filePath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath); // MinIO 对象名称
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("url", fileUrl); // MinIO 访问 URL
            
            return ResponseEntity.ok(ApiResponse.success(result, "文件上传成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 查看图片文件（从 MinIO 获取）
     * 兼容旧接口路径格式
     */
    @GetMapping("/view/**")
    public ResponseEntity<Resource> viewFile(HttpServletRequest request) {
        try {
            // 从请求 URI 中提取 MinIO 对象名称
            String requestURI = request.getRequestURI();
            String objectName;
            
            // 处理不同的URL格式
            if (requestURI.contains("/api/v1/files/view/")) {
                objectName = requestURI.substring(requestURI.indexOf("/api/v1/files/view/") + "/api/v1/files/view/".length());
            } else if (requestURI.contains("/files/view/")) {
                objectName = requestURI.substring(requestURI.indexOf("/files/view/") + "/files/view/".length());
            } else {
                logger.warn("无法识别的文件查看请求: {}", requestURI);
                return ResponseEntity.notFound().build();
            }
            
            logger.debug("查看文件，对象名称: {}", objectName);
            
            // 尝试从 MinIO 获取文件
            InputStream inputStream = minIOService.downloadFile(objectName);
            Resource resource = new InputStreamResource(inputStream);
            
            // 确定文件类型
            String contentType = determineContentType(objectName);
            
            logger.debug("返回文件，类型: {}, 对象: {}", contentType, objectName);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400") // 24小时缓存
                    .header(HttpHeaders.EXPIRES, getExpiresHeader(86400)) // 设置Expires头
                    .header(HttpHeaders.ETAG, generateETag(objectName)) // 添加ETag
                    .header(HttpHeaders.LAST_MODIFIED, getLastModifiedHeader()) // 添加Last-Modified
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*") // 允许跨域
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("查看文件失败: {}", request.getRequestURI(), e);
            // 如果文件不存在，尝试返回默认头像
            try {
                InputStream defaultInputStream = minIOService.downloadFile("avatars/default-avatar.png");
                Resource defaultResource = new InputStreamResource(defaultInputStream);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .body(defaultResource);
            } catch (Exception defaultEx) {
                logger.error("默认头像也不存在", defaultEx);
                return ResponseEntity.notFound().build();
            }
        }
    }

    /**
     * 下载文件（从 MinIO 获取）
     */
    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            // 从请求 URI 中提取 MinIO 对象名称
            String requestURI = request.getRequestURI();
            String objectName;
            
            if (requestURI.contains("/api/v1/files/download/")) {
                objectName = requestURI.substring(requestURI.indexOf("/api/v1/files/download/") + "/api/v1/files/download/".length());
            } else if (requestURI.contains("/files/download/")) {
                objectName = requestURI.substring(requestURI.indexOf("/files/download/") + "/files/download/".length());
            } else {
                return ResponseEntity.notFound().build();
            }
            
            // 从 MinIO 获取文件
            InputStream inputStream = minIOService.downloadFile(objectName);
            Resource resource = new InputStreamResource(inputStream);
            
            // 获取原始文件名
            String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{category}/{dateDir}/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String category,
            @PathVariable String dateDir,
            @PathVariable String fileName,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String filePath = category + "/" + dateDir + "/" + fileName;
        boolean deleted = fileUploadService.deleteFile(filePath);
        
        if (deleted) {
            // 记录操作日志
            systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "删除文件",
                "文件路径: " + filePath
            );
            
            return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("文件删除失败"));
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{category}/{dateDir}/{fileName}")
    public ResponseEntity<ApiResponse<FileUploadService.FileInfo>> getFileInfo(
            @PathVariable String category,
            @PathVariable String dateDir,
            @PathVariable String fileName) {
        
        String filePath = category + "/" + dateDir + "/" + fileName;
        FileUploadService.FileInfo fileInfo = fileUploadService.getFileInfo(filePath);
        
        if (fileInfo != null) {
            return ResponseEntity.ok(ApiResponse.success(fileInfo, "获取文件信息成功"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * 确定文件内容类型
     */
    private String determineContentType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * 生成 Expires 头
     */
    private String getExpiresHeader(int maxAge) {
        return Instant.now().plusSeconds(maxAge).atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
    
    /**
     * 生成 ETag
     */
    private String generateETag(String objectName) {
        return "\"" + UUID.nameUUIDFromBytes(objectName.getBytes()).toString() + "\"";
    }
    
    /**
     * 生成 Last-Modified 头
     */
    private String getLastModifiedHeader() {
        return Instant.now().atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}