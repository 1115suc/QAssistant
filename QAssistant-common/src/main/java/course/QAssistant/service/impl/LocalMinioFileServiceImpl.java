package course.QAssistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.mapper.MiniofileMapper;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.service.LocalMinioFileService;
import org.springframework.stereotype.Service;

@Service
public class LocalMinioFileServiceImpl extends ServiceImpl<MiniofileMapper, Miniofile>
        implements LocalMinioFileService {

}
