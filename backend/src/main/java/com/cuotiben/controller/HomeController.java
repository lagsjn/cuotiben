package com.cuotiben.controller;

import com.cuotiben.common.ApiResponse;
import com.cuotiben.common.UserContext;
import com.cuotiben.dto.HomeOverviewVO;
import com.cuotiben.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页聚合接口：一次请求返回首屏所有数据。
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * GET /api/home/overview
     * 未登录时返回空概览（首页同时是落地页，不应 401）。
     */
    @GetMapping("/overview")
    public ApiResponse<HomeOverviewVO> overview() {
        Long userId = UserContext.get();
        return ApiResponse.ok(userId == null ? homeService.guestOverview() : homeService.overview(userId));
    }
}
