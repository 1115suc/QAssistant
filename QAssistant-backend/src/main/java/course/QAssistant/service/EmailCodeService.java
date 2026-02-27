package course.QAssistant.service;

import course.QAssistant.pojo.vo.request.EmailCheckCodeVo;
import course.QAssistant.pojo.vo.response.R;

public interface EmailCodeService {

	R sendEmailCode(EmailCheckCodeVo email);

	void checkCode(String email,String code);
}