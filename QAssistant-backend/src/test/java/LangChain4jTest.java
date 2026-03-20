import course.QAssistant.LangChain4j.service.IChatAgent;
import course.QAssistant.QAssistantBackendApplication;
import course.QAssistant.properties.AiProperties;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = QAssistantBackendApplication.class)
public class LangChain4jTest {
    @Resource
    private AiProperties aiProperties;

    @Test
    public void test() {
        UserMessage userMessage = UserMessage.from("你好");
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(aiProperties.getApiKey())
                .baseUrl(aiProperties.getBaseUrl())
                .modelName(aiProperties.getModelName())
                .temperature(aiProperties.getTemperature() != null ? aiProperties.getTemperature() : 0.7)
                .topP(aiProperties.getTopP() != null ? aiProperties.getTopP() : 1.0)
                .maxTokens(aiProperties.getMaxTokens() != null ? aiProperties.getMaxTokens() : 2048)
                .timeout(aiProperties.getTimeout())
                .logRequests(true)
                .logResponses(true)
                .build();


        IChatAgent chatAgent = AiServices.builder(IChatAgent.class)
                .chatLanguageModel(model)
                .build();
        

        String response = chatAgent.chat("你好");
        System.out.println(response);
    }

}
