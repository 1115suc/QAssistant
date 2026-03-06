package course.QAssistant.langchain.context;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * MDC 过滤器
 * 用于从请求头中提取上下文信息并填充到 MDC 和 ThreadLocal 中
 */
@Component
public class MdcFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_UID = "X-User-Uid";
    private static final String HEADER_AI_UID = "X-Ai-Uid";
    private static final String HEADER_CONVERSATION_UID = "X-Conversation-Uid";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String userUid = request.getHeader(HEADER_USER_UID);
            String aiUid = request.getHeader(HEADER_AI_UID);
            String conversationUid = request.getHeader(HEADER_CONVERSATION_UID);

            // 如果没有提供 conversationUid，生成一个新的
            if (conversationUid == null || conversationUid.isEmpty()) {
                conversationUid = UUID.randomUUID().toString();
            }

            // 填充 ThreadLocal
            if (userUid != null) {
                ConversationContextHolder.set(ConversationContextHolder.KEY_USER_UID, userUid);
                MDC.put(ConversationContextHolder.KEY_USER_UID, userUid);
            }
            if (aiUid != null) {
                ConversationContextHolder.set(ConversationContextHolder.KEY_AI_UID, aiUid);
                MDC.put(ConversationContextHolder.KEY_AI_UID, aiUid);
            }

            ConversationContextHolder.set(ConversationContextHolder.KEY_CONVERSATION_UID, conversationUid);
            MDC.put(ConversationContextHolder.KEY_CONVERSATION_UID, conversationUid);

            filterChain.doFilter(request, response);
        } finally {
            // 清理上下文
            ConversationContextHolder.clear();
            MDC.clear();
        }
    }
}
