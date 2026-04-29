package course.QAssistant.controller;

import course.QAssistant.pojo.vo.request.GenerateQuestionsRequestVO;
import course.QAssistant.pojo.vo.request.IngestDocumentsVO;
import course.QAssistant.pojo.vo.request.SaveQuizPaperVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.repository.QuizPaperService;
import course.QAssistant.service.ChatService;
import course.QAssistant.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Tag(name = "题卷管理", description = "题卷的保存、查询、删除、生成及RAG文档解析等接口")
public class QuizPaperController {

    private final QuizPaperService quizPaperService;
    private final QuizService quizService;
    private final ChatService chatService;

    @Operation(
            summary = "AI生成试题卷",
            description = "根据指定的主题、难度和各题型数量，使用AI自动生成试题卷。支持选择题、填空题、问答题三种题型。" +
                    "可关联文件ID进行RAG增强生成，也可指定自定义AI模型。返回生成的题卷ID。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @PostMapping("/generate")
    public R<String> generate(@Valid @RequestBody GenerateQuestionsRequestVO requestVO,
                              @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                              @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        String paper = quizService.generateQuiz(requestVO, token, loginType);
        return R.ok(paper);
    }

    @Operation(
            summary = "RAG文档解析",
            description = "根据文件ID列表对已上传的文档进行RAG解析，将文档内容向量化后存入向量数据库，" +
                    "用于后续的AI对话增强检索。仅允许解析自己上传的文件。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @PostMapping("/rag")
    public R ingestDocuments(@Valid @RequestBody IngestDocumentsVO ingestVO,
                             @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                             @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        return chatService.ingestDocumentsByFileIds(ingestVO.getFileIds(), token, loginType);
    }

    @Operation(
            summary = "保存题卷",
            description = "保存题卷信息，包含标题、主题、题目列表和总分",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @PostMapping("/save")
    public R save(@Valid @RequestBody SaveQuizPaperVO vo,
                  @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                  @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        return quizPaperService.saveQuizPaper(vo, token, loginType);
    }

    @Operation(
            summary = "获取当前用户的题卷列表",
            description = "查询当前登录用户的所有题卷，返回题卷的基本信息列表（标题、主题、总分、题目数量等）",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @GetMapping("/list")
    public R list(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                  @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        return quizPaperService.listQuizPapers(token, loginType);
    }

    @Operation(
            summary = "获取题卷详情",
            description = "根据题卷id获取完整的题卷信息，包括所有题目详情",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "paperId", description = "题卷ID", required = true, in = ParameterIn.PATH)
    })
    @GetMapping("/{paperId}")
    public R detail(@PathVariable("paperId") String paperId,
                    @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                    @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        return quizPaperService.getQuizPaperDetail(paperId, token, loginType);
    }

    @Operation(
            summary = "删除题卷",
            description = "根据题卷id删除指定的题卷（仅允许删除自己的题卷）",
            method = "DELETE"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "paperId", description = "题卷ID", required = true, in = ParameterIn.PATH)
    })
    @DeleteMapping("/{paperId}")
    public R delete(@PathVariable("paperId") String paperId,
                    @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                    @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        return quizPaperService.deleteQuizPaper(paperId, token, loginType);
    }
}
