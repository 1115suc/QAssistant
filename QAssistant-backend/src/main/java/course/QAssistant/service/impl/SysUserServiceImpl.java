package course.QAssistant.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.constant.RedisConstant;
import course.QAssistant.constant.TimeConstant;
import course.QAssistant.exception.QAException;
import course.QAssistant.mapper.SysUserMapper;
import course.QAssistant.pojo.po.SysUser;
import course.QAssistant.pojo.vo.request.EmailLoginVo;
import course.QAssistant.pojo.vo.response.CheckCodeVo;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.SysUserService;
import course.QAssistant.util.IdWorker;
import course.QAssistant.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;


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
    private final RedisUtil redisUtil;

    @Override
    public R<CheckCodeVo> getCaptcha() {
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
        CheckCodeVo checkCodeVo = new CheckCodeVo();
        checkCodeVo.setSessionId(sessionId);
        checkCodeVo.setImageData(lineCaptcha.getImageBase64Data());
        if (ObjectUtil.isNotEmpty(checkCodeVo)) {
            return R.ok(checkCodeVo);
        }else {
            log.error("生成校验码失败!!");
            throw new QAException("生成校验码失败,请稍后重试");
        }
    }

    @Override
    public R<String> register(EmailLoginVo emailLoginVo) {

        return null;
    }
}




