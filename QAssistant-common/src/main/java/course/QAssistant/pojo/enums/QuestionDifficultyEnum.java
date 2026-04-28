package course.QAssistant.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionDifficultyEnum {

    EASY("easy", "简单难度"),
    MEDIUM("medium", "中等难度"),
    HARD("hard", "困难难度");

    private final String code;
    private final String description;

    /**
     * 通过code获取枚举值
     *
     * @param code 难度代码
     * @return 对应的枚举值，如果未找到返回null
     */
    public static QuestionDifficultyEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (QuestionDifficultyEnum difficulty : values()) {
            if (difficulty.getCode().equalsIgnoreCase(code)) {
                return difficulty;
            }
        }
        return null;
    }

    /**
     * 验证code是否有效
     *
     * @param code 难度代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}
