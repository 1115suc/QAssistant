package course.QAssistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.enums.ResponseCode;
import course.QAssistant.pojo.po.UserAiModel;
import course.QAssistant.pojo.vo.request.UserAiModelCreateVO;
import course.QAssistant.pojo.vo.request.UserAiModelUpdateVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserAiModelVO;
import course.QAssistant.service.UserAiModelService;
import course.QAssistant.service.UserAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAiServiceImpl implements UserAiService {

    private final UserAiModelService userAiModelService;
    private final RedisComponent redisComponent;

    @Override
    public R<UserAiModelVO> createUserAiModel(String token, String loginType, UserAiModelCreateVO createVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        // 校验该用户下模型名是否已存在，防止重复创建
        long count = userAiModelService.count(new LambdaQueryWrapper<UserAiModel>()
                .eq(UserAiModel::getUserUid, uid)
                .eq(UserAiModel::getModelName, createVO.getModelName()));
        if (count > 0) {
            throw new QAWebException(ResponseCode.CODE_601.getMessage(), ResponseCode.CODE_601.getCode());
        }

        UserAiModel model = new UserAiModel();
        model.setUserUid(uid);
        model.setModelName(createVO.getModelName());
        model.setBaseUrl(createVO.getBaseUrl());
        model.setApiKey(createVO.getApiKey());
        model.setProvider(createVO.getProvider());
        model.setDescription(createVO.getDescription());
        model.setCreatedAt(new Date());
        model.setUpdatedAt(new Date());

        userAiModelService.save(model);

        UserAiModelVO vo = toVO(model);
        return R.ok(vo);
    }

    @Override
    public R<UserAiModelVO> updateUserAiModel(String token, String loginType, UserAiModelUpdateVO updateVO) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        UserAiModel model = userAiModelService.getById(updateVO.getId());
        if (model == null || !uid.equals(model.getUserUid())) {
            throw new QAWebException("模型不存在或无权限");
        }

        if (updateVO.getModelName() != null) {
            model.setModelName(updateVO.getModelName());
        }
        if (updateVO.getBaseUrl() != null) {
            model.setBaseUrl(updateVO.getBaseUrl());
        }
        if (updateVO.getApiKey() != null) {
            model.setApiKey(updateVO.getApiKey());
        }
        if (updateVO.getProvider() != null) {
            model.setProvider(updateVO.getProvider());
        }
        if (updateVO.getDescription() != null) {
            model.setDescription(updateVO.getDescription());
        }
        model.setUpdatedAt(new Date());

        userAiModelService.updateById(model);
        return R.ok(toVO(model));
    }

    @Override
    public R deleteUserAiModel(String token, String loginType, Long id) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        UserAiModel model = userAiModelService.getById(id);
        if (model == null || !uid.equals(model.getUserUid())) {
            return R.error("模型不存在或无权限");
        }
        userAiModelService.removeById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<List<UserAiModelVO>> listUserAiModels(String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        List<UserAiModel> list = userAiModelService.list(new LambdaQueryWrapper<UserAiModel>()
                .eq(UserAiModel::getUserUid, uid));

        List<UserAiModelVO> vos = list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return R.ok(vos);
    }

    private UserAiModelVO toVO(UserAiModel model) {
        UserAiModelVO vo = new UserAiModelVO();
        BeanUtils.copyProperties(model, vo);
        return vo;
    }
}

