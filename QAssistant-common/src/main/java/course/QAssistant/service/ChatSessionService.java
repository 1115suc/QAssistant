package course.QAssistant.service;

import course.QAssistant.pojo.dto.CreateSessionRequest;
import course.QAssistant.pojo.dto.MessageResponse;
import course.QAssistant.pojo.dto.SendMessageRequest;
import course.QAssistant.pojo.dto.SessionDetailResponse;
import course.QAssistant.pojo.dto.SessionSummaryResponse;
import course.QAssistant.pojo.dto.UpdateSessionRequest;

import java.util.List;

/**
 * ChatSession Service 接口
 */
public interface ChatSessionService {

    // ==================== Session 会话管理 ====================

    /**
     * 创建新会话
     *
     * @param request 创建会话请求
     * @return 会话摘要信息
     */
    SessionSummaryResponse createSession(CreateSessionRequest request);

    /**
     * 获取用户所有会话列表（不含消息体）
     *
     * @param userUid 用户 UID
     * @return 会话摘要列表
     */
    List<SessionSummaryResponse> getUserSessions(String userUid);

    /**
     * 获取会话详情（含所有消息）
     *
     * @param sessionId 会话 ID
     * @param userUid   用户 UID（鉴权）
     * @return 会话详情
     */
    SessionDetailResponse getSessionDetail(String sessionId, String userUid);

    /**
     * 更新会话信息（标题、模型等）
     *
     * @param sessionId 会话 ID
     * @param userUid   用户 UID（鉴权）
     * @param request   更新请求
     * @return 更新后的会话摘要
     */
    SessionSummaryResponse updateSession(String sessionId, String userUid,
                                         UpdateSessionRequest request);

    /**
     * 删除会话（含其所有消息）
     *
     * @param sessionId 会话 ID
     * @param userUid   用户 UID（鉴权）
     */
    void deleteSession(String sessionId, String userUid);

    /**
     * 删除某用户的全部会话
     *
     * @param userUid 用户 UID
     */
    void deleteAllUserSessions(String userUid);

    // ==================== Message 消息管理 ====================

    /**
     * 向会话追加一条消息
     *
     * @param request 发送消息请求
     * @return 新增的消息详情
     */
    MessageResponse addMessage(SendMessageRequest request);

    /**
     * 获取会话的所有消息
     *
     * @param sessionId 会话 ID
     * @param userUid   用户 UID（鉴权）
     * @return 消息列表
     */
    List<MessageResponse> getMessages(String sessionId, String userUid);

    /**
     * 删除会话中的某条消息
     *
     * @param sessionId 会话 ID
     * @param messageId 消息 ID
     * @param userUid   用户 UID（鉴权）
     */
    void deleteMessage(String sessionId, String messageId, String userUid);

    /**
     * 清空会话的所有消息（保留会话本身）
     *
     * @param sessionId 会话 ID
     * @param userUid   用户 UID（鉴权）
     */
    void clearMessages(String sessionId, String userUid);
}