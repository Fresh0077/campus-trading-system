package com.example.campussystem.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 存储服务
 */
@Service
public class MinIOService {

    private static final Logger logger = LoggerFactory.getLogger(MinIOService.class);

    @Autowired
    private MinioClient minioClient;
    
    @Autowired(required = false)
    private ImageCacheService imageCacheService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url-prefix}")
    private String urlPrefix;

    /**
     * 初始化 MinIO Bucket（如果不存在则创建）
     */
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                logger.info("MinIO Bucket 创建成功: {}", bucketName);
            } else {
                logger.info("MinIO Bucket 已存在: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("MinIO Bucket 初始化失败", e);
            throw new RuntimeException("MinIO Bucket 初始化失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到 MinIO
     * 
     * @param file 文件
     * @param category 分类（avatars, products, categories, documents 等）
     * @return 文件在 MinIO 中的对象名称（objectName）
     */
    public String uploadFile(MultipartFile file, String category) {
        try {
            // 确保 Bucket 存在
            initBucket();
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension;
            
            // 构建对象名称：category/filename
            String objectName = category + "/" + fileName;
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            logger.info("文件上传到 MinIO 成功: {}", objectName);
            return objectName;
            
        } catch (Exception e) {
            logger.error("文件上传到 MinIO 失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传图片文件（带图片处理）
     * 
     * @param inputStream 图片输入流
     * @param category 分类
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param size 文件大小
     * @return 文件在 MinIO 中的对象名称
     */
    public String uploadImage(InputStream inputStream, String category, String fileName, String contentType, long size) {
        try {
            // 确保 Bucket 存在
            initBucket();
            
            // 构建对象名称：category/filename
            String objectName = category + "/" + fileName;
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            
            logger.info("图片上传到 MinIO 成功: {}", objectName);
            return objectName;
            
        } catch (Exception e) {
            logger.error("图片上传到 MinIO 失败", e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件的访问 URL（预签名 URL，7天有效期）
     * 
     * @param objectName 对象名称
     * @return 文件访问 URL
     */
    public String getFileUrl(String objectName) {
        try {
            // 如果 objectName 已经是完整 URL，直接返回
            if (objectName != null && (objectName.startsWith("http://") || objectName.startsWith("https://"))) {
                return objectName;
            }
            
            // 生成预签名 URL（7天有效期）
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
            
            return url;
        } catch (Exception e) {
            logger.error("获取文件 URL 失败: {}", objectName, e);
            // 如果生成预签名 URL 失败，返回拼接的 URL
            return urlPrefix + "/" + objectName;
        }
    }

    /**
     * 获取文件的公共访问 URL（如果 Bucket 是公开的）
     * 
     * @param objectName 对象名称
     * @return 文件访问 URL
     */
    public String getPublicUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        
        // 如果已经是完整 URL，直接返回
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }
        
        // 尝试从缓存获取
        if (imageCacheService != null) {
            String cachedUrl = imageCacheService.getCachedImageUrl(objectName);
            if (cachedUrl != null) {
                return cachedUrl;
            }
        }
        
        // 生成URL并缓存
        String publicUrl = urlPrefix + "/" + objectName;
        
        if (imageCacheService != null) {
            imageCacheService.cacheImageUrl(objectName, publicUrl);
        }
        
        return publicUrl;
    }

    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    public boolean deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            logger.info("文件从 MinIO 删除成功: {}", objectName);
            return true;
        } catch (Exception e) {
            logger.error("文件从 MinIO 删除失败: {}", objectName, e);
            return false;
        }
    }

    /**
     * 从 MinIO 下载文件
     * 
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("从 MinIO 下载文件失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

