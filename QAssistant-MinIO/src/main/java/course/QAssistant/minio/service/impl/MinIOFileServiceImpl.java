package course.QAssistant.minio.service.impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import course.QAssistant.minio.constant.MinioErrorConstant;
import course.QAssistant.minio.properties.MinIOConfigProperties;
import course.QAssistant.minio.exception.MinioException;
import course.QAssistant.minio.model.FileUploadResponse;
import course.QAssistant.minio.service.MinIOFileService;
import course.QAssistant.minio.util.MinioFileStorageUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * MinIO文件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOFileServiceImpl implements MinIOFileService {

    private final MinioClient minioClient;
    private final MinIOConfigProperties minIOConfigProperties;
    private final MinioFileStorageUtil minioFileStorageUtil;

    /**
     * 检查存储桶是否存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     */
    public void checkBucket(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("检查存储桶失败: {}", bucketName, e);
            throw new MinioException(MinioErrorConstant.ERROR_1001_MINIO_CHECK_BUCKET_FAIL, e);
        }
    }

    /**
     * 设置存储桶策略
     *
     * @param bucketName 存储桶名称
     * @param publicRead true为公开读取，false为私有
     */
    public void setBucketPolicy(String bucketName, boolean publicRead) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minIOConfigProperties.getBucketName();
        }

        try {
            String policy;
            if (publicRead) {
                // 公开读取策略
                policy = String.format("{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}", bucketName);
                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
                );
            } else {
                // 删除策略使其变为私有
                minioClient.deleteBucketPolicy(
                        DeleteBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("设置存储桶策略失败: {}", bucketName, e);
            throw new MinioException(MinioErrorConstant.ERROR_1002_MINIO_SET_POLICY_FAIL, e);
        }
    }

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "png", "gif", "webp", "jpeg",
            "pdf", "doc", "docx", "md", "txt", "xls", "xlsx", "ppt", "pptx",
            "html", "css", "js",
            "mp4", "avi", "mov", "wmv", "flv"
    ));

    /**
     * 上传通用文件
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @return FileUploadResponse响应对象
     */
    public FileUploadResponse uploadFile(MultipartFile file, String bucketName) {
        return uploadFile(file, bucketName, null);
    }

    /**
     * 上传通用文件
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @param customPath 自定义文件路径
     * @return FileUploadResponse响应对象
     */
    public FileUploadResponse uploadFile(MultipartFile file, String bucketName, String customPath) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minIOConfigProperties.getBucketName();
        }
        checkBucket(bucketName);

        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalFilename);
        if (StrUtil.isBlank(suffix) || !ALLOWED_EXTENSIONS.contains(suffix.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型: " + suffix);
        }

        String contentType = file.getContentType();
        if (StrUtil.isBlank(contentType)) {
            contentType = "application/octet-stream";
        }

        // 生成唯一的对象名称
        String fileName = IdUtil.simpleUUID() + "." + suffix;
        String objectName = minioFileStorageUtil.buildFilePath(customPath, fileName);

        try {
            // 读取字节以避免流耗尽问题并计算MD5
            byte[] bytes = file.getBytes();
            String md5 = SecureUtil.md5(new ByteArrayInputStream(bytes));

            Map<String, String> metadata = new HashMap<>();
            // 仅在元数据中存储安全字符
            // metadata.put("originalName", originalFilename != null ? originalFilename : "");
            metadata.put("md5", md5);
            metadata.put("fileSize", String.valueOf(file.getSize()));

            InputStream inputStream = new ByteArrayInputStream(bytes);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .userMetadata(metadata)
                            .build()
            );

            String fileUrl = getPreviewUrl(bucketName, objectName);

            return FileUploadResponse.builder()
                    .fileId(objectName)
                    .fileName(objectName)
                    .originalName(originalFilename)
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .mimeType(contentType)
                    .uploadTime(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("文件上传失败: {}", originalFilename, e);
            throw new MinioException(MinioErrorConstant.ERROR_1003_MINIO_UPLOAD_FAIL, e);
        }
    }

    /**
     * 专门上传图片
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称
     * @param thumbnail  是否生成缩略图
     * @return FileUploadResponse响应对象
     */
    public FileUploadResponse uploadImage(MultipartFile file, String bucketName, boolean thumbnail) {
        return uploadImage(file, bucketName, null, thumbnail);
    }

    /**
     * 专门上传图片
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称
     * @param customPath 自定义路径
     * @param thumbnail  是否生成缩略图
     * @return FileUploadResponse响应对象
     */
    public FileUploadResponse uploadImage(MultipartFile file, String bucketName, String customPath, boolean thumbnail) {
        // 验证图片格式
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalFilename);
        if (!StrUtil.equalsAnyIgnoreCase(suffix, "jpg", "png", "gif", "webp", "jpeg")) {
            throw new IllegalArgumentException("无效的图片格式。支持: jpg, png, gif, webp");
        }

        // 验证大小（5MB限制）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片大小超过5MB限制");
        }

        FileUploadResponse response = uploadFile(file, bucketName, customPath);

        if (thumbnail) {
            try {
                // 生成缩略图
                // 使用Hutool ImgUtil
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImgUtil.scale(file.getInputStream(), out, 0.5f); // 缩放到50%
                byte[] thumbnailBytes = out.toByteArray();

                String thumbnailObjectName = response.getFileId().replace("." + suffix, "_thumb." + suffix);

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName != null ? bucketName : minIOConfigProperties.getBucketName())
                                .object(thumbnailObjectName)
                                .stream(new ByteArrayInputStream(thumbnailBytes), thumbnailBytes.length, -1)
                                .contentType(file.getContentType())
                                .build()
                );

                log.info("缩略图已生成: {}", thumbnailObjectName);
            } catch (Exception e) {
                log.warn("缩略图生成失败: {}", originalFilename, e);
                // 如果缩略图生成失败，不要使整个上传失败
            }
        }

        return response;
    }

    /**
     * 获取预览URL（临时签名URL）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return URL字符串
     */
    public String getPreviewUrl(String bucketName, String objectName) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minIOConfigProperties.getBucketName();
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS) // 1小时过期
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预览URL失败: {}", objectName, e);
            throw new MinioException(MinioErrorConstant.ERROR_1004_MINIO_GET_PREVIEW_URL_FAIL, e);
        }
    }

    /**
     * 获取永久URL（如果存储桶是公开的）
     */
    public String getPublicUrl(String bucketName, String objectName) {
         if (StrUtil.isBlank(bucketName)) {
            bucketName = minIOConfigProperties.getBucketName();
        }
        return String.format("%s/%s/%s", minIOConfigProperties.getEndpoint(), bucketName, objectName);
    }

    /**
     * 下载文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minIOConfigProperties.getBucketName();
        }
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件失败: {}", objectName, e);
            throw new MinioException(MinioErrorConstant.ERROR_1005_MINIO_DOWNLOAD_FAIL, e);
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public void removeFile(String bucketName, String objectName) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minIOConfigProperties.getBucketName();
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
            throw new MinioException(MinioErrorConstant.ERROR_1006_MINIO_DELETE_FILE_FAIL, e);
        }
    }

    /**
     * 批量删除文件
     *
     * @param bucketName  存储桶名称
     * @param objectNames 对象名称列表
     */
    public void removeFiles(String bucketName, List<String> objectNames) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = minIOConfigProperties.getBucketName();
        }
        try {
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build()
            );

            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("删除对象出错 " + error.objectName() + "; " + error.message());
            }
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new MinioException(MinioErrorConstant.ERROR_1007_MINIO_DELETE_BATCH_FAIL, e);
        }
    }
}
