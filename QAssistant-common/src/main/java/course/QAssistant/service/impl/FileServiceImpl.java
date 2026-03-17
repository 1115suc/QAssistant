package course.QAssistant.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import course.QAssistant.pojo.vo.request.FileListQueryVO;
import course.QAssistant.pojo.vo.request.FileUploadVO;
import course.QAssistant.pojo.vo.request.FileUrlVO;
import course.QAssistant.pojo.vo.response.FileInfoRespVO;
import course.QAssistant.pojo.vo.response.FileUploadRespVO;
import course.QAssistant.pojo.vo.response.FileUrlRespVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.FileService;
import course.QAssistant.service.MiniofileService;
import course.QAssistant.util.IdWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public R<List<FileInfoRespVO>> listMyFiles(String token, String loginType, FileListQueryVO queryVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        LambdaQueryWrapper<Miniofile> wrapper = new LambdaQueryWrapper<Miniofile>()
                .eq(Miniofile::getUid, uid)
                .eq(Miniofile::getDeleted, 0)
                .like(StrUtil.isNotBlank(queryVO.getFileName()), Miniofile::getFileName, queryVO.getFileName())
                .orderByDesc(Miniofile::getCreateTime);

        int pageNum = queryVO.getPageNum() == null ? 1 : queryVO.getPageNum();
        int pageSize = queryVO.getPageSize() == null ? 20 : queryVO.getPageSize();
        IPage<Miniofile> page = miniofileService.page(new Page<>(pageNum, pageSize), wrapper);

        List<FileInfoRespVO> list = page.getRecords().stream().map(record -> {
            FileInfoRespVO vo = new FileInfoRespVO();
            vo.setId(record.getId());
            vo.setFileName(record.getFileName());
            vo.setFileExt(record.getFileExt());
            vo.setFileSize(record.getFileSize());
            vo.setMinioPath(record.getMinioPath());
            vo.setCreateTime(record.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        return R.ok(list);
    }

    @Override
    public R<FileUrlRespVO> getFileUrl(String token, String loginType, FileUrlVO urlVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        Miniofile record = miniofileService.getOne(new LambdaQueryWrapper<Miniofile>()
                .eq(Miniofile::getId, urlVO.getId())
                .eq(Miniofile::getUid, uid)
                .eq(Miniofile::getDeleted, 0));

        if (ObjectUtil.isNull(record)) {
            throw new QAWebException("文件不存在或无权限");
        }

        String urlType = urlVO.getUrlType();
        String url;
        if ("preview".equalsIgnoreCase(urlType)) {
            url = minIOFileService.getPreviewUrl(record.getBucket(), record.getObjectName());
        } else if ("public".equalsIgnoreCase(urlType)) {
            url = minIOFileService.getPublicUrl(record.getBucket(), record.getObjectName());
        } else {
            throw new QAWebException("urlType参数非法，仅支持 preview 或 public");
        }

        FileUrlRespVO vo = new FileUrlRespVO();
        vo.setId(record.getId());
        vo.setFileName(record.getFileName());
        vo.setUrlType(urlType.toLowerCase());
        vo.setUrl(url);

        return R.ok(vo);
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
