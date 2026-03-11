package course.QAssistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.pojo.po.UserAiModel;
import course.QAssistant.service.UserAiModelService;
import course.QAssistant.mapper.UserAiModelMapper;
import org.springframework.stereotype.Service;

/**
* @author 32147
* @description 针对表【user_ai_model(用户自定义AI模型表)】的数据库操作Service实现
* @createDate 2026-03-11 11:16:17
*/
@Service
public class UserAiModelServiceImpl extends ServiceImpl<UserAiModelMapper, UserAiModel>
    implements UserAiModelService{

}




