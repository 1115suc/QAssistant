package course.QAssistant.langchain.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI 服务接口，用于动态构建代理
 */
public interface IChatAgent {

    @SystemMessage("{{systemPrompt}}")
    String chat(@UserMessage String userMessage);

}
