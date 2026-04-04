package course.QAssistant.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户AI个性化配置表
 * @TableName user_ai_preference
 */
@TableName(value ="user_ai_preference")
public class UserAiPreference {
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
     * 关联的AI模型ID
     */
    private Long aiModelId;

    /**
     * 温度参数
     */
    private BigDecimal temperature;

    /**
     * 核采样参数
     */
    private BigDecimal topP;

    /**
     * 最大生成Token数
     */
    private Integer maxTokens;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 用户UID
     */
    public String getUserUid() {
        return userUid;
    }

    /**
     * 用户UID
     */
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    /**
     * 关联的AI模型ID
     */
    public Long getAiModelId() {
        return aiModelId;
    }

    /**
     * 关联的AI模型ID
     */
    public void setAiModelId(Long aiModelId) {
        this.aiModelId = aiModelId;
    }

    /**
     * 温度参数
     */
    public BigDecimal getTemperature() {
        return temperature;
    }

    /**
     * 温度参数
     */
    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    /**
     * 核采样参数
     */
    public BigDecimal getTopP() {
        return topP;
    }

    /**
     * 核采样参数
     */
    public void setTopP(BigDecimal topP) {
        this.topP = topP;
    }

    /**
     * 最大生成Token数
     */
    public Integer getMaxTokens() {
        return maxTokens;
    }

    /**
     * 最大生成Token数
     */
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     * 系统提示词
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * 系统提示词
     */
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    /**
     * 创建时间
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 创建时间
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 更新时间
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 更新时间
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        UserAiPreference other = (UserAiPreference) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserUid() == null ? other.getUserUid() == null : this.getUserUid().equals(other.getUserUid()))
            && (this.getAiModelId() == null ? other.getAiModelId() == null : this.getAiModelId().equals(other.getAiModelId()))
            && (this.getTemperature() == null ? other.getTemperature() == null : this.getTemperature().equals(other.getTemperature()))
            && (this.getTopP() == null ? other.getTopP() == null : this.getTopP().equals(other.getTopP()))
            && (this.getMaxTokens() == null ? other.getMaxTokens() == null : this.getMaxTokens().equals(other.getMaxTokens()))
            && (this.getSystemPrompt() == null ? other.getSystemPrompt() == null : this.getSystemPrompt().equals(other.getSystemPrompt()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserUid() == null) ? 0 : getUserUid().hashCode());
        result = prime * result + ((getAiModelId() == null) ? 0 : getAiModelId().hashCode());
        result = prime * result + ((getTemperature() == null) ? 0 : getTemperature().hashCode());
        result = prime * result + ((getTopP() == null) ? 0 : getTopP().hashCode());
        result = prime * result + ((getMaxTokens() == null) ? 0 : getMaxTokens().hashCode());
        result = prime * result + ((getSystemPrompt() == null) ? 0 : getSystemPrompt().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userUid=").append(userUid);
        sb.append(", aiModelId=").append(aiModelId);
        sb.append(", temperature=").append(temperature);
        sb.append(", topP=").append(topP);
        sb.append(", maxTokens=").append(maxTokens);
        sb.append(", systemPrompt=").append(systemPrompt);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}