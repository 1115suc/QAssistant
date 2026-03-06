package course.QAssistant.langchain.service;

import dev.langchain4j.service.TokenStream;

/**
 * 聊天服务接口
 *
 * @author TraeAI
 * @date 2026-03-06
 * @description 提供与 AI 模型进行同步和流式聊天交互的内部服务接口。
 */
public interface ChatService {

    /**
     * 发送提示到 AI 模型并同步返回响应。
     *
     * @param userId 用户的唯一标识符。
     * @param prompt 用户的输入提示。
     * @return AI 模型的响应字符串。
     * @throws RuntimeException 如果交互失败。
     */
    String sendPrompt(String userId, String prompt);

    /**
     * 发送提示到 AI 模型并返回流式响应。
     *
     * @param userId 用户的唯一标识符。
     * @param prompt 用户的输入提示。
     * @return TokenStream，用于逐个 token 发射生成的内容。
     * @throws RuntimeException 如果交互失败。
     */
    TokenStream streamPrompt(String userId, String prompt);
}
