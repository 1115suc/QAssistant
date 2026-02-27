package course.QAssistant.minio.service;

import course.QAssistant.minio.model.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * MinIO 文件服务接口
 */
public interface MinIOFileService {

    /**
     * 检查存储桶是否存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     */
    void checkBucket(String bucketName);

    /**
     * 设置存储桶策略
     *
     * @param bucketName 存储桶名称
     * @param publicRead true为公开读取，false为私有
     */
    void setBucketPolicy(String bucketName, boolean publicRead);

    /**
     * 上传通用文件
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadFile(MultipartFile file, String bucketName);

    /**
     * 上传通用文件
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @param customPath 自定义文件路径
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadFile(MultipartFile file, String bucketName, String customPath);

    /**
     * 专门上传图片
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称
     * @param thumbnail  是否生成缩略图
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadImage(MultipartFile file, String bucketName, boolean thumbnail);

    /**
     * 专门上传图片
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称
     * @param customPath 自定义路径
     * @param thumbnail  是否生成缩略图
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadImage(MultipartFile file, String bucketName, String customPath, boolean thumbnail);

    /**
     * 获取预览URL（临时签名URL）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return URL字符串
     */
    String getPreviewUrl(String bucketName, String objectName);

    /**
     * 获取永久URL（如果存储桶是公开的）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return URL字符串
     */
    String getPublicUrl(String bucketName, String objectName);

    /**
     * 下载文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String bucketName, String objectName);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    void removeFile(String bucketName, String objectName);

    /**
     * 批量删除文件
     *
     * @param bucketName  存储桶名称
     * @param objectNames 对象名称列表
     */
    void removeFiles(String bucketName, List<String> objectNames);
}
