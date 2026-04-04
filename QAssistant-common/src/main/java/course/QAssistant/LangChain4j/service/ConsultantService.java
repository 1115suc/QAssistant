package course.QAssistant.LangChain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface ConsultantService {
    @SystemMessage("{{systemPrompt}}")
    Flux<String> chat(@UserMessage String message, @MemoryId String memoryId, @V("systemPrompt") String systemPrompt);
}