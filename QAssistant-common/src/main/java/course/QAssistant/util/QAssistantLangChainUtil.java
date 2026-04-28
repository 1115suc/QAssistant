package course.QAssistant.util;

import course.QAssistant.pojo.quiz.QuizQuestion;
import course.QAssistant.pojo.quiz.QuizResult;
import course.QAssistant.pojo.vo.request.GenerateQuestionsRequestVO;
import dev.langchain4j.data.message.SystemMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QAssistantLangChainUtil {

    private final ObjectMapper objectMapper;

    // =========================================================================
    // JsonSchema 构建
    // =========================================================================

    public ResponseFormat buildResponseFormat() {

        JsonObjectSchema optionItemSchema = JsonObjectSchema.builder()
                .addProperty("key", JsonStringSchema.builder()
                        .description("选项键，固定为 A / B / C / D").build())
                .addProperty("text", JsonStringSchema.builder()
                        .description("选项的具体内容").build())
                .required("key", "text")
                .build();

        JsonArraySchema optionsSchema = JsonArraySchema.builder()
                .description("选项列表：type=choice 时必须包含 A B C D 四个选项；type=fill 或 qa 时输出空数组 []")
                .items(optionItemSchema)
                .build();

        JsonObjectSchema questionSchema = JsonObjectSchema.builder()
                .addProperty("id", JsonStringSchema.builder()
                        .description("题目唯一 ID，从 q1 开始，按顺序递增").build())
                .addProperty("type", JsonEnumSchema.builder()
                        .enumValues(List.of("choice", "fill", "qa"))
                        .description("题目类型：choice=选择题 | fill=填空题 | qa=问答题").build())
                .addProperty("content", JsonStringSchema.builder()
                        .description("题目正文；fill 题在需填写处使用 ___ 占位").build())
                .addProperty("options", optionsSchema)
                .addProperty("answer", JsonStringSchema.builder()
                        .description("答案：choice 填选项 key（如 B），fill/qa 填文字答案").build())
                .addProperty("explanation", JsonStringSchema.builder()
                        .description("解析：说明正确答案的理由，每题必须提供，不可为空").build())
                .addProperty("score", JsonIntegerSchema.builder()
                        .description("分值：choice=5，fill=5，qa=10~15").build())
                .required("id", "type", "content", "options", "answer", "explanation", "score")
                .build();

        JsonObjectSchema rootSchema = JsonObjectSchema.builder()
                .addProperty("title", JsonStringSchema.builder()
                        .description("题卷标题，格式：{主题} 练习").build())
                .addProperty("topic", JsonStringSchema.builder()
                        .description("题目主题，与用户输入保持一致").build())
                .addProperty("questions", JsonArraySchema.builder()
                        .description("题目列表，顺序：选择题 → 填空题 → 问答题")
                        .items(questionSchema).build())
                .required("title", "topic", "questions")
                .build();

        return ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("QuizResult")
                        .rootElement(rootSchema)
                        .build())
                .build();
    }

    // =========================================================================
    // Prompt 构建
    // =========================================================================

    public SystemMessage buildSystemMessage() {
        String prompt = """
            你是一位专业的在线教育题目设计专家。
            
            ## 题目质量规则
            - 题目表述清晰、无歧义，考查目标明确
            - 选择题（choice）：options 字段必须包含完整的 A B C D 四个选项对象，缺少任何一个选项均视为错误输出
            - 填空题（fill）和问答题（qa）：options 字段输出空数组 []，不得省略该字段
            - 填空题（fill）：答案应简洁准确，一般为关键词或短语
            - 问答题（qa）：参考答案应结构清晰、逻辑完整，体现核心知识点
            - explanation 字段每题必须提供，不可为空字符串，说明答案正确的原因或相关原理
            - 中文主题用中文出题，英文主题可中英混用
            
            ## 难度规则
            - easy：考查基础定义和直接用法，适合初学者
            - medium：考查原理理解和综合运用，需有一定基础
            - hard：考查边界情况、性能分析或复杂推理，面向进阶学习者
            
            ## 输出规则
            - 严格按照用户要求的数量生成各题型题目，数量精确，不多不少
            - 题目顺序：先输出所有选择题，再输出填空题，最后输出问答题
            - 题目 ID 从 q1 开始顺序递增，不跳号、不重复
            - score 分值参考：choice=5分，fill=5分，qa 根据复杂度取 10~15 分
            - 每道题的 options 和 explanation 字段都必须输出，不可省略
            """;
        return SystemMessage.from(prompt);
    }

    public UserMessage buildUserMessage(GenerateQuestionsRequestVO requestVO) {
        int choice = safeInt(requestVO.getChoiceCount());
        int fill   = safeInt(requestVO.getFillCount());
        int qa     = safeInt(requestVO.getQaCount());

        String prompt = String.format("""
                请生成以下题目：
                
                主题：%s
                难度：%s
                数量：选择题 %d 道 / 填空题 %d 道 / 问答题 %d 道（合计 %d 道）
                """,
                requestVO.getTopic(),
                formatDifficulty(requestVO.getDifficulty()),
                choice, fill, qa, choice + fill + qa
        );
        return UserMessage.from(prompt);
    }

    public UserMessage buildUserMessageForType(GenerateQuestionsRequestVO requestVO, QuestionType type) {
        GenerateQuestionsRequestVO singleTypeVO = new GenerateQuestionsRequestVO();
        singleTypeVO.setTopic(requestVO.getTopic());
        singleTypeVO.setDifficulty(requestVO.getDifficulty());
        singleTypeVO.setChoiceCount(type == QuestionType.CHOICE ? requestVO.getChoiceCount() : 0);
        singleTypeVO.setFillCount(type == QuestionType.FILL ? requestVO.getFillCount() : 0);
        singleTypeVO.setQaCount(type == QuestionType.QA ? requestVO.getQaCount() : 0);
        return buildUserMessage(singleTypeVO);
    }

    /**
     * 构建携带 RAG 检索上下文的 UserMessage
     * ragContext 为 null 时退化为普通 buildUserMessageForType 行为
     */
    public UserMessage buildUserMessageForTypeWithContext(
            GenerateQuestionsRequestVO requestVO,
            QuestionType type,
            String ragContext) {

        int choice = type == QuestionType.CHOICE ? safeInt(requestVO.getChoiceCount()) : 0;
        int fill   = type == QuestionType.FILL   ? safeInt(requestVO.getFillCount())   : 0;
        int qa     = type == QuestionType.QA     ? safeInt(requestVO.getQaCount())     : 0;

        String contextBlock = (ragContext != null && !ragContext.isBlank())
                ? """
                  ## 参考资料（请优先基于以下内容出题，不要超出资料范围）
                  %s
                  
                  ---
                  """.formatted(ragContext)
                : "";

        String prompt = contextBlock + """
                请生成以下题目：
                
                主题：%s
                难度：%s
                数量：选择题 %d 道 / 填空题 %d 道 / 问答题 %d 道（合计 %d 道）
                """.formatted(
                requestVO.getTopic(),
                formatDifficulty(requestVO.getDifficulty()),
                choice, fill, qa, choice + fill + qa
        );

        return UserMessage.from(prompt);
    }

    public enum QuestionType { CHOICE, FILL, QA }

    // =========================================================================
    // 响应解析
    // =========================================================================

    public QuizResult parseResult(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            log.warn("[Parse] 原始响应为空，返回空结果");
            return emptyQuizResult();
        }

        // 第一层：直接解析
        try {
            return objectMapper.readValue(rawJson, QuizResult.class);
        } catch (Exception e) {
            log.warn("[Parse] 直接解析失败，尝试修复截断 JSON: {}", e.getMessage());
        }

        // 第二层：修复截断后重试
        String repaired = repairTruncatedJson(rawJson);
        if (repaired != null) {
            try {
                QuizResult result = objectMapper.readValue(repaired, QuizResult.class);
                log.info("[Parse] JSON 修复成功，共解析 {} 道题",
                        result.getQuestions() == null ? 0 : result.getQuestions().size());
                return result;
            } catch (Exception e) {
                log.warn("[Parse] 修复后解析仍失败，尝试逐题抢救: {}", e.getMessage());
            }
        }

        // 第三层：逐题抢救
        List<QuizQuestion> rescued = rescueQuestions(rawJson);
        if (!rescued.isEmpty()) {
            log.info("[Parse] 抢救模式：成功恢复 {} 道题", rescued.size());
            QuizResult partial = emptyQuizResult();
            partial.setQuestions(rescued);
            return partial;
        }

        log.error("[Parse] 三层解析全部失败，rawJson 片段: {}",
                rawJson.substring(0, Math.min(200, rawJson.length())));
        throw new RuntimeException("AI 响应格式解析失败，请重试");
    }

    // =========================================================================
    // JSON 截断修复
    // =========================================================================

    private String repairTruncatedJson(String rawJson) {
        try {
            int questionsStart = rawJson.indexOf("\"questions\"");
            if (questionsStart == -1) return null;

            int arrayStart = rawJson.indexOf('[', questionsStart);
            if (arrayStart == -1) return null;

            int lastCompleteObj = findLastCompleteObject(rawJson, arrayStart);
            if (lastCompleteObj == -1) return null;

            String truncated = rawJson.substring(0, lastCompleteObj + 1);
            String repaired = truncated + closeJsonStructure(truncated);

            log.debug("[Repair] 修复前长度={}，修复后长度={}", rawJson.length(), repaired.length());
            return repaired;
        } catch (Exception e) {
            log.warn("[Repair] 修复过程异常: {}", e.getMessage());
            return null;
        }
    }

    private int findLastCompleteObject(String json, int searchFrom) {
        int lastClose = -1;
        int depth = 0;
        boolean inString = false;
        char[] chars = json.toCharArray();

        for (int i = searchFrom; i < chars.length; i++) {
            char c = chars[i];
            if (c == '"' && (i == 0 || chars[i - 1] != '\\')) {
                inString = !inString;
                continue;
            }
            if (inString) continue;
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 1) lastClose = i;
            }
        }
        return lastClose;
    }

    private String closeJsonStructure(String partial) {
        int braceCount = 0;
        int bracketCount = 0;
        boolean inString = false;
        char[] chars = partial.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '"' && (i == 0 || chars[i - 1] != '\\')) {
                inString = !inString;
                continue;
            }
            if (inString) continue;
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;
            else if (c == '[') bracketCount++;
            else if (c == ']') bracketCount--;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bracketCount; i++) sb.append(']');
        for (int i = 0; i < braceCount; i++) sb.append('}');
        return sb.toString();
    }

    // =========================================================================
    // 逐题解析
    // =========================================================================

    private List<QuizQuestion> rescueQuestions(String rawJson) {
        List<QuizQuestion> rescued = new ArrayList<>();

        int arrayStart = rawJson.indexOf('[', rawJson.indexOf("\"questions\""));
        if (arrayStart == -1) return rescued;

        int i = arrayStart + 1;
        char[] chars = rawJson.toCharArray();

        while (i < chars.length) {
            while (i < chars.length && chars[i] != '{') i++;
            if (i >= chars.length) break;

            int objStart = i;
            int depth = 0;
            boolean inStr = false;
            int objEnd = -1;

            for (int j = objStart; j < chars.length; j++) {
                char c = chars[j];
                if (c == '"' && (j == 0 || chars[j - 1] != '\\')) {
                    inStr = !inStr;
                    continue;
                }
                if (inStr) continue;
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) { objEnd = j; break; }
                }
            }

            if (objEnd == -1) break;

            String objJson = rawJson.substring(objStart, objEnd + 1);
            try {
                QuizQuestion q = objectMapper.readValue(objJson, QuizQuestion.class);
                rescued.add(q);
                log.debug("[Rescue] 成功解析题目 id={}", q.getId());
            } catch (Exception e) {
                log.warn("[Rescue] 跳过无法解析的题目对象: {}", e.getMessage());
            }
            i = objEnd + 1;
        }
        return rescued;
    }

    // =========================================================================
    // 工具方法
    // =========================================================================

    private QuizResult emptyQuizResult() {
        QuizResult r = new QuizResult();
        r.setQuestions(new ArrayList<>());
        return r;
    }

    public void validateRequest(GenerateQuestionsRequestVO requestVO) {
        int total = safeInt(requestVO.getChoiceCount())
                + safeInt(requestVO.getFillCount())
                + safeInt(requestVO.getQaCount());
        if (total <= 0) {
            throw new IllegalArgumentException("题目总数必须大于 0，请至少指定一种题型的数量");
        }
    }

    public int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    public String formatDifficulty(String difficulty) {
        if (difficulty == null) return "medium";
        return switch (difficulty.toLowerCase()) {
            case "easy"   -> "easy（简单）";
            case "medium" -> "medium（中等）";
            case "hard"   -> "hard（困难）";
            default       -> difficulty;
        };
    }
}