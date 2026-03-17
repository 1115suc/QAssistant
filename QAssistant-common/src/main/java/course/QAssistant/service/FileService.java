package course.QAssistant.service;

import course.QAssistant.pojo.dto.FileDownloadDTO;
import course.QAssistant.pojo.vo.request.FileDeleteVO;
import course.QAssistant.pojo.vo.request.FileDownloadVO;
import course.QAssistant.pojo.vo.request.FileListQueryVO;
import course.QAssistant.pojo.vo.request.FileUploadVO;
import course.QAssistant.pojo.vo.request.FileUrlVO;
import course.QAssistant.pojo.vo.response.FileInfoRespVO;
import course.QAssistant.pojo.vo.response.FileUploadRespVO;
import course.QAssistant.pojo.vo.response.FileUrlRespVO;
import course.QAssistant.pojo.vo.response.R;

import java.util.List;

public interface FileService {

    R<FileUploadRespVO> upload(String token, String loginType, FileUploadVO uploadVO);

    R delete(String token, String loginType, FileDeleteVO deleteVO);

    R<List<FileInfoRespVO>> listMyFiles(String token, String loginType, FileListQueryVO queryVO);

    R<FileUrlRespVO> getFileUrl(String token, String loginType, FileUrlVO urlVO);
}

