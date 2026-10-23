package com.cuotiben.controller;

import com.cuotiben.common.ApiResponse;
import com.cuotiben.common.UserContext;
import com.cuotiben.dto.CatalogDtos;
import com.cuotiben.service.CatalogService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 功能 4：学科、知识点、标签分类管理
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** GET /api/subjects */
    @GetMapping("/subjects")
    public ApiResponse<List<CatalogDtos.SubjectVO>> subjects() {
        return ApiResponse.ok(catalogService.subjects(UserContext.require()));
    }

    /** POST /api/subjects */
    @PostMapping("/subjects")
    public ApiResponse<Map<String, Long>> createSubject(@RequestBody CatalogDtos.SubjectRequest request) {
        Long id = catalogService.createSubject(UserContext.require(), request);
        return ApiResponse.ok(Map.of("id", id));
    }

    /** PUT /api/subjects/{id} */
    @PutMapping("/subjects/{id}")
    public ApiResponse<Void> updateSubject(@PathVariable Long id,
                                           @RequestBody CatalogDtos.SubjectRequest request) {
        catalogService.updateSubject(UserContext.require(), id, request);
        return ApiResponse.ok();
    }

    /** DELETE /api/subjects/{id}（学科下有错题时拒绝删除） */
    @DeleteMapping("/subjects/{id}")
    public ApiResponse<Void> deleteSubject(@PathVariable Long id) {
        catalogService.deleteSubject(UserContext.require(), id);
        return ApiResponse.ok();
    }

    /** GET /api/subjects/{id}/knowledge-points */
    @GetMapping("/subjects/{id}/knowledge-points")
    public ApiResponse<List<CatalogDtos.KnowledgePointVO>> knowledgePoints(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.knowledgePoints(UserContext.require(), id));
    }

    /** GET /api/knowledge-points?subjectId= */
    @GetMapping("/knowledge-points")
    public ApiResponse<List<CatalogDtos.KnowledgePointVO>> allKnowledgePoints(
            @RequestParam(required = false) Long subjectId) {
        return ApiResponse.ok(catalogService.knowledgePoints(UserContext.require(), subjectId));
    }

    /** POST /api/knowledge-points */
    @PostMapping("/knowledge-points")
    public ApiResponse<Map<String, Long>> createKnowledgePoint(
            @RequestBody CatalogDtos.KnowledgePointRequest request) {
        Long id = catalogService.createKnowledgePoint(UserContext.require(), request);
        return ApiResponse.ok(Map.of("id", id));
    }

    /** GET /api/tags */
    @GetMapping("/tags")
    public ApiResponse<List<CatalogDtos.TagVO>> tags() {
        return ApiResponse.ok(catalogService.tags(UserContext.require()));
    }

    /** POST /api/tags */
    @PostMapping("/tags")
    public ApiResponse<Map<String, Long>> createTag(@RequestBody CatalogDtos.TagRequest request) {
        Long id = catalogService.createTag(UserContext.require(), request.name());
        return ApiResponse.ok(Map.of("id", id));
    }

    /** DELETE /api/tags/{id} */
    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        catalogService.deleteTag(UserContext.require(), id);
        return ApiResponse.ok();
    }
}
