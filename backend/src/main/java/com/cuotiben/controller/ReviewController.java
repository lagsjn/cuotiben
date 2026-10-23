package com.cuotiben.controller;

import com.cuotiben.common.ApiResponse;
import com.cuotiben.common.UserContext;
import com.cuotiben.dto.HomeOverviewVO;
import com.cuotiben.dto.ReviewDtos;
import com.cuotiben.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能 8：错题复习与待复习管理
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** GET /api/reviews/queue —— 今日待复习队列 */
    @GetMapping("/queue")
    public ApiResponse<List<HomeOverviewVO.QueueItem>> queue() {
        return ApiResponse.ok(reviewService.queue(UserContext.require()));
    }

    /** POST /api/reviews —— 提交一次复习结果 */
    @PostMapping
    public ApiResponse<Void> submit(@Valid @RequestBody ReviewDtos.CreateRequest request) {
        reviewService.submit(UserContext.require(), request.mistakeId(), new ReviewDtos.SubmitRequest(
                request.result(),
                request.reasonCode(),
                request.reasonNote(),
                request.nextReviewDate(),
                request.masteryStatus()));
        return ApiResponse.ok();
    }
}
