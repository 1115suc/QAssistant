package course.QAssistant.minio.constant;

/**
 * MinIO 错误常量
 */
public class MinioErrorConstant {

    /**
     * 检查存储桶失败
     */
    public static final String ERROR_1001_MINIO_CHECK_BUCKET_FAIL = "检查存储桶失败";

    /**
     * 设置存储桶策略失败
     */
    public static final String ERROR_1002_MINIO_SET_POLICY_FAIL = "设置存储桶策略失败";

    /**
     * 文件上传失败
     */
    public static final String ERROR_1003_MINIO_UPLOAD_FAIL = "文件上传失败";

    /**
     * 获取预览URL失败
     */
    public static final String ERROR_1004_MINIO_GET_PREVIEW_URL_FAIL = "获取预览URL失败";


    /**
     * 下载文件失败
     */
    public static final String ERROR_1005_MINIO_DOWNLOAD_FAIL = "下载文件失败";

    /**
     * 删除文件失败
     */
    public static final String ERROR_1006_MINIO_DELETE_FILE_FAIL = "删除文件失败";

    /**
     * 批量删除文件失败
     */
    public static final String ERROR_1007_MINIO_DELETE_BATCH_FAIL = "批量删除文件失败";


    private MinioErrorConstant() {
        // 私有构造方法，防止实例化
    }
}
