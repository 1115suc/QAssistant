package course.QAssistant.util;

import cn.hutool.core.util.ObjectUtil;
import course.QAssistant.exception.QAException;
import course.QAssistant.pojo.enums.FileEnum;

public class QAssistantFileUtil {
    public static String getSuffix(String filename) {
        int index = filename.lastIndexOf(".");
        String suffix = filename.substring(index + 1);
        FileEnum fileEnum = FileEnum.getFileEnum(suffix);
        if (ObjectUtil.isNotNull(fileEnum)) {
            return fileEnum.getSuffix().trim();
        }else {
            throw new QAException("文件后缀名错误");
        }
    }
}
