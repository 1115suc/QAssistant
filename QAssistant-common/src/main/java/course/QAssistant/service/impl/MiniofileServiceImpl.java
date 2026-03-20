package course.QAssistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.service.MiniofileService;
import course.QAssistant.mapper.MiniofileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class MiniofileServiceImpl extends ServiceImpl<MiniofileMapper, Miniofile>
    implements MiniofileService{

}




