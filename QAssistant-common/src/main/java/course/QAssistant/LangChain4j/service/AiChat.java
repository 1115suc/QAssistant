package course.QAssistant.LangChain4j.service;

import dev.langchain4j.service.TokenStream;

public interface AiChat{
    TokenStream chat(String message);
}
