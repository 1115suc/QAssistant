package course.QAssistant.pojo.enums;

/**
 * 重复提交检查类型枚举
 */
public enum CheckType {
    /** 仅基于 IP 地址 */
    IP,
    /** 仅基于 DeviceID（前端指纹） */
    DEVICE_ID,
    /** 仅基于 Token（JWT / Session Token） */
    TOKEN,
    /** IP + DeviceID + Token 组合 */
    ALL
}