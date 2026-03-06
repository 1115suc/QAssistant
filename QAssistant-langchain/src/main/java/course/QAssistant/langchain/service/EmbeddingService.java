package course.QAssistant.langchain.service;

import dev.langchain4j.data.embedding.Embedding;

import java.util.List;

/**
 * 嵌入服务接口
 *
 * @author TraeAI
 * @date 2026-03-06
 * @description 提供文本嵌入操作的内部服务接口。
 */
public interface EmbeddingService {

    /**
     * 为单个文本片段生成嵌入向量。
     *
     * @param text 要嵌入的文本。
     * @return 生成的嵌入向量对象。
     * @throws RuntimeException 如果嵌入过程失败。
     */
    Embedding embed(String text);

    /**
     * 为批量文本片段生成嵌入向量。
     *
     * @param texts 要嵌入的文本列表。
     * @return 生成的嵌入向量对象列表。
     * @throws RuntimeException 如果嵌入过程失败。
     */
    List<Embedding> batchEmbed(List<String> texts);
}
