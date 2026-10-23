package com.cuotiben.service;

import com.cuotiben.common.BusinessException;
import com.cuotiben.enums.ExportFormat;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导出服务（骨架）。
 *
 * 预留说明：文档要求的「导出 Word / PDF」需要引入模板引擎或文档库，例如
 * - Word：poi-tl / Apache POI（docx）
 * - PDF：openhtmltopdf / iText（使用前端同一套 HTML+CSS 排版）
 * 后续实现时替换 {@link #export} 的方法体即可，Controller 与前端接口保持不变。
 */
@Service
public class ExportService {

    public ExportFile export(Long userId, String format, List<Long> mistakeIds) {
        ExportFormat exportFormat;
        try {
            exportFormat = ExportFormat.valueOf(format == null ? "" : format.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(40030, "导出格式仅支持 WORD / PDF");
        }
        throw new BusinessException(50100,
                "导出功能开发中：已预留接口，待接入 " + exportFormat.getLabel() + " 生成器");
    }

    /** 导出结果：文件名 + 内容 */
    public record ExportFile(String fileName, byte[] content) {
    }
}
