package course.QAssistant.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.minio.enums.MinioFileTypeEnum;
import course.QAssistant.minio.model.FileUploadResponse;
import course.QAssistant.minio.properties.MinIOConfigProperties;
import course.QAssistant.minio.service.MinIOFileService;
import course.QAssistant.pojo.dto.FileDownloadDTO;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.pojo.vo.request.FileDeleteVO;
import course.QAssistant.pojo.vo.request.FileDownloadVO;
import course.QAssistant.pojo.vo.request.FileUploadVO;
import course.QAssistant.pojo.vo.response.FileUploadRespVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.FileService;
import course.QAssistant.service.MiniofileService;
import course.QAssistant.util.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinIOFileService minIOFileService;
    private final MinIOConfigProperties minIOConfigProperties;
    private final MiniofileService miniofileService;
    private final RedisComponent redisComponent;
    private final IdWorker idWorker;

    @Override
    public R<FileUploadRespVO> upload(String token, String loginType, FileUploadVO uploadVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        MinioFileTypeEnum typeEnum = MinioFileTypeEnum.from(uploadVO.getFileType());
        FileUploadResponse resp = minIOFileService.uploadFile(uploadVO.getFile(), null, uid, typeEnum);

        Miniofile record = new Miniofile();
        record.setId(idWorker.nextId());
        record.setUid(uid);
        record.setBucket(minIOConfigProperties.getBucketName());
        record.setObjectName(resp.getFileId());
        record.setMinioPath(record.getBucket() + "/" + record.getObjectName());
        record.setFileName(resp.getOriginalName());
        record.setFileExt(extractExt(uploadVO.getFile() != null ? uploadVO.getFile().getOriginalFilename() : null));
        record.setContentType(null);
        record.setFileSize(resp.getFileSize());
        record.setDeleted(0);
        record.setDeleteTime(null);
        record.setCreateTime(new Date(System.currentTimeMillis()));
        miniofileService.save(record);

        FileUploadRespVO vo = new FileUploadRespVO();
        vo.setId(record.getId());
        vo.setBucket(record.getBucket());
        vo.setObjectName(record.getObjectName());
        vo.setMinioPath(record.getMinioPath());
        vo.setFileName(record.getFileName());
        vo.setFileSize(record.getFileSize());
        vo.setMimeType(resp.getMimeType());
        vo.setFileUrl(resp.getFileUrl());
        vo.setUploadTime(resp.getUploadTime());

        return R.ok(vo);
    }

    @Override
    public FileDownloadDTO download(String token, String loginType, FileDownloadVO downloadVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        Miniofile record = miniofileService.getOne(new LambdaQueryWrapper<Miniofile>()
                .eq(Miniofile::getId, downloadVO.getId())
                .eq(Miniofile::getUid, uid)
                .eq(Miniofile::getDeleted, 0));

        if (ObjectUtil.isNull(record)) {
            throw new QAWebException("文件不存在或无权限");
        }

        InputStream in = minIOFileService.downloadFile(record.getBucket(), record.getObjectName());
        String filename = StrUtil.blankToDefault(record.getFileName(), "download");
        return new FileDownloadDTO(in, filename);
    }

    @Override
    public R delete(String token, String loginType, FileDeleteVO deleteVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        Miniofile record = miniofileService.getOne(new LambdaQueryWrapper<Miniofile>()
                .eq(Miniofile::getId, deleteVO.getId())
                .eq(Miniofile::getUid, uid)
                .eq(Miniofile::getDeleted, 0));

        if (ObjectUtil.isNull(record)) {
            return R.error("文件不存在或无权限");
        }

        minIOFileService.removeFile(record.getBucket(), record.getObjectName());
        record.setDeleted(1);
        record.setDeleteTime(new Date(System.currentTimeMillis()));
        miniofileService.updateById(record);
        return R.ok("删除成功");
    }

    private String extractExt(String filename) {
        if (StrUtil.isBlank(filename)) {
            return null;
        }
        String fn = filename;
        int slash = Math.max(fn.lastIndexOf('/'), fn.lastIndexOf('\\'));
        if (slash >= 0 && slash < fn.length() - 1) {
            fn = fn.substring(slash + 1);
        }
        int dot = fn.lastIndexOf('.');
        if (dot >= 0 && dot < fn.length() - 1) {
            return fn.substring(dot + 1);
        }
        return null;
    }
}

