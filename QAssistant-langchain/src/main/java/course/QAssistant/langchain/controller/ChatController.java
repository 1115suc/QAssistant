package course.QAssistant.langchain.controller;

import course.QAssistant.langchain.dto.AiChatResponse;
import course.QAssistant.langchain.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * 聊天控制器
 * 演示如何接收用户请求并调用支持多租户隔离的 AI 服务
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送消息
     * 请求头中需包含 X-User-Uid, X-Ai-Uid (可选)
     *
     * @param requestBody 请求体，包含 prompt
     * @param userUid 用户 UID (通常由网关或 Filter 注入，这里演示从 Header 获取)
     * @return 响应结果
     */
    @PostMapping("/send")
    public AiChatResponse sendMessage(
            @RequestBody Map<String, String> requestBody,
            @RequestHeader(value = "X-User-Uid", required = true) String userUid,
            @RequestHeader(value = "X-Conversation-Uid", required = false) String conversationUid) {

        String prompt = requestBody.get("prompt");
        
        log.info("收到用户 {} 的请求: {}", userUid, prompt);

        // 调用 Service，Service 内部会通过 ThreadLocal 获取上下文信息 (AI UID 等)
        // 并根据 AI UID 动态加载配置、构建模型、隔离会话
        String responseContent = chatService.sendPrompt(userUid, prompt);

        return new AiChatResponse(
                conversationUid,
                responseContent,
                Collections.emptyMap(),
                200,
                "Success"
        );
    }
}
