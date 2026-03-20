package course.QAssistant.LangChain4j.config;

import course.QAssistant.properties.AiProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "qassistant.qdrant")
public class QdrantConfig {

    private String qdrantHost;
    private int qdrantPort;
    private String collectionName;

    @Resource
    private AiProperties aiProperties;

    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(qdrantHost, qdrantPort, false)
                        .build());
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return QdrantEmbeddingStore.builder()
                .host(qdrantHost)
                .port(qdrantPort)
                .collectionName(collectionName)
                .build();
    }

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(aiProperties.getApiKey())
                .baseUrl(aiProperties.getBaseUrl())
                .modelName(aiProperties.getModelName())
                .temperature(aiProperties.getTemperature())
                .topP(aiProperties.getTopP())
                .maxTokens(aiProperties.getMaxTokens())
                .timeout(aiProperties.getTimeout())
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingModel openAiEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(aiProperties.getEmbeddingApiKey())
                .baseUrl(aiProperties.getEmbeddingModelUrl())
                .modelName(aiProperties.getEmbeddingModelName())
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(aiProperties.getEmbeddingApiKey())
                .baseUrl(aiProperties.getEmbeddingModelUrl())
                .modelName(aiProperties.getEmbeddingModelName())
                .temperature(aiProperties.getTemperature())
                .topP(aiProperties.getTopP())
                .maxTokens(aiProperties.getMaxTokens())
                .timeout(aiProperties.getTimeout())
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}