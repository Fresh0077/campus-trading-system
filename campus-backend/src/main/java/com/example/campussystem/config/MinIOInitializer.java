package com.example.campussystem.config;

import com.example.campussystem.service.MinIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MinIO 初始化器
 * 应用启动时自动初始化 MinIO Bucket
 */
@Component
public class MinIOInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MinIOInitializer.class);

    @Autowired
    private MinIOService minIOService;

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("开始初始化 MinIO...");
            minIOService.initBucket();
            logger.info("MinIO 初始化完成");
        } catch (Exception e) {
            logger.error("MinIO 初始化失败", e);
            // 不抛出异常，避免影响应用启动
            // 生产环境可以考虑根据配置决定是否必须成功
        }
    }
}

