package course.QAssistant.LangChain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface RagAssistant {
    @SystemMessage("你是一个专业的文档助手。请根据提供的上下文信息，准确回答用户的问题。{{systemPrompt}}")
    Flux<String> chat(@MemoryId String sessionId, @UserMessage String userMessage, @V("systemPrompt") String systemPrompt);
}