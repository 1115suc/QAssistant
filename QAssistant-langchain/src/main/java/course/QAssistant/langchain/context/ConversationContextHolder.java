package course.QAssistant.langchain.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 会话上下文持有者
 * 使用 ThreadLocal 存储当前请求的会话信息，实现线程隔离
 */
public class ConversationContextHolder {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static final String KEY_USER_UID = "userUid";
    public static final String KEY_AI_UID = "aiUid";
    public static final String KEY_CONVERSATION_UID = "conversationUid";

    /**
     * 设置上下文信息
     * @param key 键
     * @param value 值
     */
    public static void set(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    /**
     * 获取上下文信息
     * @param key 键
     * @return 值
     */
    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    /**
     * 获取当前用户 UID
     * @return 用户 UID
     */
    public static String getUserUid() {
        return (String) get(KEY_USER_UID);
    }

    /**
     * 获取当前 AI 模型 UID
     * @return AI 模型 UID
     */
    public static String getAiUid() {
        return (String) get(KEY_AI_UID);
    }

    /**
     * 获取当前会话 UID
     * @return 会话 UID
     */
    public static String getConversationUid() {
        return (String) get(KEY_CONVERSATION_UID);
    }

    /**
     * 清除上下文信息
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
