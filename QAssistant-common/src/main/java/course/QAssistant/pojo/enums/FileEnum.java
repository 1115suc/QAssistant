package course.QAssistant.pojo.enums;

public enum FileEnum {
    MD("md"),
    MARKDOWN("markdown"),
    DOCX("docx"),
    DOC("doc"),
    PPTX("pptx"),
    PPT("ppt"),
    XLS("xls"),
    XLSX("xlsx"),
    EXCEL("excel"),
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif"),
    WEBP("webp"),
    IMAGE("image"),
    MP4("mp4"),
    AVI("avi"),
    MOV("mov"),
    WMV("wmv"),
    FLV("flv"),
    MKV("mkv"),
    WEBM("webm"),
    VIDEO("video"),
    TEXT("text"),
    TXT("txt"),
    PDF("pdf");

    private final String suffix;

    FileEnum(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public static FileEnum getFileEnum(String suffix) {
        for (FileEnum fileEnum : values()) {
            if (fileEnum.getSuffix().equals(suffix)) {
                return fileEnum;
            }
        }
        return null;
    }
}
