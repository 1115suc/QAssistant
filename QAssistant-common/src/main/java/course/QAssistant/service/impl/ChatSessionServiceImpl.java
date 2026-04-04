package course.QAssistant.service.impl;

import course.QAssistant.pojo.dto.CreateSessionRequest;
import course.QAssistant.pojo.dto.MessageResponse;
import course.QAssistant.pojo.dto.SendMessageRequest;
import course.QAssistant.pojo.dto.SessionDetailResponse;
import course.QAssistant.pojo.dto.SessionSummaryResponse;
import course.QAssistant.pojo.dto.UpdateSessionRequest;
import course.QAssistant.pojo.po.ChatMessageDocument;
import course.QAssistant.pojo.po.ChatSession;
import course.QAssistant.repository.ChatSessionRepository;
import course.QAssistant.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatSession Service 实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final MongoTemplate mongoTemplate;

    // ====================================================================
    // Session 会话管理
    // ====================================================================

    @Override
    public SessionSummaryResponse createSession(CreateSessionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = ChatSession.builder()
                .userUid(request.getUserUid())
                .title(request.getTitle())
                .aiModelId(request.getAiModelId())
                .createdAt(now)
                .updatedAt(now)
                .build();

        ChatSession saved = chatSessionRepository.save(session);
        log.info("Created session [{}] for user [{}]", saved.getId(), request.getUserUid());
        return toSummaryResponse(saved);
    }

    @Override
    public List<SessionSummaryResponse> getUserSessions(String userUid) {
        // 查询时排除 messages 字段，节省网络传输
        Query query = Query.query(Criteria.where("userUid").is(userUid));
        query.with(Sort.by(Sort.Direction.DESC, "updatedAt"));
        query.fields().exclude("messages");

        List<ChatSession> sessionsWithMessages = mongoTemplate.find(query, ChatSession.class);

        return sessionsWithMessages.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDetailResponse getSessionDetail(String sessionId, String userUid) {
        ChatSession session = findSessionWithAuth(sessionId, userUid);
        return toDetailResponse(session);
    }

    @Override
    public SessionSummaryResponse updateSession(String sessionId, String userUid,
                                                UpdateSessionRequest request) {
        findSessionWithAuth(sessionId, userUid);

        Query query = Query.query(Criteria.where("_id").is(sessionId));
        Update update = new Update().set("updated_at", LocalDateTime.now());

        if (request.getTitle() != null) {
            update.set("title", request.getTitle());
        }
        if (request.getAiModelId() != null) {
            update.set("ai_model_id", request.getAiModelId());
        }

        mongoTemplate.updateFirst(query, update, ChatSession.class);

        ChatSession updated = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        log.info("Updated session [{}]", sessionId);
        return toSummaryResponse(updated);
    }


    @Override
    public void deleteSession(String sessionId, String userUid) {
        ChatSession session = findSessionWithAuth(sessionId, userUid);
        chatSessionRepository.delete(session);
        log.info("Deleted session [{}] for user [{}]", sessionId, userUid);
    }

    @Override
    public void deleteAllUserSessions(String userUid) {
        chatSessionRepository.deleteByUserUid(userUid);
        log.info("Deleted all sessions for user [{}]", userUid);
    }

    // ====================================================================
    // Message 消息管理
    // ====================================================================

    @Override
    public MessageResponse addMessage(SendMessageRequest request) {
        findSessionWithAuth(request.getSessionId(), request.getUserUid());

        ChatMessageDocument message = ChatMessageDocument.builder()
                .id(new ObjectId().toHexString())
                .role(request.getRole())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        Query query = Query.query(Criteria.where("_id").is(request.getSessionId()));
        Update update = new Update()
                .push("messages", message)
                .set("updated_at", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, ChatSession.class);
        log.info("Added message [{}] to session [{}]", message.getId(), request.getSessionId());

        return toMessageResponse(message);
    }

    @Override
    public List<MessageResponse> getMessages(String sessionId, String userUid) {
        ChatSession session = findSessionWithAuth(sessionId, userUid);
        return session.getMessages().stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMessage(String sessionId, String messageId, String userUid) {
        findSessionWithAuth(sessionId, userUid);

        Query query = Query.query(Criteria.where("_id").is(sessionId));
        Update update = new Update()
                .pull("messages", Query.query(Criteria.where("_id").is(messageId)))
                .set("updated_at", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, ChatSession.class);
        log.info("Deleted message [{}] from session [{}]", messageId, sessionId);
    }

    @Override
    public void clearMessages(String sessionId, String userUid) {
        findSessionWithAuth(sessionId, userUid);

        Query query = Query.query(Criteria.where("_id").is(sessionId));
        Update update = new Update()
                .set("messages", List.of())
                .set("updated_at", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, ChatSession.class);
        log.info("Cleared all messages in session [{}]", sessionId);
    }


    // ====================================================================
    // 私有工具方法
    // ====================================================================

    /**
     * 查询会话并校验用户权限（防止越权访问）
     */
    private ChatSession findSessionWithAuth(String sessionId, String userUid) {
        return chatSessionRepository.findByIdAndUserUid(sessionId, userUid)
                .orElseThrow(() -> new RuntimeException(
                        "Session not found or access denied: sessionId=" + sessionId));
    }

    /**
     * ChatSession → SessionSummaryResponse
     */
    private SessionSummaryResponse toSummaryResponse(ChatSession session) {
        return SessionSummaryResponse.builder()
                .id(session.getId())
                .userUid(session.getUserUid())
                .title(session.getTitle())
                .aiModelId(session.getAiModelId())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    /**
     * ChatSession → SessionDetailResponse（含 messages）
     */
    private SessionDetailResponse toDetailResponse(ChatSession session) {
        List<MessageResponse> messages = session.getMessages().stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        return SessionDetailResponse.builder()
                .id(session.getId())
                .userUid(session.getUserUid())
                .title(session.getTitle())
                .aiModelId(session.getAiModelId())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .messages(messages)
                .build();
    }

    /**
     * ChatMessage → MessageResponse
     */
    private MessageResponse toMessageResponse(ChatMessageDocument message) {
        return MessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}