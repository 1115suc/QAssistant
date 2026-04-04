package course.QAssistant.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户自定义AI模型表
 * @TableName user_ai_model
 */
@Data
@TableName(value ="user_ai_model")
public class UserAiModel {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户UID
     */
    private String userUid;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型调用URL
     */
    private String baseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型提供商 (OpenAI, DeepSeek等)
     */
    private String provider;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}