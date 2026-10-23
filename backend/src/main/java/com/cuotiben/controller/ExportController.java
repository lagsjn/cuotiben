package com.cuotiben.controller;

import com.cuotiben.common.UserContext;
import com.cuotiben.dto.ExportDtos;
import com.cuotiben.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能 10：导出错题集为 Word / PDF
 *
 * 说明：接口与文件流响应已就绪，ExportService 内部待接入文档生成器（见该类的说明）。
 */
@RestController
@RequestMapping("/api")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /** POST /api/export —— 返回文件流 */
    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ExportDtos.ExportRequest request) {
        ExportService.ExportFile file = exportService.export(
                UserContext.require(), request.format(), request.mistakeIds());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file.content());
    }
}
