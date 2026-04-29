package course.QAssistant.service;

import course.QAssistant.pojo.vo.request.ChatRequestVO;
import course.QAssistant.pojo.vo.request.CreateSessionRequestVO;
import course.QAssistant.pojo.vo.request.RagUploadVO;
import course.QAssistant.pojo.vo.response.ChatSessionRespVO;
import course.QAssistant.pojo.vo.response.DetailChatMessageVO;
import course.QAssistant.pojo.vo.response.R;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    // 创建会话
    R<ChatSessionRespVO> createSession(CreateSessionRequestVO sessionCreateVO, String token, String loginType);
    // 获取用户会话列表
    R<List<ChatSessionRespVO>> getUserSessions(String token, String loginType);
    // 获取会话详情
    R<DetailChatMessageVO> getSessionDetail(String sessionId, String token, String loginType);
    // RAG 文件上传
    R ingestDocument(RagUploadVO ragUploadVO, String token, String loginType);
    // RAG 文件上传
    R ingestDocumentsByFileIds(List<Long> fileIds, String token, String loginType);
    // 聊天功能
    Flux<String> streamChat(ChatRequestVO chatRequestVO, String token, String loginType);
}
