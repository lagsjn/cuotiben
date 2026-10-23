/**
 * 功能 9：错题统计
 */
import { http } from '@/api/http'

/** 总量、学科/知识点题量、错误原因占比、三种掌握状态题量 */
export function fetchStatsSummary() {
  return http.get('/stats/summary')
}

/** 最近一周 / 一个月的新增题量与错误原因分布 */
export function fetchStatsTrend(params) {
  return http.get('/stats/trend', params)
}

/** 错误原因分布（可带时间范围与学科条件） */
export function fetchReasonDistribution(params) {
  return http.get('/stats/reasons', params)
}
