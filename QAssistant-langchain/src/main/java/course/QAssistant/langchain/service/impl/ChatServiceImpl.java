package course.QAssistant.langchain.service.impl;

import course.QAssistant.langchain.service.ChatService;
import course.QAssistant.langchain.context.ContextValidator;
import course.QAssistant.langchain.context.ConversationContextHolder;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天服务实现类
 *
 * @author 1115suc
 * @date 2026-03-06
 * @description 使用 LangChain4j 模型实现 ChatService 接口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingChatLanguageModel;

    /**
     * 同步发送提示到 AI 模型并返回响应。
     *
     * @param userId 用户的唯一标识符。
     * @param prompt 用户的输入提示。
     * @return AI 模型的响应字符串。
     * @throws RuntimeException 如果交互失败或模型未配置。
     */
    @Override
    public String sendPrompt(String userId, String prompt) {
        // 获取当前 AI UID 并校验上下文
        String aiUid = ConversationContextHolder.getAiUid();
        ContextValidator.assertContext(userId, aiUid);

        log.info("正在为用户 {} 发送提示，AI UID: {}", userId, aiUid);
        try {
            if (chatLanguageModel == null) {
                throw new IllegalStateException("未配置 ChatLanguageModel。");
            }
            // 这里可以集成 ChatMemory，使用 userUid + aiUid + conversationUid 作为 key
            String response = chatLanguageModel.generate(prompt);
            log.info("已收到用户 {} 的响应", userId);
            return response;
        } catch (Exception e) {
            log.error("为用户 {} 发送提示时出错：{}", userId, e);
            throw new RuntimeException("发送提示失败", e);
        }
    }

    @Override
    public TokenStream streamPrompt(String userId, String prompt) {
        return null;
    }
}
