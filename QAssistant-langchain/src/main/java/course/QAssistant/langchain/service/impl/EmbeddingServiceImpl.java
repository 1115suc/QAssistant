package course.QAssistant.langchain.service.impl;

import course.QAssistant.langchain.service.EmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 嵌入服务实现类
 *
 * @author 1115suc
 * @date 2026-03-06
 * @description 使用 LangChain4j EmbeddingModel 实现 EmbeddingService 接口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    /**
     * 为单个文本片段生成嵌入向量。
     *
     * @param text 要嵌入的文本。
     * @return 生成的嵌入向量对象。
     * @throws RuntimeException 如果嵌入过程失败。
     */
    @Override
    public Embedding embed(String text) {
        log.info("正在嵌入长度为 {} 的文本", text.length());
        try {
            Response<Embedding> response = embeddingModel.embed(text);
            return response.content();
        } catch (Exception e) {
            log.error("嵌入文本时出错", e);
            throw new RuntimeException("嵌入文本失败", e);
        }
    }

    /**
     * 为批量文本片段生成嵌入向量。
     *
     * @param texts 要嵌入的文本列表。
     * @return 生成的嵌入向量对象列表。
     * @throws RuntimeException 如果嵌入过程失败。
     */
    @Override
    public List<Embedding> batchEmbed(List<String> texts) {
        log.info("批量嵌入 {} 个文本", texts.size());
        try {
            List<TextSegment> segments = texts.stream()
                    .map(TextSegment::from)
                    .collect(Collectors.toList());
            Response<List<Embedding>> response = embeddingModel.embedAll(segments);
            return response.content();
        } catch (Exception e) {
            log.error("批量嵌入文本时出错", e);
            throw new RuntimeException("批量嵌入文本失败", e);
        }
    }
}
