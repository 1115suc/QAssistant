package course.QAssistant.langchain.service.impl;

import course.QAssistant.langchain.service.RagService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 服务实现类
 *
 * @author TraeAI
 * @date 2026-03-06
 * @description 实现 RagService 接口，用于文档摄入和问答功能。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatLanguageModel;

    private Assistant assistant;

    interface Assistant {
        String answer(String query);
    }

    @PostConstruct
    public void init() {
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3) // 默认返回前 3 个结果
                .minScore(0.7) // 默认阈值
                .build();

        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // 简单的记忆窗口，用于 RAG 查询时的上下文（如果需要），尽管通常是单轮查询
                .build();
    }

    /**
     * 将文档列表摄入到嵌入存储中以便检索。
     *
     * @param docs 要摄入的文档列表。
     * @throws RuntimeException 如果摄入过程失败。
     */
    @Override
    public void ingestDocuments(List<Document> docs) {
        log.info("正在摄入 {} 个文档", docs.size());
        try {
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .build();
            ingestor.ingest(docs);
            log.info("摄入完成");
        } catch (Exception e) {
            log.error("摄入文档时出错", e);
            throw new RuntimeException("摄入文档失败", e);
        }
    }

    /**
     * 向 RAG 系统提出问题并检索相关答案。
     *
     * @param question 用户的问题。
     * @return 基于检索到的文档生成的答案。
     * @throws RuntimeException 如果查询过程失败。
     */
    @Override
    public String query(String question) {
        log.info("正在查询 RAG 系统：{}", question);
        try {
            return assistant.answer(question);
        } catch (Exception e) {
            log.error("查询 RAG 系统时出错", e);
            throw new RuntimeException("查询 RAG 系统失败", e);
        }
    }
}
