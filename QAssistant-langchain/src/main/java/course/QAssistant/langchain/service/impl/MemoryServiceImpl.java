package course.QAssistant.langchain.service.impl;

import course.QAssistant.langchain.service.MemoryService;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

/**
 * 记忆服务实现类
 *
 * @author TraeAI
 * @date 2026-03-06
 * @description 使用内存存储（ConcurrentHashMap）实现 MemoryService 接口。
 *              这是一个演示实现，在生产环境中应替换为持久化存储（如 Redis、数据库）。
 */
@Slf4j
@Service
public class MemoryServiceImpl implements MemoryService {

    private final Map<String, List<ChatMessage>> memoryStore = new ConcurrentHashMap<>();

    /**
     * 保存特定用户的聊天历史。
     *
     * @param userId   用户的唯一标识符。
     * @param messages 要保存的聊天消息列表。
     * @throws RuntimeException 如果保存操作失败。
     */
    @Override
    public void saveHistory(String userId, List<ChatMessage> messages) {
        log.info("正在为用户 {} 保存历史，消息数量：{}", userId, messages.size());
        try {
            memoryStore.put(userId, new ArrayList<>(messages)); // 复制以确保读取时的线程安全
        } catch (Exception e) {
            log.error("为用户 {} 保存历史时出错", userId, e);
            throw new RuntimeException("保存历史失败", e);
        }
    }

    /**
     * 获取特定用户的聊天历史。
     *
     * @param userId 用户的唯一标识符。
     * @return 与该用户关联的聊天消息列表。
     * @throws RuntimeException 如果获取操作失败。
     */
    @Override
    public List<ChatMessage> getHistory(String userId) {
        log.info("正在检索用户 {} 的历史记录", userId);
        try {
            return memoryStore.getOrDefault(userId, new ArrayList<>());
        } catch (Exception e) {
            log.error("为用户 {} 检索历史时出错", userId, e);
            throw new RuntimeException("检索历史失败", e);
        }
    }
}
