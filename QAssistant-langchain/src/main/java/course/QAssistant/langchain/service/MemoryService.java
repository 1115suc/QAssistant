package course.QAssistant.langchain.service;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * 记忆服务接口
 *
 * @author TraeAI
 * @date 2026-03-06
 * @description 提供管理聊天历史和上下文的内部服务接口。
 */
public interface MemoryService {

    /**
     * 保存特定用户的聊天历史。
     *
     * @param userId   用户的唯一标识符。
     * @param messages 要保存的聊天消息列表。
     * @throws RuntimeException 如果保存操作失败。
     */
    void saveHistory(String userId, List<ChatMessage> messages);

    /**
     * 获取特定用户的聊天历史。
     *
     * @param userId 用户的唯一标识符。
     * @return 与该用户关联的聊天消息列表。
     * @throws RuntimeException 如果获取操作失败。
     */
    List<ChatMessage> getHistory(String userId);
}
