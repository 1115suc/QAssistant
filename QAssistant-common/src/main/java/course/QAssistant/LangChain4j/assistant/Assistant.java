package course.QAssistant.LangChain4j.assistant;

import dev.langchain4j.service.TokenStream;

public interface Assistant {
    TokenStream chat(String message);
}