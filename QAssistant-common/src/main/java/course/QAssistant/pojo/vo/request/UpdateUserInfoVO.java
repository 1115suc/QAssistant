package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 更新用户信息请求 VO
 */
@Data
@Schema(description = "更新用户信息请求类,使用form表单提交（头像是文件）")
public class UpdateUserInfoVO {
    
    @Schema(description = "用户昵称")
    private String nickName;
    
    @Schema(description = "用户头像文件")
    private MultipartFile avatar;
    
    @Schema(description = "性别 (0.未知 1.男 2.女)")
    private Integer sex;
    
    @Schema(description = "真实名称")
    private String realName;
    
    @Schema(description = "手机号码")
    private String phone;
    
    @Schema(description = "用户简介")
    private String description;
    
    @Schema(description = "生日")
    private Date birthday;
    
    @Schema(description = "地区名称")
    private String areaName;
}