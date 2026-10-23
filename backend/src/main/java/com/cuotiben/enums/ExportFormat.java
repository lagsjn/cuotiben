package com.cuotiben.enums;

/**
 * 导出格式，对应前端 constants/enums.js 的 EXPORT_FORMATS。
 */
public enum ExportFormat {

    WORD("Word 文档", "docx"),
    PDF("PDF 文件", "pdf");

    private final String label;
    private final String extension;

    ExportFormat(String label, String extension) {
        this.label = label;
        this.extension = extension;
    }

    public String getLabel() {
        return label;
    }

    public String getExtension() {
        return extension;
    }
}
