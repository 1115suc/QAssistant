package course.QAssistant.LangChain4j.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chat 配置类
 * 负责注册 ChatMemory 相关的 Bean
 */
@Configuration
public class MemoryConfig {

    // ====================================================================
    // 本项目采用 MongoDB 进行会话管理 根据需要开启下述功能
    // ====================================================================

    /**
     * 注册 ChatMemoryStore Bean
     * 实际生产中应替换为 Redis 或数据库实现
     */
//    @Bean
//    public ChatMemoryStore chatMemoryStore() {
//        return new InMemoryChatMemoryStore();
//    }

    /**
     * 注册 ChatMemoryProvider Bean
     * 用于为每个会话创建独立的 ChatMemory 实例
     *
     * @param chatMemoryStore 聊天记忆存储
     * @return ChatMemoryProvider
     */
//    @Bean
//    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
//        return memoryId -> {
//            // MemoryId 格式应为：userUid:aiUid:conversationUid
//            // 确保同一用户不同 AI 实例内存隔离
//            return MessageWindowChatMemory.builder()
//                    .id(memoryId)
//                    .maxMessages(20)
//                    .chatMemoryStore(chatMemoryStore)
//                    .build();
//        };
//    }
}
