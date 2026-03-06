package course.QAssistant.langchain.service;

import dev.langchain4j.data.document.Document;

import java.util.List;

/**
 * RAG 服务接口
 *
 * @author TraeAI
 * @date 2026-03-06
 * @description 提供检索增强生成（RAG）操作的内部服务接口，包括文档摄入和查询功能。
 */
public interface RagService {

    /**
     * 将文档列表摄入到嵌入存储中以便检索。
     *
     * @param docs 要摄入的文档列表。
     * @throws RuntimeException 如果摄入过程失败。
     */
    void ingestDocuments(List<Document> docs);

    /**
     * 向 RAG 系统提出问题并检索相关答案。
     *
     * @param question 用户的问题。
     * @return 基于检索到的文档生成的答案。
     * @throws RuntimeException 如果查询过程失败。
     */
    String query(String question);
}
