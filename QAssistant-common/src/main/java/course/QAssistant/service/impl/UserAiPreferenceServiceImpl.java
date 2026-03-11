package course.QAssistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.mapper.UserAiPreferenceMapper;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.UserAiPreference;
import course.QAssistant.pojo.vo.request.UserAiPreferenceUpdateVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserAiPreferenceVO;
import course.QAssistant.service.UserAiPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author 32147
 * @description 针对表【user_ai_preference(用户AI个性化配置表)】的数据库操作Service实现
 * @createDate 2026-03-11 11:16:17
 */
@Service
@RequiredArgsConstructor
public class UserAiPreferenceServiceImpl extends ServiceImpl<UserAiPreferenceMapper, UserAiPreference>
        implements UserAiPreferenceService {

    private final RedisComponent redisComponent;

    @Override
    public R<UserAiPreferenceVO> getPreference(String token, String loginType, Long aiModelId) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        UserAiPreference preference = this.getOne(new LambdaQueryWrapper<UserAiPreference>()
                .eq(UserAiPreference::getUserUid, uid)
                .eq(UserAiPreference::getAiModelId, aiModelId));

        if (preference == null) {
            // 如果没有配置，返回默认值或者空对象，这里返回带有默认值的VO
            UserAiPreferenceVO vo = new UserAiPreferenceVO();
            vo.setAiModelId(aiModelId);
            vo.setTemperature(new BigDecimal("0.70"));
            vo.setTopP(new BigDecimal("1.00"));
            vo.setMaxTokens(2048);
            return R.ok(vo);
        }

        return R.ok(toVO(preference));
    }

    @Override
    public R<UserAiPreferenceVO> updatePreference(String token, String loginType, UserAiPreferenceUpdateVO updateVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        UserAiPreference preference = this.getOne(new LambdaQueryWrapper<UserAiPreference>()
                .eq(UserAiPreference::getUserUid, uid)
                .eq(UserAiPreference::getAiModelId, updateVO.getAiModelId()));

        if (preference == null) {
            preference = new UserAiPreference();
            preference.setUserUid(uid);
            preference.setAiModelId(updateVO.getAiModelId());
            preference.setCreatedAt(new Date());
            
            // 设置默认值，如果UpdateVO中没有提供
            preference.setTemperature(updateVO.getTemperature() != null ? updateVO.getTemperature() : new BigDecimal("0.70"));
            preference.setTopP(updateVO.getTopP() != null ? updateVO.getTopP() : new BigDecimal("1.00"));
            preference.setMaxTokens(updateVO.getMaxTokens() != null ? updateVO.getMaxTokens() : 2048);
            preference.setSystemPrompt(updateVO.getSystemPrompt()); // 可以为null
        } else {
            if (updateVO.getTemperature() != null) {
                preference.setTemperature(updateVO.getTemperature());
            }
            if (updateVO.getTopP() != null) {
                preference.setTopP(updateVO.getTopP());
            }
            if (updateVO.getMaxTokens() != null) {
                preference.setMaxTokens(updateVO.getMaxTokens());
            }
            if (updateVO.getSystemPrompt() != null) {
                preference.setSystemPrompt(updateVO.getSystemPrompt());
            }
        }
        preference.setUpdatedAt(new Date());

        this.saveOrUpdate(preference);

        return R.ok(toVO(preference));
    }

    private UserAiPreferenceVO toVO(UserAiPreference preference) {
        UserAiPreferenceVO vo = new UserAiPreferenceVO();
        BeanUtils.copyProperties(preference, vo);
        return vo;
    }
}
