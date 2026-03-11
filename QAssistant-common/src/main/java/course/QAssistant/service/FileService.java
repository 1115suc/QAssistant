package course.QAssistant.service;

import course.QAssistant.pojo.dto.FileDownloadDTO;
import course.QAssistant.pojo.vo.request.FileDeleteVO;
import course.QAssistant.pojo.vo.request.FileDownloadVO;
import course.QAssistant.pojo.vo.request.FileUploadVO;
import course.QAssistant.pojo.vo.response.FileUploadRespVO;
import course.QAssistant.pojo.vo.response.R;

public interface FileService {

    R<FileUploadRespVO> upload(String token, String loginType, FileUploadVO uploadVO);

    FileDownloadDTO download(String token, String loginType, FileDownloadVO downloadVO);

    R delete(String token, String loginType, FileDeleteVO deleteVO);
}

