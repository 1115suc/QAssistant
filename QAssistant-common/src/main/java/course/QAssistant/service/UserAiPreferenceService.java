package course.QAssistant.service;

import course.QAssistant.pojo.po.UserAiPreference;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QAssistant.pojo.vo.request.UserAiPreferenceUpdateVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserAiPreferenceVO;

public interface UserAiPreferenceService extends IService<UserAiPreference> {

    /**
     * 获取用户AI个性化配置
     *
     * @param token     用户Token
     * @param loginType 登录方式
     * @param aiModelId AI模型ID
     * @return 配置信息
     */
    R<UserAiPreferenceVO> getPreference(String token, String loginType, Long aiModelId);

    /**
     * 更新或创建用户AI个性化配置
     *
     * @param token     用户Token
     * @param loginType 登录方式
     * @param updateVO  配置更新信息
     * @return 更新后的配置信息
     */
    R<UserAiPreferenceVO> updatePreference(String token, String loginType, UserAiPreferenceUpdateVO updateVO);
}
