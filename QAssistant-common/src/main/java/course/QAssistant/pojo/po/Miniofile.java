package course.QAssistant.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * MinIO上传文件记录表
 * @TableName minioFile
 */
@TableName(value ="minioFile")
public class Miniofile {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 上传用户uid(sys_user.uid,冗余)
     */
    private String uid;

    /**
     * MinIO bucket
     */
    private String bucket;

    /**
     * MinIO objectName(对象key)
     */
    private String objectName;

    /**
     * MinIO存储路径(通常为bucket/objectName)
     */
    private String minioPath;

    /**
     * 上传时文件名(原始文件名)
     */
    private String fileName;

    /**
     * 文件扩展名(不含点)
     */
    private String fileExt;

    /**
     * 文件类型(MIME)
     */
    private Integer contentType;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 是否删除(0未删除；1已删除)
     */
    private Integer deleted;

    /**
     * 删除时间
     */
    private Date deleteTime;

    /**
     * 创建时间(上传时间)
     */
    private Date createTime;

    /**
     * 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 上传用户uid(sys_user.uid,冗余)
     */
    public String getUid() {
        return uid;
    }

    /**
     * 上传用户uid(sys_user.uid,冗余)
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * MinIO bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * MinIO bucket
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * MinIO objectName(对象key)
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * MinIO objectName(对象key)
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * MinIO存储路径(通常为bucket/objectName)
     */
    public String getMinioPath() {
        return minioPath;
    }

    /**
     * MinIO存储路径(通常为bucket/objectName)
     */
    public void setMinioPath(String minioPath) {
        this.minioPath = minioPath;
    }

    /**
     * 上传时文件名(原始文件名)
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 上传时文件名(原始文件名)
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 文件扩展名(不含点)
     */
    public String getFileExt() {
        return fileExt;
    }

    /**
     * 文件扩展名(不含点)
     */
    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    /**
     * 文件类型(MIME)
     */
    public Integer getContentType() {
        return contentType;
    }

    /**
     * 文件类型(MIME)
     */
    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    /**
     * 文件大小(字节)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 文件大小(字节)
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 是否删除(0未删除；1已删除)
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 是否删除(0未删除；1已删除)
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    /**
     * 删除时间
     */
    public Date getDeleteTime() {
        return deleteTime;
    }

    /**
     * 删除时间
     */
    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    /**
     * 创建时间(上传时间)
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间(上传时间)
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Miniofile other = (Miniofile) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUid() == null ? other.getUid() == null : this.getUid().equals(other.getUid()))
            && (this.getBucket() == null ? other.getBucket() == null : this.getBucket().equals(other.getBucket()))
            && (this.getObjectName() == null ? other.getObjectName() == null : this.getObjectName().equals(other.getObjectName()))
            && (this.getMinioPath() == null ? other.getMinioPath() == null : this.getMinioPath().equals(other.getMinioPath()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileExt() == null ? other.getFileExt() == null : this.getFileExt().equals(other.getFileExt()))
            && (this.getContentType() == null ? other.getContentType() == null : this.getContentType().equals(other.getContentType()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getDeleted() == null ? other.getDeleted() == null : this.getDeleted().equals(other.getDeleted()))
            && (this.getDeleteTime() == null ? other.getDeleteTime() == null : this.getDeleteTime().equals(other.getDeleteTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUid() == null) ? 0 : getUid().hashCode());
        result = prime * result + ((getBucket() == null) ? 0 : getBucket().hashCode());
        result = prime * result + ((getObjectName() == null) ? 0 : getObjectName().hashCode());
        result = prime * result + ((getMinioPath() == null) ? 0 : getMinioPath().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileExt() == null) ? 0 : getFileExt().hashCode());
        result = prime * result + ((getContentType() == null) ? 0 : getContentType().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
        result = prime * result + ((getDeleteTime() == null) ? 0 : getDeleteTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", uid=").append(uid);
        sb.append(", bucket=").append(bucket);
        sb.append(", objectName=").append(objectName);
        sb.append(", minioPath=").append(minioPath);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileExt=").append(fileExt);
        sb.append(", contentType=").append(contentType);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", deleted=").append(deleted);
        sb.append(", deleteTime=").append(deleteTime);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}