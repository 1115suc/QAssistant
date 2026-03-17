package course.QAssistant.controller;

import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.vo.request.FileDeleteVO;
import course.QAssistant.pojo.vo.request.FileListQueryVO;
import course.QAssistant.pojo.vo.request.FileUploadVO;
import course.QAssistant.pojo.vo.request.FileUrlVO;
import course.QAssistant.pojo.vo.response.FileInfoRespVO;
import course.QAssistant.pojo.vo.response.FileUploadRespVO;
import course.QAssistant.pojo.vo.response.FileUrlRespVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、删除等接口")
public class FileController {

    private final FileService fileService;

    @Operation(
            summary = "用户上传文件",
            description = "上传文件到MinIO，并写入minioFile表记录。存储路径规则：bucket/QAssistant/{uid}/{type}/{originalName}",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileUploadRespVO> upload(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                                      @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                                      @ModelAttribute FileUploadVO uploadVO) {
        return fileService.upload(token, loginType, uploadVO);
    }

    @Operation(
            summary = "删除文件",
            description = "根据文件记录id删除MinIO对象，并将minioFile记录标记为已删除（仅允许删除自己上传的且未删除的文件）",
            method = "DELETE"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "minioFile记录id", required = true, in = ParameterIn.PATH)
    })
    @VerificationInterceptor(checkLogin = true)
    @DeleteMapping("/{id}")
    public R delete(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                    @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                    @PathVariable("id") Long id) {
        FileDeleteVO deleteVO = new FileDeleteVO();
        deleteVO.setId(id);
        return fileService.delete(token, loginType, deleteVO);
    }

    @Operation(
            summary = "查询我的文件列表",
            description = "查询当前登录用户所有未删除的文件，支持文件名模糊搜索和分页。pageNum默认1，pageSize默认20（最大100）",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "fileName", description = "文件名模糊搜索（可选）", in = ParameterIn.QUERY),
            @Parameter(name = "pageNum", description = "页码（可选，默认1）", in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "每页数量（可选，默认20，最大100）", in = ParameterIn.QUERY)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/list")
    public R<List<FileInfoRespVO>> listMyFiles(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
            @Valid FileListQueryVO queryVO) {
        return fileService.listMyFiles(token, loginType, queryVO);
    }

    @Operation(
            summary = "获取文件访问URL",
            description = "根据文件记录id获取MinIO访问链接（仅允许访问自己上传的且未删除的文件）。" +
                    "urlType=preview返回临时签名URL(3小时过期)；urlType=public返回永久公开URL（需bucket为公开读取）",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "minioFile记录id", required = true, in = ParameterIn.PATH)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/url/{id}")
    public R<FileUrlRespVO> getFileUrl(
            @NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
            @PathVariable("id") Long id,
            @NotBlank(message = "urlType不能为空") @RequestParam("urlType") String urlType) {
        FileUrlVO urlVO = new FileUrlVO();
        urlVO.setId(id);
        urlVO.setUrlType(urlType);
        return fileService.getFileUrl(token, loginType, urlVO);
    }
}
