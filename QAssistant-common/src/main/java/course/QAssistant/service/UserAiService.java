package course.QAssistant.service;

import course.QAssistant.pojo.vo.request.UserAiModelCreateVO;
import course.QAssistant.pojo.vo.request.UserAiModelUpdateVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserAiModelVO;

import java.util.List;

public interface UserAiService {

    R<UserAiModelVO> createUserAiModel(String token, String loginType, UserAiModelCreateVO createVO);

    R<UserAiModelVO> updateUserAiModel(String token, String loginType, UserAiModelUpdateVO updateVO);

    R deleteUserAiModel(String token, String loginType, Long id);

    R<List<UserAiModelVO>> listUserAiModels(String token, String loginType);
}

