package course.QAssistant.controller;

import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.vo.request.ChatRequestVO;
import course.QAssistant.pojo.vo.request.CreateSessionRequestVO;
import course.QAssistant.pojo.vo.request.RagUploadVO;
import course.QAssistant.pojo.vo.response.ChatSessionRespVO;
import course.QAssistant.pojo.vo.response.DetailChatMessageVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "AI 聊天管理", description = "AI 聊天核心功能接口，包括会话管理、流式对话、文档分析(RAG)")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "创建聊天会话", description = "根据用户选择的模型（系统默认或用户自定义）创建新的会话。")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/session")
    public R<ChatSessionRespVO> createSession(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
            @RequestBody CreateSessionRequestVO sessionCreateVO) {
        return chatService.createSession(sessionCreateVO, token, loginType);
    }

    @Operation(summary = "获取用户的会话列表", description = "返回当前用户创建的所有历史会话。")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/session/list")
    public R<List<ChatSessionRespVO>> getSessions(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        return chatService.getUserSessions(token, loginType);
    }

    @Operation(
            summary = "获取用户聊天记录",
            description = "获取当前登录用户的会话中的聊天记录。",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "sessionId", description = "会话ID", required = true, in = ParameterIn.PATH)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/{sessionId}")
    public R<DetailChatMessageVO> getChatHistory(@NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
                                                 @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType,
                                                 @NotBlank(message = "sessionId 不能为空") @PathVariable("sessionId") String sessionId) {
        return chatService.getSessionDetail(sessionId, token, loginType);
    }

    @Operation(summary = "对文档进行RAG解析", description = "用户上传文件后进行切分并存入Qdrant知识库，在同一会话中提供RAG支持。")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping(value = "/rag/ingest")
    public R ingestDocument(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
            @RequestBody RagUploadVO ragUploadVO) {
        return chatService.ingestDocument(ragUploadVO, token, loginType);
    }


    @Operation(summary = "发送聊天消息 (流式响应)", description = "支持系统默认模型与用户自定义模型，带上下文记忆。")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // GET 不适合携带 Body，建议改为 POST
    public Flux<String> streamChat(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
            @RequestBody ChatRequestVO chatRequestVO) {
        return chatService.streamChat(chatRequestVO, token, loginType);
    }
}
