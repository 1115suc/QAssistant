我将把这个 README 文档翻译成中文。

# QAssistant-langchain 模块

本模块基于 LangChain4j 提供核心 AI 功能，包括聊天、嵌入、RAG 和记忆服务。

## 依赖版本矩阵

| 依赖 | 版本 | 描述 |
| --- | --- | --- |
| Spring Boot | 3.3.7 | 框架 |
| JDK | 17 | Java 版本 |
| LangChain4j | 0.33.0 | AI 框架 |
| LangChain4j OpenAI | 0.33.0 | OpenAI 集成 |
| LangChain4j Easy RAG | 0.33.0 | RAG 工具库 |

## 接口列表

### 1. ChatService（聊天服务）
提供同步和流式聊天能力。
- `String sendPrompt(String userId, String prompt)`: 发送提示并获取响应。
- `TokenStream streamPrompt(String userId, String prompt)`: 逐 token 流式输出响应。

### 2. EmbeddingService（嵌入服务）
处理文本向量化。
- `Embedding embed(String text)`: 嵌入单个文本字符串。
- `List<Embedding> batchEmbed(List<String> texts)`: 批量嵌入多个文本。

### 3. RagService（RAG 服务）
管理检索增强生成（Retrieval-Augmented Generation）。
- `void ingestDocuments(List<Document> docs)`: 将文档摄入到嵌入存储中。
- `String query(String question)`: 基于已摄入的文档回答问题。

### 4. MemoryService（记忆服务）
管理对话历史。
- `void saveHistory(String userId, List<ChatMessage> messages)`: 保存用户聊天历史。
- `List<ChatMessage> getHistory(String userId)`: 获取用户聊天历史。

## 使用示例

### 聊天服务

```java
@Autowired
private ChatService chatService;

// 同步调用
String response = chatService.sendPrompt("user123", "你好 AI");

// 流式调用
chatService.streamPrompt("user123", "讲个故事")
.onNext(token -> System.out.print(token))
.onComplete(msg -> System.out.println("完成"))
.onError(e -> e.printStackTrace())
.start();
```

### RAG 服务

```java
@Autowired
private RagService ragService;

// 摄入文档
List<Document> docs = FileSystemDocumentLoader.loadDocuments("/path/to/docs");
ragService.ingestDocuments(docs);

// 查询
String answer = ragService.query("文档里有什么内容？");
```

### 配置说明

确保在 `application.properties` 或 `application.yml` 中配置了必要的 API 密钥：

```
properties
langchain4j.open-ai.chat-model.api-key=demo
langchain4j.open-ai.embedding-model.api-key=demo
```