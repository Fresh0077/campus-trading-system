package com.example.campussystem.config;

import io.minio.MinioClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * MinIO 配置类
 */
@Configuration
public class MinIOConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.secure:false}")
    private boolean secure;
    
    @Value("${minio.connection-timeout:30000}")
    private int connectionTimeout;
    
    @Value("${minio.connection-acquire-timeout:60000}")
    private int connectionAcquireTimeout;
    
    @Value("${minio.connection-pool-size:20}")
    private int connectionPoolSize;
    
    @Value("${minio.read-timeout:60000}")
    private int readTimeout;
    
    @Value("${minio.write-timeout:60000}")
    private int writeTimeout;

    /**
     * 创建优化的 HTTP 客户端
     */
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(connectionPoolSize, 5, TimeUnit.MINUTES))
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 创建 MinIO 客户端
     */
    @Bean
    public MinioClient minioClient(OkHttpClient httpClient) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .httpClient(httpClient)
                .build();
    }
}

