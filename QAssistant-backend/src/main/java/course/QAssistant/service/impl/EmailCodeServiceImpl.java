package course.QAssistant.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import course.QAssistant.constant.RedisConstant;
import course.QAssistant.constant.TimeConstant;
import course.QAssistant.exception.QAException;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.pojo.vo.request.EmailCheckCodeVo;
import course.QAssistant.pojo.vo.response.CheckCodeVo;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.ResponseCode;
import course.QAssistant.properties.EmailConfigProperties;
import course.QAssistant.service.EmailCodeService;
import course.QAssistant.util.RedisUtil;
import jakarta.mail.internet.MimeMessage;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service("emailCodeService")
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

	private final RedisUtil redisUtil;
    private final JavaMailSender javaMailSender;
    private final EmailConfigProperties emailConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R sendEmailCode(EmailCheckCodeVo vo) {
        String sessionId = vo.getSessionId();
        String checkCode = vo.getCheckCode();
        String verification = (String) redisUtil.get(RedisConstant.CAPTCHA_KEY + sessionId);
        if (StringUtil.isBlank(verification)) {
            throw new QAWebException(ResponseCode.CHECK_CODE_EXPIRED.getMessage());
        }
        if (!StringUtil.equals(verification, checkCode)) {
            throw new QAWebException(ResponseCode.CHECK_CODE_ERROR.getMessage());
        }

        String email = vo.getEmail();
		if (redisUtil.hasKey(RedisConstant.EMAIL_CODE + email)) {
			throw new QAWebException(ResponseCode.EMAIL_SEND_ERROR_WAIT.getMessage());
        }
        String code = RandomUtil.randomNumbers(6);
        try {
			sentMailCode(email, code);
			redisUtil.set(RedisConstant.EMAIL_CODE + email, code, TimeConstant.FIVE_MINUTE);
			return R.ok(ResponseCode.EMAIL_SEND_SUCCESS.getMessage());
		} catch (Exception e) {
			throw new QAWebException(ResponseCode.EMAIL_SEND_ERROR.getMessage(), e);
		}
    }

    private void sentMailCode(String toEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailConfig.getUsername());
            helper.setTo(toEmail);

            helper.setSubject("QAssistant 邮箱验证码");

            String htmlContent = new String(Files.readAllBytes(Paths.get("document/email/EmailHtml.html")));
            htmlContent = htmlContent.replace("123456", code);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new QAWebException(ResponseCode.EMAIL_SEND_ERROR.getMessage());
        }
    }

    @Override
    public void checkCode(String email, String code) {
		if (!redisUtil.hasKey(RedisConstant.EMAIL_CODE + email)) {
			throw new QAWebException(ResponseCode.CHECK_CODE_ERROR.getMessage());
        }
		if (!redisUtil.get(RedisConstant.EMAIL_CODE + email).equals(code)) {
			throw new QAWebException(ResponseCode.CHECK_CODE_ERROR.getMessage());
        }
		// 验证码验证成功后，删除验证码
		redisUtil.del(RedisConstant.EMAIL_CODE + email);
    }
}