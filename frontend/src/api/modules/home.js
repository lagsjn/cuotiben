/**
 * 首页聚合数据
 * 对应后端 HomeController#overview（一次请求拿齐首屏所需数据，减少瀑布式请求）
 */
import { http } from '@/api/http'

/** 功能 1/7/8/9 的数据汇总，供首页概览使用 */
export function fetchHomeOverview() {
  return http.get('/home/overview')
}

/** 只取待复习队列（复习页复用同一接口） */
export function fetchReviewQueue(params) {
  return http.get('/reviews/queue', params)
}
