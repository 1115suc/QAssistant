package course.QAssistant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import course.QAssistant.pojo.po.SysUser;
import course.QAssistant.pojo.vo.request.*;
import course.QAssistant.pojo.vo.response.CheckCodeVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserInfoVO;
import course.QAssistant.pojo.vo.response.UserLoginVO;

/**
* @author 1115suc
* @description 针对表【sys_user(用户表)】的数据库操作Service
* @createDate 2026-02-25 12:22:35
*/
public interface SysUserService extends IService<SysUser> {
    // 获取验证码
    R<CheckCodeVO> getCaptcha();
    // 用户注册接口
    R register(EmailLoginVO emailLoginVo);
    // 邮箱密码登录
    R<UserLoginVO> emailPasswordLogin(EmailPasswordLoginVO emailPasswordLoginVo);
    // 邮箱验证码登录
    R<UserLoginVO> emailCodeLogin(EmailCodeLoginVO emailCodeLoginVo);
    // 登出
    R logout(String token, String loginType);
    // 重置密码
    R resetPassword(String token, String loginType, ResetPasswordVO resetVo);
    // 获取用户信息
    R<UserInfoVO> getUserInfo(String token, String loginType);
    // 修改用户信息
    R updateUserInfo(String token, String loginType, UpdateUserInfoVO updateUserInfoVO);
}
