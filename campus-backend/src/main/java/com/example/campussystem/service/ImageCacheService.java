package com.example.campussystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 图片缓存服务
 * 使用Redis缓存图片URL和元数据，提高访问速度
 */
@Service
public class ImageCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageCacheService.class);
    
    private static final String CACHE_PREFIX = "image_cache:";
    private static final String METADATA_PREFIX = "image_meta:";
    private static final long DEFAULT_CACHE_DURATION = 24; // 小时
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 缓存图片URL
     * @param objectName MinIO对象名称
     * @param imageUrl 图片URL
     */
    public void cacheImageUrl(String objectName, String imageUrl) {
        try {
            String key = CACHE_PREFIX + objectName;
            redisTemplate.opsForValue().set(key, imageUrl, DEFAULT_CACHE_DURATION, TimeUnit.HOURS);
            logger.debug("缓存图片URL: {} -> {}", objectName, imageUrl);
        } catch (Exception e) {
            logger.warn("缓存图片URL失败: {}", objectName, e);
        }
    }
    
    /**
     * 获取缓存的图片URL
     * @param objectName MinIO对象名称
     * @return 缓存的图片URL，如果不存在返回null
     */
    public String getCachedImageUrl(String objectName) {
        try {
            String key = CACHE_PREFIX + objectName;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("命中缓存图片URL: {}", objectName);
                return cached.toString();
            }
        } catch (Exception e) {
            logger.warn("获取缓存图片URL失败: {}", objectName, e);
        }
        return null;
    }
    
    /**
     * 缓存图片元数据
     * @param objectName MinIO对象名称
     * @param metadata 元数据（如文件大小、尺寸等）
     */
    public void cacheImageMetadata(String objectName, Object metadata) {
        try {
            String key = METADATA_PREFIX + objectName;
            redisTemplate.opsForValue().set(key, metadata, DEFAULT_CACHE_DURATION, TimeUnit.HOURS);
            logger.debug("缓存图片元数据: {}", objectName);
        } catch (Exception e) {
            logger.warn("缓存图片元数据失败: {}", objectName, e);
        }
    }
    
    /**
     * 获取缓存的图片元数据
     * @param objectName MinIO对象名称
     * @return 缓存的元数据，如果不存在返回null
     */
    public Object getCachedImageMetadata(String objectName) {
        try {
            String key = METADATA_PREFIX + objectName;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("命中缓存图片元数据: {}", objectName);
                return cached;
            }
        } catch (Exception e) {
            logger.warn("获取缓存图片元数据失败: {}", objectName, e);
        }
        return null;
    }
    
    /**
     * 删除图片缓存
     * @param objectName MinIO对象名称
     */
    public void deleteImageCache(String objectName) {
        try {
            String urlKey = CACHE_PREFIX + objectName;
            String metaKey = METADATA_PREFIX + objectName;
            
            redisTemplate.delete(urlKey);
            redisTemplate.delete(metaKey);
            
            logger.debug("删除图片缓存: {}", objectName);
        } catch (Exception e) {
            logger.warn("删除图片缓存失败: {}", objectName, e);
        }
    }
    
    /**
     * 预热缓存 - 批量缓存图片URL
     * @param objectNames MinIO对象名称列表
     * @param minioService MinIO服务
     */
    public void warmupCache(java.util.List<String> objectNames, MinIOService minioService) {
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }
        
        logger.info("开始预热图片缓存，数量: {}", objectNames.size());
        
        objectNames.parallelStream().forEach(objectName -> {
            try {
                String cachedUrl = getCachedImageUrl(objectName);
                if (cachedUrl == null) {
                    String imageUrl = minioService.getPublicUrl(objectName);
                    cacheImageUrl(objectName, imageUrl);
                }
            } catch (Exception e) {
                logger.warn("预热缓存失败: {}", objectName, e);
            }
        });
        
        logger.info("图片缓存预热完成");
    }
}