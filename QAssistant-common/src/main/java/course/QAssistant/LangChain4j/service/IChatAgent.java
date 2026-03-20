package course.QAssistant.LangChain4j.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IChatAgent {
    String chat(@UserMessage String userMessage);
}