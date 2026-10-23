package com.cuotiben.controller;

import com.cuotiben.common.ApiResponse;
import com.cuotiben.common.UserContext;
import com.cuotiben.dto.HomeOverviewVO;
import com.cuotiben.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 功能 9：错题统计
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** GET /api/stats/summary —— 总量、掌握状态、学科/原因分布 */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(statsService.summary(UserContext.require()));
    }

    /** GET /api/stats/trend?days=7 —— 近 N 天新增与复习趋势 */
    @GetMapping("/trend")
    public ApiResponse<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(statsService.trend(UserContext.require(), Math.min(Math.max(days, 1), 90)));
    }

    /** GET /api/stats/reasons —— 错误原因分布 */
    @GetMapping("/reasons")
    public ApiResponse<List<HomeOverviewVO.ReasonSlice>> reasons() {
        return ApiResponse.ok(statsService.reasonDistribution(UserContext.require()));
    }
}
