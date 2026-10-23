package com.cuotiben.controller;

import com.cuotiben.common.ApiResponse;
import com.cuotiben.common.PageResult;
import com.cuotiben.common.UserContext;
import com.cuotiben.dto.MistakeDetailVO;
import com.cuotiben.dto.MistakeRequest;
import com.cuotiben.dto.MistakeSummaryVO;
import com.cuotiben.dto.ReviewDtos;
import com.cuotiben.entity.ReviewRecord;
import com.cuotiben.service.MistakeService;
import com.cuotiben.service.ReviewService;
import com.cuotiben.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 功能 2/3/5/6/7：错题录入、编辑、删除、检索筛选、错误原因、掌握状态
 */
@RestController
@RequestMapping("/api/mistakes")
public class MistakeController {

    private final MistakeService mistakeService;
    private final ReviewService reviewService;
    private final UploadService uploadService;

    public MistakeController(MistakeService mistakeService,
                             ReviewService reviewService,
                             UploadService uploadService) {
        this.mistakeService = mistakeService;
        this.reviewService = reviewService;
        this.uploadService = uploadService;
    }

    /** GET /api/mistakes —— 分页检索筛选 */
    @GetMapping
    public ApiResponse<PageResult<MistakeSummaryVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) String reasonCode,
            @RequestParam(required = false) String masteryStatus,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Boolean dueOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(mistakeService.page(UserContext.require(), keyword, subjectId, knowledgePointId,
                reasonCode, masteryStatus, tagIds, dueOnly, Math.max(page, 1), Math.min(Math.max(size, 1), 100)));
    }

    /** GET /api/mistakes/{id} */
    @GetMapping("/{id}")
    public ApiResponse<MistakeDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(mistakeService.detail(UserContext.require(), id));
    }

    /** POST /api/mistakes —— 新增错题 */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody MistakeRequest request) {
        Long id = mistakeService.create(UserContext.require(), request);
        return ApiResponse.ok(Map.of("id", id));
    }

    /** PUT /api/mistakes/{id} —— 编辑错题 */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MistakeRequest request) {
        mistakeService.update(UserContext.require(), id, request);
        return ApiResponse.ok();
    }

    /** DELETE /api/mistakes/{id} */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mistakeService.delete(UserContext.require(), id);
        return ApiResponse.ok();
    }

    /** PUT /api/mistakes/{id}/mastery —— 功能 7：切换掌握状态 */
    @PutMapping("/{id}/mastery")
    public ApiResponse<Void> updateMastery(@PathVariable Long id,
                                           @Valid @RequestBody ReviewDtos.MasteryRequest request) {
        mistakeService.updateMastery(UserContext.require(), id, request.masteryStatus());
        return ApiResponse.ok();
    }

    /** PUT /api/mistakes/{id}/error-reason —— 功能 5：修改错误原因 */
    @PutMapping("/{id}/error-reason")
    public ApiResponse<Void> updateErrorReason(@PathVariable Long id,
                                               @RequestBody ReviewDtos.ErrorReasonRequest request) {
        mistakeService.updateErrorReason(UserContext.require(), id, request.reasonCode(), request.reasonNote());
        return ApiResponse.ok();
    }

    /** PUT /api/mistakes/{id}/schedule —— 功能 8：设置下次复习日期 */
    @PutMapping("/{id}/schedule")
    public ApiResponse<Void> schedule(@PathVariable Long id,
                                      @RequestBody ReviewDtos.ScheduleRequest request) {
        mistakeService.schedule(UserContext.require(), id, request.nextReviewDate());
        return ApiResponse.ok();
    }

    /** GET /api/mistakes/{id}/reviews —— 该错题的复习历史 */
    @GetMapping("/{id}/reviews")
    public ApiResponse<List<ReviewRecord>> reviews(@PathVariable Long id) {
        return ApiResponse.ok(reviewService.history(UserContext.require(), id));
    }

    /**
     * POST /api/mistakes/images —— 功能 2：上传题目截图/拍照
     * 返回 { url }，前端把 url 放进错题的 imageUrls 数组。
     */
    @PostMapping("/images")
    public ApiResponse<Map<String, String>> uploadImage(@RequestPart("file") MultipartFile file) {
        UserContext.require();
        String url = uploadService.save(file);
        return ApiResponse.ok(Map.of("url", url, "path", url));
    }
}
