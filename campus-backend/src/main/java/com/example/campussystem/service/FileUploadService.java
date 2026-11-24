package com.example.campussystem.service;

import com.example.campussystem.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务（使用 MinIO 存储）
 */
@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Autowired
    private MinIOService minIOService;

    @Value("${file.upload.max-size:20971520}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,webp}")
    private String allowedTypes;

    // 支持的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    // 最大图片尺寸
    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1080;
    
    // 图片质量设置 (0.0-1.0)
    private static final float JPEG_QUALITY = 0.85f;
    private static final float WEBP_QUALITY = 0.80f;

    /**
     * 上传图片文件（带图片处理：压缩、调整尺寸）
     */
    public String uploadImage(MultipartFile file, String category) {
        // 验证文件
        validateFile(file);
        
        // 验证图片格式
        validateImageFile(file);
        
        try {
            // 获取文件字节数组（避免多次读取输入流）
            byte[] fileBytes = file.getBytes();
            
            // 处理图片（压缩、调整尺寸）
            BufferedImage processedImage = processImage(fileBytes);
            
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            String fileExtension = getFileExtension(file.getOriginalFilename());
            
            // 将 BufferedImage 转换为 InputStream，使用优化的压缩
            byte[] imageBytes = compressImageOptimized(processedImage, fileBytes, fileExtension);
            InputStream imageInputStream = new ByteArrayInputStream(imageBytes);
            
            // 确定 Content-Type
            String contentType = getContentType(fileExtension);
            
            // 上传到 MinIO
            String objectName = minIOService.uploadImage(
                    imageInputStream, 
                    category, 
                    fileName, 
                    contentType, 
                    imageBytes.length
            );
            
            logger.info("图片上传到 MinIO 成功: {}", objectName);
            return objectName;
            
        } catch (IOException e) {
            logger.error("图片处理或上传失败", e);
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传普通文件（不处理，直接上传）
     */
    public String uploadFile(MultipartFile file, String category) {
        // 验证文件
        validateFile(file);
        
        try {
            // 上传到 MinIO
            String objectName = minIOService.uploadFile(file, category);
            
            logger.info("文件上传到 MinIO 成功: {}", objectName);
            return objectName;
            
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String objectName) {
        try {
            return minIOService.deleteFile(objectName);
        } catch (Exception e) {
            logger.error("文件删除失败: {}", objectName, e);
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String objectName) {
        try {
            // MinIO 不直接提供文件信息查询，这里返回基本信息
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(objectName);
            
            // 从 objectName 提取文件名
            if (objectName != null && objectName.contains("/")) {
                String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
                fileInfo.setFileName(fileName);
            } else {
                fileInfo.setFileName(objectName);
            }
            
            // 判断是否为图片
            if (isImageFile(objectName)) {
                // 可以尝试从 MinIO 下载文件获取图片尺寸，但这里简化处理
                fileInfo.setWidth(null);
                fileInfo.setHeight(null);
            }
            
            return fileInfo;
        } catch (Exception e) {
            logger.error("获取文件信息失败: {}", objectName, e);
            return null;
        }
    }

    /**
     * 获取文件的访问 URL
     */
    public String getFileUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        return minIOService.getPublicUrl(objectName);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        
        if (!ALLOWED_IMAGE_TYPES.contains(fileExtension.toLowerCase())) {
            throw new BusinessException("不支持的图片格式，支持的格式: " + String.join(", ", ALLOWED_IMAGE_TYPES));
        }
        
        // 验证文件内容是否为图片
        try {
            // 使用 getBytes() 获取字节数组，避免输入流不支持 reset() 的问题
            byte[] fileBytes = file.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new BusinessException("文件不是有效的图片格式");
            }
        } catch (IOException e) {
            logger.error("读取图片文件失败", e);
            throw new BusinessException("读取图片文件失败: " + e.getMessage());
        }
    }

    /**
     * 处理图片（压缩、调整尺寸）
     */
    private BufferedImage processImage(byte[] fileBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
        BufferedImage originalImage = ImageIO.read(bais);
        
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 如果图片尺寸在限制范围内，直接返回
        if (originalWidth <= MAX_IMAGE_WIDTH && originalHeight <= MAX_IMAGE_HEIGHT) {
            return originalImage;
        }
        
        // 计算缩放比例
        double scaleWidth = (double) MAX_IMAGE_WIDTH / originalWidth;
        double scaleHeight = (double) MAX_IMAGE_HEIGHT / originalHeight;
        double scale = Math.min(scaleWidth, scaleHeight);
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        // 创建缩放后的图片
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        logger.info("图片已压缩: {}x{} -> {}x{}", originalWidth, originalHeight, newWidth, newHeight);
        
        return scaledImage;
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + fileExtension;
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

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String filePath) {
        String extension = getFileExtension(filePath);
        return ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase());
    }

    /**
     * 优化的图片压缩方法
     */
    private byte[] compressImageOptimized(BufferedImage processedImage, byte[] originalBytes, String extension) throws IOException {
        try {
            // 使用简化的压缩方法
            byte[] compressedBytes = com.example.campussystem.util.ImageCompressor.compressImageSimple(
                originalBytes, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
            
            logger.info("图片压缩完成: 原始大小={}KB, 压缩后大小={}KB, 压缩比={:.1f}%", 
                       originalBytes.length / 1024, 
                       compressedBytes.length / 1024,
                       (1.0 - (double)compressedBytes.length / originalBytes.length) * 100);
            
            return compressedBytes;
            
        } catch (Exception e) {
            logger.warn("图片压缩失败，使用原始图片: {}", e.getMessage());
            // 如果压缩失败，返回原始图片
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage, extension, baos);
            return baos.toByteArray();
        }
    }
    
    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 文件信息类
     */
    public static class FileInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private LocalDateTime lastModified;
        private Integer width;
        private Integer height;

        // Getters and Setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public LocalDateTime getLastModified() {
            return lastModified;
        }

        public void setLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }
    }
}
