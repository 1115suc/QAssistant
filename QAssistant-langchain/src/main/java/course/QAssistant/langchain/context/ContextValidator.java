package course.QAssistant.langchain.context;

import course.QAssistant.langchain.exception.AccessDeniedException;
import org.springframework.util.StringUtils;

/**
 * 上下文校验器
 * 用于校验当前请求是否具有访问目标资源的权限
 */
public class ContextValidator {

    /**
     * 校验当前上下文是否与目标用户和 AI 匹配
     * @param targetUserUid 目标用户 UID
     * @param targetAiUid 目标 AI UID
     * @throws AccessDeniedException 如果上下文不匹配或缺少必要的上下文信息
     */
    public static void assertContext(String targetUserUid, String targetAiUid) {
        String currentUserUid = ConversationContextHolder.getUserUid();
        String currentAiUid = ConversationContextHolder.getAiUid();

        // 校验用户一致性
        if (!StringUtils.hasText(currentUserUid)) {
            throw new AccessDeniedException("缺少用户上下文信息");
        }
        if (!currentUserUid.equals(targetUserUid)) {
            throw new AccessDeniedException("跨租户访问被拒绝：当前用户 " + currentUserUid + " 试图访问用户 " + targetUserUid + " 的资源");
        }

        // 校验 AI 一致性（如果目标 AI 不为空）
        if (StringUtils.hasText(targetAiUid)) {
            if (!StringUtils.hasText(currentAiUid)) {
                // 如果请求中没有带 AI UID，但操作需要特定 AI，可能需要根据业务逻辑放行或报错
                // 这里假设必须匹配
                 throw new AccessDeniedException("缺少 AI 上下文信息");
            }
            if (!currentAiUid.equals(targetAiUid)) {
                throw new AccessDeniedException("跨 AI 实例访问被拒绝：当前 AI " + currentAiUid + " 试图访问 AI " + targetAiUid + " 的资源");
            }
        }
    }
}
