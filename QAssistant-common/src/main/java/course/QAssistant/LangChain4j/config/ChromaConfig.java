package course.QAssistant.LangChain4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Data
@Configuration
@ConfigurationProperties(prefix = "chroma")
public class ChromaConfig {

    private String baseUrl;
    private String collectionName;
    private int timeout = 30;

    @Bean
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .collectionName(collectionName)
                .timeout(Duration.ofSeconds(timeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                             EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .dynamicFilter(query -> {
                    String sessionId = (String) query.metadata()
                            .chatMemoryId();
                    if (sessionId == null || sessionId.isBlank()) {
                        return null;
                    }
                    return metadataKey("sessionId").isEqualTo(sessionId);
                })
                .build();
    }
}