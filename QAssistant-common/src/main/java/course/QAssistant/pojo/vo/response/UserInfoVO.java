package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息响应 VO
 */
@Data
@Schema(description = "用户信息查询返回类")
public class UserInfoVO {
    
    @Schema(description = "用户 ID")
    private String uid;
    
    @Schema(description = "用户昵称")
    private String nickName;
    
    @Schema(description = "用户头像 URL")
    private String avatar;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "性别 (0.未知 1.男 2.女)")
    private Integer sex;
    
    @Schema(description = "手机号码")
    private String phone;
    
    @Schema(description = "用户简介")
    private String description;
    
    @Schema(description = "生日")
    private Date birthday;
    
    @Schema(description = "地区名称")
    private String areaName;
}