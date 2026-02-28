package course.QAssistant.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.constant.RedisConstant;
import course.QAssistant.constant.TimeConstant;
import course.QAssistant.enums.*;
import course.QAssistant.exception.QAException;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.mapper.SysUserMapper;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.SysUser;
import course.QAssistant.pojo.vo.request.EmailCodeLoginVO;
import course.QAssistant.pojo.vo.request.EmailLoginVO;
import course.QAssistant.pojo.vo.request.EmailPasswordLoginVO;
import course.QAssistant.pojo.vo.request.ResetPasswordVO;
import course.QAssistant.pojo.vo.response.CheckCodeVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.ResponseCode;
import course.QAssistant.pojo.vo.response.UserLoginVO;
import course.QAssistant.service.EmailCodeService;
import course.QAssistant.service.SysUserService;
import course.QAssistant.util.IdWorker;
import course.QAssistant.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Date;

import static course.QAssistant.service.base.BaseService.vertifyCheckCode;


/**
 * @author 1115suc
 * @description 针对表【sys_user(用户表)】的数据库操作Service实现
 * @createDate 2026-02-25 12:22:35
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final IdWorker idWorker;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final SysUserMapper sysUserMapper;
    private final EmailCodeService emailCodeService;

    @Override
    public R<CheckCodeVO> getCaptcha() {
        // 自定义纯数字的验证码（随机4位数字，可重复）
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 5);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);
        lineCaptcha.setGenerator(randomGenerator);
        lineCaptcha.setFont(new Font("Arial", Font.BOLD, 50));
        lineCaptcha.setBackground(new Color(255, 255, 255));
        // 生成code
        lineCaptcha.createCode();
        String checkCode = lineCaptcha.getCode();

        log.info("生成校验码:{}", checkCode);

        //生成sessionId
        String sessionId = String.valueOf(idWorker.nextId());
        redisUtil.set(RedisConstant.CAPTCHA_KEY + sessionId, checkCode, TimeConstant.FIVE_MINUTE);
        CheckCodeVO checkCodeVo = new CheckCodeVO();
        checkCodeVo.setSessionId(sessionId);
        checkCodeVo.setImageData(lineCaptcha.getImageBase64Data());
        if (ObjectUtil.isNotEmpty(checkCodeVo)) {
            return R.ok(checkCodeVo);
        } else {
            log.error("生成校验码失败!!");
            throw new QAException(ResponseCode.CHECK_CODE_GENERATE_ERROR.getMessage());
        }
    }

    @Override
    public R register(EmailLoginVO emailLoginVo) {
        // 判断邮箱是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        String email = emailLoginVo.getEmail();
        queryWrapper.eq(SysUser::getEmail, email);
        if (ObjectUtil.isNotNull(sysUserMapper.selectOne(queryWrapper))) {
            throw new QAWebException(ResponseCode.ACCOUNT_EXISTS_ERROR.getMessage());
        }

        // 获取邮箱验证码
        if (!redisUtil.hasKey(RedisConstant.EMAIL_CODE + email)) {
            throw new QAWebException(ResponseCode.CHECK_CODE_EXPIRED_WAIT.getMessage());
        }
        emailCodeService.checkCode(email, emailLoginVo.getEmailCode());
        SysUser sysUser = new SysUser();

        String uid = "U" + RandomUtil.randomNumbers(9);
        sysUser.setEmail(emailLoginVo.getEmail());
        sysUser.setUid(uid);
        sysUser.setPassword(passwordEncoder.encode(emailLoginVo.getPassword()));
        sysUser.setNickName(emailLoginVo.getNickName());
        sysUser.setSex(SexEnum.UNKNOWN.getCode());
        sysUser.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        sysUser.setStatus(StatusEnum.NORMAL.getCode());
        sysUser.setCreateWhere(emailLoginVo.getCreateWhere());
        sysUser.setCreateTime(new Date(System.currentTimeMillis()));
        sysUser.setUpdateTime(new Date(System.currentTimeMillis()));

        try {
            sysUserMapper.insert(sysUser);
            return R.ok(ResponseCode.REGISTER_SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("用户注册失败:{}", e.getMessage());
            return R.error(ResponseCode.REGISTER_ERROR.getMessage());
        }
    }

    @Override
    public R<UserLoginVO> emailPasswordLogin(EmailPasswordLoginVO emailPasswordLoginVo) {
        String checkCode = emailPasswordLoginVo.getCheckCode();
        String sessionId = emailPasswordLoginVo.getSessionId();

        vertifyCheckCode(checkCode, sessionId, redisUtil);

        return processLogin(emailPasswordLoginVo.getEmail(), emailPasswordLoginVo.getLoginWhere(), emailPasswordLoginVo.getPassword());
    }

    @Override
    public R<UserLoginVO> emailCodeLogin(EmailCodeLoginVO emailCodeLoginVo) {
        String email = emailCodeLoginVo.getEmail();
        String code = emailCodeLoginVo.getEmailCode();

        // 校验验证码
        if (!redisUtil.hasKey(RedisConstant.EMAIL_CODE + email)) {
            throw new QAWebException(ResponseCode.CHECK_CODE_EXPIRED_WAIT.getMessage());
        }
        emailCodeService.checkCode(email, code);

        return processLogin(email, emailCodeLoginVo.getLoginWhere(), null);
    }

    private R<UserLoginVO> processLogin(String email, Integer loginWhere, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail, email);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);

        // 账号不存在
        if (ObjectUtil.isNull(sysUser) || NumberUtil.equals(sysUser.getDeleted(), DeletedEnum.DELETED.getCode())) {
            throw new QAWebException(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
        }
        // 账号被禁用
        Integer status = sysUser.getStatus();
        if (NumberUtil.equals(status, StatusEnum.DISABLED.getCode())) {
            throw new QAWebException(ResponseCode.ACCOUNT_LOCKED.getMessage());
        }

        // 密码校验 (仅当传入密码时)
        if (password != null && !passwordEncoder.matches(passwordEncoder.encode(password), sysUser.getPassword())) {
            throw new QAWebException(ResponseCode.PASSWORD_ERROR.getMessage());
        }

        String redisPrefix = LoginTypeEnum.of(loginWhere).getPrefix();
        String token = DigestUtil.md5Hex(sysUser.getUid());

        try {
            if (redisUtil.hasKey(redisPrefix + token)) {
                // 获取登录信息
                Object tokenLoginInfo = redisUtil.get(redisPrefix + token);
                TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);
                // 判断时间是否快要过期
                if (tokenUserDTO.getExpireAt() - System.currentTimeMillis() < TimeConstant.ONE_DAY) {
                    tokenUserDTO.setExpireAt(System.currentTimeMillis() + TimeConstant.ONE_WEEK);
                    redisUtil.set(redisPrefix + token, JSONUtil.toJsonStr(tokenUserDTO), TimeConstant.ONE_WEEK);
                }
            }else{
                TokenUserDTO tokenUserDTO = new TokenUserDTO();
                tokenUserDTO.setUid(sysUser.getUid());
                tokenUserDTO.setNickname(sysUser.getNickName());
                tokenUserDTO.setLoginWhere(loginWhere);
                tokenUserDTO.setExpireAt(System.currentTimeMillis() + TimeConstant.ONE_WEEK);
                tokenUserDTO.setToken(token);
                String tokenLoginInfo = JSONUtil.toJsonStr(tokenUserDTO);
                redisUtil.set(redisPrefix + token, tokenLoginInfo, TimeConstant.ONE_WEEK);
            }

            sysUser.setLastLoginTime(new Date(System.currentTimeMillis()));
            sysUserMapper.updateById(sysUser);

            UserLoginVO userLoginVO = new UserLoginVO();
            userLoginVO.setUid(sysUser.getUid());
            userLoginVO.setNickname(sysUser.getNickName());
            userLoginVO.setAvatar(sysUser.getAvatar());
            userLoginVO.setToken(token);
            return R.ok(ResponseCode.LOGIN_SUCCESS.getMessage(), userLoginVO);
        } catch (Exception e) {
            throw new QAWebException(ResponseCode.LOGIN_ERROR.getMessage());
        }
    }

    @Override
    public R logout(String token, Integer loginType) {
        try {
            redisUtil.del(LoginTypeEnum.of(loginType).getPrefix() + token);
            return R.ok(ResponseCode.LOGOUT_SUCCESS.getMessage());
        } catch (Exception e) {
            throw new QAWebException(ResponseCode.ERROR.getMessage());
        }

    }

    @Override
    public R resetPassword(String token, Integer loginType, ResetPasswordVO resetVo) {
        String emailCode = resetVo.getEmailCode();
        emailCodeService.checkCode(resetVo.getEmail(), emailCode);

        String redisPrefix = LoginTypeEnum.of(loginType).getPrefix();
        try {
            Object tokenLoginInfo = redisUtil.get(redisPrefix + token);
            TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);

            // 获取用户信息
            String uid = tokenUserDTO.getUid();
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUid, uid);
            SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNull(sysUser)) {
                throw new QAWebException(ResponseCode.ACCOUNT_NOT_EXISTS.getMessage());
            }

            sysUser.setPassword(passwordEncoder.encode(resetVo.getNewPassword()));
            sysUserMapper.updateById(sysUser);
            if (redisUtil.hasKey(LoginTypeEnum.of(1).getPrefix() + token)) {
                redisUtil.del(LoginTypeEnum.of(1).getPrefix() + token);
            }
            if (redisUtil.hasKey(LoginTypeEnum.of(2).getPrefix() + token)) {
                redisUtil.del(LoginTypeEnum.of(2).getPrefix() + token);
            }
            if (redisUtil.hasKey(LoginTypeEnum.of(3).getPrefix() + token)) {
                redisUtil.del(LoginTypeEnum.of(3).getPrefix() + token);
            }

            return R.ok(ResponseCode.RESET_PASSWORD_SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("获取用户token信息失败:{}, 登录方式:{}", e.getMessage(), loginType);
            throw new QAWebException(ResponseCode.ERROR.getMessage());
        }
    }
}