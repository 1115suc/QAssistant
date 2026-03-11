package course.QAssistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.service.MiniofileService;
import course.QAssistant.mapper.MiniofileMapper;
import org.springframework.stereotype.Service;

/**
* @author 32147
* @description 针对表【minioFile(MinIO上传文件记录表)】的数据库操作Service实现
* @createDate 2026-03-11 11:16:17
*/
@Service
public class MiniofileServiceImpl extends ServiceImpl<MiniofileMapper, Miniofile>
    implements MiniofileService{

}




