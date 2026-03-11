package course.QAssistant.controller;

import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.dto.FileDownloadDTO;
import course.QAssistant.pojo.vo.request.FileDeleteVO;
import course.QAssistant.pojo.vo.request.FileDownloadVO;
import course.QAssistant.pojo.vo.request.FileUploadVO;
import course.QAssistant.pojo.vo.response.FileUploadRespVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            summary = "下载文件",
            description = "根据文件记录id下载MinIO中的文件（仅允许下载自己上传的且未删除的文件）",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "minioFile记录id", required = true, in = ParameterIn.QUERY)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/download")
    public void download(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                         @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                         @RequestParam("id") Long id,
                         HttpServletResponse response) {
        FileDownloadVO downloadVO = new FileDownloadVO();
        downloadVO.setId(id);
        FileDownloadDTO downloadDTO = fileService.download(token, loginType, downloadVO);

        try (ServletOutputStream out = response.getOutputStream()) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String encoded = URLEncoder.encode(downloadDTO.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);

            try (var in = downloadDTO.getInputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
        } catch (Exception e) {
            throw new IllegalStateException("下载失败");
        }
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
}
