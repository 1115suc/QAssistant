package course.QAssistant.service;

import course.QAssistant.pojo.vo.request.EmailCheckCodeVO;
import course.QAssistant.pojo.vo.response.R;

public interface EmailCodeService {

	R sendEmailCode(EmailCheckCodeVO email);

	void checkCode(String email,String code);
}